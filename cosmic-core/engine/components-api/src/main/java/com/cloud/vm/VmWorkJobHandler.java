package com.cloud.vm;

import com.cloud.utils.Pair;
import org.apache.cloudstack.jobs.JobInfo;

public interface VmWorkJobHandler {
    Pair<JobInfo.Status, String> handleVmWorkJob(VmWork work) throws Exception;
}
