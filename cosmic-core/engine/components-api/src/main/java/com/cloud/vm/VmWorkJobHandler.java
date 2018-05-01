package com.cloud.vm;

import com.cloud.jobs.JobInfo;
import com.cloud.legacymodel.exceptions.CloudException;
import com.cloud.legacymodel.utils.Pair;

public interface VmWorkJobHandler {
    Pair<JobInfo.Status, String> handleVmWorkJob(VmWork work) throws CloudException;
}
