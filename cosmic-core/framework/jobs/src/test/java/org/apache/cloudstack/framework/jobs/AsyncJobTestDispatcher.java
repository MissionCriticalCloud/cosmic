package org.apache.cloudstack.framework.jobs;

import com.cloud.utils.component.AdapterBase;
import org.apache.cloudstack.jobs.JobInfo.Status;

import javax.inject.Inject;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJobTestDispatcher extends AdapterBase implements AsyncJobDispatcher {
    private static final Logger s_logger =
            LoggerFactory.getLogger(AsyncJobTestDispatcher.class);
    Random _random = new Random();
    @Inject
    private AsyncJobManager _asyncJobMgr;
    @Inject
    private AsyncJobTestDashboard _testDashboard;

    public AsyncJobTestDispatcher() {
    }

    @Override
    public void runJob(final AsyncJob job) {
        _testDashboard.increaseConcurrency();

        s_logger.info("Execute job " + job.getId() + ", current concurrency " + _testDashboard.getConcurrencyCount());

        final int interval = 3000;

        try {
            Thread.sleep(interval);
        } catch (final InterruptedException e) {
            s_logger.debug("[ignored] .");
        }

        _asyncJobMgr.completeAsyncJob(job.getId(), Status.SUCCEEDED, 0, null);

        _testDashboard.decreaseConcurrency();
        _testDashboard.jobCompleted();
    }
}
