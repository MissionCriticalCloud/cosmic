package org.apache.cloudstack.storage.datastore;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionService;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.async.AsyncRpcContext;
import org.apache.cloudstack.storage.command.CommandResult;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataObjectManagerImpl implements DataObjectManager {
    private static final Logger s_logger = LoggerFactory.getLogger(DataObjectManagerImpl.class);
    protected long waitingTime = 1800; // half an hour
    protected long waitingRetries = 10;
    @Inject
    ObjectInDataStoreManager objectInDataStoreMgr;
    @Inject
    DataMotionService motionSrv;

    @Override
    public void createAsync(final DataObject data, final DataStore store, final AsyncCompletionCallback<CreateCmdResult> callback, final boolean noCopy) {
        DataObjectInStore obj = objectInDataStoreMgr.findObject(data, store);
        DataObject objInStore = null;
        boolean freshNewTemplate = false;
        if (obj == null) {
            try {
                objInStore = objectInDataStoreMgr.create(data, store);
                freshNewTemplate = true;
            } catch (final Throwable e) {
                obj = objectInDataStoreMgr.findObject(data, store);
                if (obj == null) {
                    final CreateCmdResult result = new CreateCmdResult(null, null);
                    result.setSuccess(false);
                    result.setResult(e.toString());
                    callback.complete(result);
                    return;
                }
            }
        }

        if (!freshNewTemplate && obj.getState() != ObjectInDataStoreStateMachine.State.Ready) {
            try {
                objInStore = waitingForCreated(data, store);
            } catch (final Exception e) {
                final CreateCmdResult result = new CreateCmdResult(null, null);
                result.setSuccess(false);
                result.setResult(e.toString());
                callback.complete(result);
                return;
            }

            final CreateCmdResult result = new CreateCmdResult(null, null);
            callback.complete(result);
            return;
        }

        try {
            ObjectInDataStoreStateMachine.Event event = null;
            if (noCopy) {
                event = ObjectInDataStoreStateMachine.Event.CreateOnlyRequested;
            } else {
                event = ObjectInDataStoreStateMachine.Event.CreateRequested;
            }
            objectInDataStoreMgr.update(objInStore, event);
        } catch (final NoTransitionException e) {
            try {
                objectInDataStoreMgr.update(objInStore, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("state transation failed", e1);
            }
            final CreateCmdResult result = new CreateCmdResult(null, null);
            result.setSuccess(false);
            result.setResult(e.toString());
            callback.complete(result);
            return;
        } catch (final ConcurrentOperationException e) {
            try {
                objectInDataStoreMgr.update(objInStore, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("state transation failed", e1);
            }
            final CreateCmdResult result = new CreateCmdResult(null, null);
            result.setSuccess(false);
            result.setResult(e.toString());
            callback.complete(result);
            return;
        }

        final CreateContext<CreateCmdResult> context = new CreateContext<>(callback, objInStore);
        final AsyncCallbackDispatcher<DataObjectManagerImpl, CreateCmdResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().createAsynCallback(null, null)).setContext(context);

        store.getDriver().createAsync(store, objInStore, caller);
        return;
    }

    protected DataObject waitingForCreated(final DataObject dataObj, final DataStore dataStore) {
        long retries = this.waitingRetries;
        DataObjectInStore obj = null;
        do {
            try {
                Thread.sleep(waitingTime);
            } catch (final InterruptedException e) {
                s_logger.debug("sleep interrupted", e);
                throw new CloudRuntimeException("sleep interrupted", e);
            }

            obj = objectInDataStoreMgr.findObject(dataObj, dataStore);
            if (obj == null) {
                s_logger.debug("can't find object in db, maybe it's cleaned up already, exit waiting");
                break;
            }
            if (obj.getState() == ObjectInDataStoreStateMachine.State.Ready) {
                break;
            }
            retries--;
        } while (retries > 0);

        if (obj == null || retries <= 0) {
            s_logger.debug("waiting too long for template downloading, marked it as failed");
            throw new CloudRuntimeException("waiting too long for template downloading, marked it as failed");
        }
        return objectInDataStoreMgr.get(dataObj, dataStore);
    }

    protected Void createAsynCallback(final AsyncCallbackDispatcher<DataObjectManagerImpl, CreateCmdResult> callback, final CreateContext<CreateCmdResult> context) {
        final CreateCmdResult result = callback.getResult();
        final DataObject objInStrore = context.objInStrore;
        final CreateCmdResult upResult = new CreateCmdResult(null, null);
        if (result.isFailed()) {
            upResult.setResult(result.getResult());
            context.getParentCallback().complete(upResult);
            return null;
        }

        try {
            objectInDataStoreMgr.update(objInStrore, ObjectInDataStoreStateMachine.Event.OperationSuccessed);
        } catch (final NoTransitionException e) {
            try {
                objectInDataStoreMgr.update(objInStrore, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("failed to change state", e1);
            }

            upResult.setResult(e.toString());
            context.getParentCallback().complete(upResult);
            return null;
        } catch (final ConcurrentOperationException e) {
            try {
                objectInDataStoreMgr.update(objInStrore, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("failed to change state", e1);
            }

            upResult.setResult(e.toString());
            context.getParentCallback().complete(upResult);
            return null;
        }

        context.getParentCallback().complete(result);
        return null;
    }

    @Override
    public DataObject createInternalStateOnly(final DataObject data, final DataStore store) {
        final DataObjectInStore obj = objectInDataStoreMgr.findObject(data, store);
        DataObject objInStore = null;
        if (obj == null) {
            objInStore = objectInDataStoreMgr.create(data, store);
        }
        try {
            ObjectInDataStoreStateMachine.Event event = null;
            event = ObjectInDataStoreStateMachine.Event.CreateRequested;
            objectInDataStoreMgr.update(objInStore, event);

            objectInDataStoreMgr.update(objInStore, ObjectInDataStoreStateMachine.Event.OperationSuccessed);
        } catch (final NoTransitionException e) {
            s_logger.debug("Failed to update state", e);
            throw new CloudRuntimeException("Failed to update state", e);
        } catch (final ConcurrentOperationException e) {
            s_logger.debug("Failed to update state", e);
            throw new CloudRuntimeException("Failed to update state", e);
        }

        return objInStore;
    }

    @Override
    public void update(final DataObject data, final String path, final Long size) {
        throw new CloudRuntimeException("not implemented");
    }

    @Override
    public void copyAsync(final DataObject srcData, final DataObject destData, final AsyncCompletionCallback<CreateCmdResult> callback) {
        try {
            objectInDataStoreMgr.update(destData, ObjectInDataStoreStateMachine.Event.CopyingRequested);
        } catch (final NoTransitionException e) {
            s_logger.debug("failed to change state", e);
            try {
                objectInDataStoreMgr.update(destData, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("failed to further change state to OperationFailed", e1);
            }
            final CreateCmdResult res = new CreateCmdResult(null, null);
            res.setResult("Failed to change state: " + e.toString());
            callback.complete(res);
        } catch (final ConcurrentOperationException e) {
            s_logger.debug("failed to change state", e);
            try {
                objectInDataStoreMgr.update(destData, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("failed to further change state to OperationFailed", e1);
            }
            final CreateCmdResult res = new CreateCmdResult(null, null);
            res.setResult("Failed to change state: " + e.toString());
            callback.complete(res);
        }

        final CopyContext<CreateCmdResult> anotherCall = new CopyContext<>(callback, srcData, destData);
        final AsyncCallbackDispatcher<DataObjectManagerImpl, CopyCommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().copyCallback(null, null)).setContext(anotherCall);

        motionSrv.copyAsync(srcData, destData, caller);
    }

    protected Void copyCallback(final AsyncCallbackDispatcher<DataObjectManagerImpl, CopyCommandResult> callback, final CopyContext<CreateCmdResult> context) {
        final CopyCommandResult result = callback.getResult();
        final DataObject destObj = context.destObj;

        if (result.isFailed()) {
            try {
                objectInDataStoreMgr.update(destObj, Event.OperationFailed);
            } catch (final NoTransitionException e) {
                s_logger.debug("Failed to update copying state", e);
            } catch (final ConcurrentOperationException e) {
                s_logger.debug("Failed to update copying state", e);
            }
            final CreateCmdResult res = new CreateCmdResult(null, null);
            res.setResult(result.getResult());
            context.getParentCallback().complete(res);
        }

        try {
            objectInDataStoreMgr.update(destObj, ObjectInDataStoreStateMachine.Event.OperationSuccessed);
        } catch (final NoTransitionException e) {
            s_logger.debug("Failed to update copying state: ", e);
            try {
                objectInDataStoreMgr.update(destObj, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("failed to further change state to OperationFailed", e1);
            }
            final CreateCmdResult res = new CreateCmdResult(null, null);
            res.setResult("Failed to update copying state: " + e.toString());
            context.getParentCallback().complete(res);
        } catch (final ConcurrentOperationException e) {
            s_logger.debug("Failed to update copying state: ", e);
            try {
                objectInDataStoreMgr.update(destObj, ObjectInDataStoreStateMachine.Event.OperationFailed);
            } catch (final Exception e1) {
                s_logger.debug("failed to further change state to OperationFailed", e1);
            }
            final CreateCmdResult res = new CreateCmdResult(null, null);
            res.setResult("Failed to update copying state: " + e.toString());
            context.getParentCallback().complete(res);
        }
        final CreateCmdResult res = new CreateCmdResult(result.getPath(), null);
        context.getParentCallback().complete(res);
        return null;
    }

    @Override
    public void deleteAsync(final DataObject data, final AsyncCompletionCallback<CommandResult> callback) {
        try {
            objectInDataStoreMgr.update(data, Event.DestroyRequested);
        } catch (final NoTransitionException e) {
            s_logger.debug("destroy failed", e);
            final CreateCmdResult res = new CreateCmdResult(null, null);
            callback.complete(res);
        } catch (final ConcurrentOperationException e) {
            s_logger.debug("destroy failed", e);
            final CreateCmdResult res = new CreateCmdResult(null, null);
            callback.complete(res);
        }

        final DeleteContext<CommandResult> context = new DeleteContext<>(callback, data);
        final AsyncCallbackDispatcher<DataObjectManagerImpl, CommandResult> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().deleteAsynCallback(null, null)).setContext(context);

        data.getDataStore().getDriver().deleteAsync(data.getDataStore(), data, caller);
        return;
    }

    protected Void deleteAsynCallback(final AsyncCallbackDispatcher<DataObjectManagerImpl, CommandResult> callback, final DeleteContext<CommandResult> context) {
        final DataObject destObj = context.obj;

        final CommandResult res = callback.getResult();
        if (res.isFailed()) {
            try {
                objectInDataStoreMgr.update(destObj, Event.OperationFailed);
            } catch (final NoTransitionException e) {
                s_logger.debug("delete failed", e);
            } catch (final ConcurrentOperationException e) {
                s_logger.debug("delete failed", e);
            }
        } else {
            try {
                objectInDataStoreMgr.update(destObj, Event.OperationSuccessed);
            } catch (final NoTransitionException e) {
                s_logger.debug("delete failed", e);
            } catch (final ConcurrentOperationException e) {
                s_logger.debug("delete failed", e);
            }
        }

        context.getParentCallback().complete(res);
        return null;
    }

    class CreateContext<T> extends AsyncRpcContext<T> {
        final DataObject objInStrore;

        public CreateContext(final AsyncCompletionCallback<T> callback, final DataObject objInStore) {
            super(callback);
            this.objInStrore = objInStore;
        }
    }

    class CopyContext<T> extends AsyncRpcContext<T> {
        DataObject destObj;
        DataObject srcObj;

        public CopyContext(final AsyncCompletionCallback<T> callback, final DataObject srcObj, final DataObject destObj) {
            super(callback);
            this.srcObj = srcObj;
            this.destObj = destObj;
        }
    }

    class DeleteContext<T> extends AsyncRpcContext<T> {
        private final DataObject obj;

        public DeleteContext(final AsyncCompletionCallback<T> callback, final DataObject obj) {
            super(callback);
            this.obj = obj;
        }
    }
}
