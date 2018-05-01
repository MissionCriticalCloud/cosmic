package com.cloud.storage.datastore;

import com.cloud.engine.subsystem.api.storage.CopyCommandResult;
import com.cloud.engine.subsystem.api.storage.CreateCmdResult;
import com.cloud.engine.subsystem.api.storage.DataMotionService;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import com.cloud.framework.async.AsyncCallbackDispatcher;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.framework.async.AsyncRpcContext;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.utils.fsm.NoTransitionException;

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
    public void update(final DataObject data, final String path, final Long size) {
        throw new CloudRuntimeException("not implemented");
    }

    @Override
    public void copyAsync(final DataObject srcData, final DataObject destData, final AsyncCompletionCallback<CreateCmdResult> callback) {
        try {
            objectInDataStoreMgr.update(destData, ObjectInDataStoreStateMachine.Event.CopyingRequested);
        } catch (final NoTransitionException | ConcurrentOperationException e) {
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
        } catch (final NoTransitionException | ConcurrentOperationException e) {
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

    class CopyContext<T> extends AsyncRpcContext<T> {
        DataObject destObj;
        DataObject srcObj;

        public CopyContext(final AsyncCompletionCallback<T> callback, final DataObject srcObj, final DataObject destObj) {
            super(callback);
            this.srcObj = srcObj;
            this.destObj = destObj;
        }
    }
}
