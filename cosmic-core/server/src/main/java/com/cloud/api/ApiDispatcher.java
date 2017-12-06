package com.cloud.api;

import com.cloud.acl.ControlledEntity;
import com.cloud.acl.InfrastructureEntity;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.dispatch.DispatchChain;
import com.cloud.api.dispatch.DispatchChainFactory;
import com.cloud.api.dispatch.DispatchTask;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.exception.CloudException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.framework.jobs.AsyncJob;
import com.cloud.framework.jobs.AsyncJobManager;
import com.cloud.projects.Project;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiDispatcher {
    private static final Logger s_logger = LoggerFactory.getLogger(ApiDispatcher.class.getName());
    @Inject()
    protected DispatchChainFactory dispatchChainFactory;
    protected DispatchChain standardDispatchChain;
    protected DispatchChain asyncCreationDispatchChain;
    Long _createSnapshotQueueSizeLimit;
    @Inject
    AsyncJobManager _asyncMgr;
    @Inject
    AccountManager _accountMgr;
    @Inject
    EntityManager _entityMgr;

    public ApiDispatcher() {
    }

    @PostConstruct
    public void setup() {
        standardDispatchChain = dispatchChainFactory.getStandardDispatchChain();
        asyncCreationDispatchChain = dispatchChainFactory.getAsyncCreationDispatchChain();
    }

    public void setCreateSnapshotQueueSizeLimit(final Long snapshotLimit) {
        _createSnapshotQueueSizeLimit = snapshotLimit;
    }

    public void dispatchCreateCmd(final BaseAsyncCreateCmd cmd, final Map<String, String> params) throws Exception {
        asyncCreationDispatchChain.dispatch(new DispatchTask(cmd, params));
    }

    private void doAccessChecks(final BaseCmd cmd, final Map<Object, AccessType> entitiesToAccess) {
        final Account caller = CallContext.current().getCallingAccount();

        final APICommand commandAnnotation = cmd.getClass().getAnnotation(APICommand.class);
        final String apiName = commandAnnotation != null ? commandAnnotation.name() : null;

        if (!entitiesToAccess.isEmpty()) {
            for (final Object entity : entitiesToAccess.keySet()) {
                if (entity instanceof ControlledEntity) {
                    _accountMgr.checkAccess(caller, entitiesToAccess.get(entity), false, apiName, (ControlledEntity) entity);
                } else if (entity instanceof InfrastructureEntity) {
                    //FIXME: Move this code in adapter, remove code from Account manager
                }
            }
        }
    }

    public void dispatch(final BaseCmd cmd, final Map<String, String> params, final boolean execute) throws CloudException {
        // Let the chain of responsibility dispatch gradually
        standardDispatchChain.dispatch(new DispatchTask(cmd, params));

        final CallContext ctx = CallContext.current();
        ctx.setEventDisplayEnabled(cmd.isDisplay());
        if (params.get(ApiConstants.PROJECT_ID) != null) {
            final Project project = _entityMgr.findByUuidIncludingRemoved(Project.class, params.get(ApiConstants.PROJECT_ID));
            ctx.setProject(project);
        }

        // TODO This if shouldn't be here. Use polymorphism and move it to validateSpecificParameters
        if (cmd instanceof BaseAsyncCmd) {

            final BaseAsyncCmd asyncCmd = (BaseAsyncCmd) cmd;
            final String startEventId = params.get(ApiConstants.CTX_START_EVENT_ID);
            ctx.setStartEventId(Long.parseLong(startEventId));

            // Synchronise job on the object if needed
            if (asyncCmd.getJob() != null && asyncCmd.getSyncObjId() != null && asyncCmd.getSyncObjType() != null) {
                final Long queueSizeLimit;
                if (asyncCmd.getSyncObjType() != null && asyncCmd.getSyncObjType().equalsIgnoreCase(BaseAsyncCmd.snapshotHostSyncObject)) {
                    queueSizeLimit = _createSnapshotQueueSizeLimit;
                } else {
                    queueSizeLimit = 1L;
                }

                if (queueSizeLimit != null) {
                    if (!execute) {
                        // if we are not within async-execution context, enqueue the command
                        _asyncMgr.syncAsyncJobExecution((AsyncJob) asyncCmd.getJob(), asyncCmd.getSyncObjType(), asyncCmd.getSyncObjId().longValue(), queueSizeLimit);
                        return;
                    }
                } else {
                    s_logger.trace("The queue size is unlimited, skipping the synchronizing");
                }
            }
        }

        // TODO This if shouldn't be here. Use polymorphism and move it to validateSpecificParameters
        if (cmd instanceof BaseAsyncCustomIdCmd) {
            ((BaseAsyncCustomIdCmd) cmd).checkUuid();
        } else if (cmd instanceof BaseCustomIdCmd) {
            ((BaseCustomIdCmd) cmd).checkUuid();
        }

        cmd.execute();
    }
}
