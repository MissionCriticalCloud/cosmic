package com.cloud.vm;

import com.cloud.exception.CloudException;
import com.cloud.jobs.JobInfo;
import com.cloud.utils.Pair;

public interface VmWorkJobHandler {
    Pair<JobInfo.Status, String> handleVmWorkJob(VmWork work) throws CloudException;
}
