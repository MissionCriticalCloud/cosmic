package org.apache.cloudstack.framework.jobs.impl;

public interface SyncQueueItem {
    public final String AsyncJobContentType = "AsyncJob";

    /**
     * @return queue item id
     */
    long getId();

    /**
     * @return queue id
     */
    Long getQueueId();

    /**
     * @return subject object type pointed by the queue item
     */
    String getContentType();

    /**
     * @return subject object id pointed by the queue item
     */
    Long getContentId();
}
