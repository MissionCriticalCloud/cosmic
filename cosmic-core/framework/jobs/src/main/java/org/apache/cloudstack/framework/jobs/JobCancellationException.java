package org.apache.cloudstack.framework.jobs;

import com.cloud.utils.SerialVersionUID;

import java.util.concurrent.CancellationException;

/**
 * This exception is fired when the job has been cancelled
 */
public class JobCancellationException extends CancellationException {

    private static final long serialVersionUID = SerialVersionUID.AffinityConflictException;
    Reason reason;

    public JobCancellationException(final Reason reason) {
        super("The job was cancelled due to " + reason.toString());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        RequestedByUser, RequestedByCaller, TimedOut
    }
}
