package com.cloud.vm;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.jobs.AsyncJob;
import org.apache.cloudstack.framework.jobs.AsyncJobDispatcher;
import org.apache.cloudstack.framework.jobs.AsyncJobManager;
import org.apache.cloudstack.jobs.JobInfo;

import javax.inject.Inject;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmWorkJobDispatcher extends AdapterBase implements AsyncJobDispatcher {
    private static final Logger s_logger = LoggerFactory.getLogger(VmWorkJobDispatcher.class);

    @Inject
    private VirtualMachineManagerImpl _vmMgr;
    @Inject
    private AsyncJobManager _asyncJobMgr;
    @Inject
    private VMInstanceDao _instanceDao;

    private Map<String, VmWorkJobHandler> _handlers;

    public VmWorkJobDispatcher() {
    }

    public Map<String, VmWorkJobHandler> getHandlers() {
        return _handlers;
    }

    public void setHandlers(final Map<String, VmWorkJobHandler> handlers) {
        _handlers = handlers;
    }

    @Override
    public void runJob(final AsyncJob job) {
        VmWork work = null;
        try {
            final String cmd = job.getCmd();
            assert (cmd != null);

            Class<?> workClz = null;
            try {
                workClz = Class.forName(job.getCmd());
            } catch (final ClassNotFoundException e) {
                s_logger.error("VM work class " + cmd + " is not found" + ", job origin: " + job.getRelated(), e);
                _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, 0, e.getMessage());
                return;
            }

            work = VmWorkSerializer.deserialize(workClz, job.getCmdInfo());
            if (work == null) {
                s_logger.error("Unable to deserialize VM work " + job.getCmd() + ", job info: " + job.getCmdInfo() + ", job origin: " + job.getRelated());
                _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, 0, "Unable to deserialize VM work");
                return;
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Run VM work job: " + cmd + " for VM " + work.getVmId() + ", job origin: " + job.getRelated());
            }
            try {
                if (_handlers == null || _handlers.isEmpty()) {
                    s_logger.error("Invalid startup configuration, no work job handler is found. cmd: " + job.getCmd() + ", job info: " + job.getCmdInfo()
                            + ", job origin: " + job.getRelated());
                    _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, 0, "Invalid startup configuration. no job handler is found");
                    return;
                }

                final VmWorkJobHandler handler = _handlers.get(work.getHandlerName());

                if (handler == null) {
                    s_logger.error("Unable to find work job handler. handler name: " + work.getHandlerName() + ", job cmd: " + job.getCmd()
                            + ", job info: " + job.getCmdInfo() + ", job origin: " + job.getRelated());
                    _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, 0, "Unable to find work job handler");
                    return;
                }

                CallContext.register(work.getUserId(), work.getAccountId());

                try {
                    final Pair<JobInfo.Status, String> result = handler.handleVmWorkJob(work);
                    _asyncJobMgr.completeAsyncJob(job.getId(), result.first(), 0, result.second());
                } finally {
                    CallContext.unregister();
                }
            } finally {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Done with run of VM work job: " + cmd + " for VM " + work.getVmId() + ", job origin: " + job.getRelated());
                }
            }
        } catch (final InvalidParameterValueException e) {
            s_logger.error("Unable to complete " + job + ", job origin:" + job.getRelated());
            _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, 0, _asyncJobMgr.marshallResultObject(e));
        } catch (final Throwable e) {
            s_logger.error("Unable to complete " + job + ", job origin:" + job.getRelated(), e);

            //RuntimeException ex = new RuntimeException("Job failed due to exception " + e.getMessage());
            _asyncJobMgr.completeAsyncJob(job.getId(), JobInfo.Status.FAILED, 0, _asyncJobMgr.marshallResultObject(e));
        }
    }
}
