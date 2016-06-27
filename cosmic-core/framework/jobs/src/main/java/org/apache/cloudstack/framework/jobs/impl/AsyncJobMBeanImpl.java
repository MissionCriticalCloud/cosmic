package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.DateUtil;
import org.apache.cloudstack.framework.jobs.AsyncJob;
import org.apache.cloudstack.framework.jobs.AsyncJobMBean;

import javax.management.StandardMBean;
import java.util.Date;
import java.util.TimeZone;

public class AsyncJobMBeanImpl extends StandardMBean implements AsyncJobMBean {
    private final AsyncJob _job;

    public AsyncJobMBeanImpl(final AsyncJob job) {
        super(AsyncJobMBean.class, false);

        _job = job;
    }

    @Override
    public long getAccountId() {
        return _job.getAccountId();
    }

    @Override
    public long getUserId() {
        return _job.getUserId();
    }

    @Override
    public String getCmd() {
        return _job.getCmd();
    }

    @Override
    public String getCmdInfo() {
        return _job.getCmdInfo();
    }

    @Override
    public String getStatus() {
        switch (_job.getStatus()) {
            case SUCCEEDED:
                return "Completed";

            case IN_PROGRESS:
                return "In progress";

            case FAILED:
                return "Failed";

            case CANCELLED:
                return "Cancelled";
        }

        return "Unknown";
    }

    @Override
    public int getProcessStatus() {
        return _job.getProcessStatus();
    }

    @Override
    public int getResultCode() {
        return _job.getResultCode();
    }

    @Override
    public String getResult() {
        return _job.getResult();
    }

    @Override
    public String getInstanceType() {
        if (_job.getInstanceType() != null) {
            return _job.getInstanceType().toString();
        }
        return "N/A";
    }

    @Override
    public String getInstanceId() {
        if (_job.getInstanceId() != null) {
            return String.valueOf(_job.getInstanceId());
        }
        return "N/A";
    }

    @Override
    public String getInitMsid() {
        if (_job.getInitMsid() != null) {
            return String.valueOf(_job.getInitMsid());
        }
        return "N/A";
    }

    @Override
    public String getCreateTime() {
        final Date time = _job.getCreated();
        if (time != null) {
            return DateUtil.getDateDisplayString(TimeZone.getDefault(), time);
        }
        return "N/A";
    }

    @Override
    public String getLastUpdateTime() {
        final Date time = _job.getLastUpdated();
        if (time != null) {
            return DateUtil.getDateDisplayString(TimeZone.getDefault(), time);
        }
        return "N/A";
    }

    @Override
    public String getLastPollTime() {
        final Date time = _job.getLastPolled();

        if (time != null) {
            return DateUtil.getDateDisplayString(TimeZone.getDefault(), time);
        }
        return "N/A";
    }

    @Override
    public String getSyncQueueId() {
        final SyncQueueItem item = _job.getSyncSource();
        if (item != null && item.getQueueId() != null) {
            return String.valueOf(item.getQueueId());
        }
        return "N/A";
    }

    @Override
    public String getSyncQueueContentType() {
        final SyncQueueItem item = _job.getSyncSource();
        if (item != null) {
            return item.getContentType();
        }
        return "N/A";
    }

    @Override
    public String getSyncQueueContentId() {
        final SyncQueueItem item = _job.getSyncSource();
        if (item != null && item.getContentId() != null) {
            return String.valueOf(item.getContentId());
        }
        return "N/A";
    }
}
