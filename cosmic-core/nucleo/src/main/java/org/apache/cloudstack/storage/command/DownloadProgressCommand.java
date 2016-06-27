//

//

package org.apache.cloudstack.storage.command;

public class DownloadProgressCommand extends DownloadCommand {
    private String jobId;
    private RequestType request;

    protected DownloadProgressCommand() {
        super();
    }

    public DownloadProgressCommand(final DownloadCommand cmd, final String jobId, final RequestType req) {
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
