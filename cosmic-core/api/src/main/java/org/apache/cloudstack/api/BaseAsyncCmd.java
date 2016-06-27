package org.apache.cloudstack.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * queryAsyncJobResult API command.
 */
public abstract class BaseAsyncCmd extends BaseCmd {

    public static final String ipAddressSyncObject = "ipaddress";
    public static final String networkSyncObject = "network";
    public static final String vpcSyncObject = "vpc";
    public static final String snapshotHostSyncObject = "snapshothost";
    public static final String gslbSyncObject = "globalserverloadbalancer";
    private static final Logger s_logger = LoggerFactory.getLogger(BaseAsyncCmd.class.getName());

    private Object job;

    @Parameter(name = "starteventid", type = CommandType.LONG)
    private Long startEventId;

    @Parameter(name = ApiConstants.CUSTOM_JOB_ID, type = CommandType.STRING)
    private String injectedJobId;

    public String getInjectedJobId() {
        return this.injectedJobId;
    }

    /**
     * For proper tracking of async commands through the system, events must be generated when the command is
     * scheduled, started, and completed. Commands should specify the type of event so that when the scheduled,
     * started, and completed events are saved to the events table, they have the proper type information.
     *
     * @return a string representing the type of event, e.g. VM.START, VOLUME.CREATE.
     */
    public abstract String getEventType();

    /**
     * For proper tracking of async commands through the system, events must be generated when the command is
     * scheduled, started, and completed. Commands should specify a description for these events so that when
     * the scheduled, started, and completed events are saved to the events table, they have a meaningful description.
     *
     * @return a string representing a description of the event
     */
    public abstract String getEventDescription();

    public Long getStartEventId() {
        return startEventId;
    }

    public void setStartEventId(final Long startEventId) {
        this.startEventId = startEventId;
    }

    /**
     * Async commands that want to be tracked as part of the listXXX commands need to
     * provide implementations of the two following methods, getInstanceId() and getInstanceType()
     * <p>
     * getObjectId() should return the id of the object the async command is executing on
     * getObjectType() should return a type from the AsyncJob.Type enumeration
     */
    public Long getInstanceId() {
        return null;
    }

    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.None;
    }

    public String getSyncObjType() {
        return null;
    }

    public Long getSyncObjId() {
        return null;
    }

    public Object getJob() {
        return job;
    }

    public void setJob(final Object job) {
        this.job = job;
    }
}
