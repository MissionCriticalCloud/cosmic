//

//

package com.cloud.agent.api.storage;

public class UploadProgressCommand extends UploadCommand {

    private String jobId;
    private RequestType request;

    protected UploadProgressCommand() {
        super();
    }

    public UploadProgressCommand(final UploadCommand cmd, final String jobId, final RequestType req) {
        super(cmd);

        this.jobId = jobId;
        this.setRequest(req);
    }

    public String getJobId() {
        return jobId;
    }

    public RequestType getRequest() {
        return request;
    }

    public void setRequest(final RequestType request) {
        this.request = request;
    }

    public static enum RequestType {
        GET_STATUS, ABORT, RESTART, PURGE, GET_OR_RESTART
    }
}
