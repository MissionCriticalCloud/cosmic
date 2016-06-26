//

//

package com.cloud.agent.api;

public class ReadyCommand extends Command {
    private String _details;
    private Long dcId;
    private Long hostId;

    public ReadyCommand() {
        super();
    }

    public ReadyCommand(final Long dcId, final Long hostId) {
        this(dcId);
        this.hostId = hostId;
    }

    public ReadyCommand(final Long dcId) {
        super();
        this.dcId = dcId;
    }

    public String getDetails() {
        return _details;
    }

    public void setDetails(final String details) {
        _details = details;
    }

    public Long getDataCenterId() {
        return dcId;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public Long getHostId() {
        return hostId;
    }
}
