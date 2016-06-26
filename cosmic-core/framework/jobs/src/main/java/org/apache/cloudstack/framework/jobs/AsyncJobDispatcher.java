package org.apache.cloudstack.framework.jobs;

import com.cloud.utils.component.Adapter;

//
// We extend it from Adapter interface for
//    1)    getName()/setName()
//    2)    Confirming to general adapter pattern used across CloudStack
//
public interface AsyncJobDispatcher extends Adapter {
    void runJob(AsyncJob job);
}
