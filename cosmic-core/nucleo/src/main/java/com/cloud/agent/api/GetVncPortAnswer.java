//

//

package com.cloud.agent.api;

public class GetVncPortAnswer extends Answer {
    String address;
    int port;

    protected GetVncPortAnswer() {
    }

    public GetVncPortAnswer(final GetVncPortCommand cmd, final int port) {
        super(cmd, true, null);
        this.port = port;
    }

    public GetVncPortAnswer(final GetVncPortCommand cmd, final String address, final int port) {
        super(cmd, true, null);
        this.address = address;
        this.port = port;
    }

    public GetVncPortAnswer(final GetVncPortCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
