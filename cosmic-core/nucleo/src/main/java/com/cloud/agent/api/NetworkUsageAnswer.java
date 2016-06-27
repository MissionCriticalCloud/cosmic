//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

@LogLevel(Log4jLevel.Debug)
public class NetworkUsageAnswer extends Answer {
    String routerName;
    Long bytesSent;
    Long bytesReceived;

    protected NetworkUsageAnswer() {
    }

    public NetworkUsageAnswer(final NetworkUsageCommand cmd, final String details, final Long bytesSent, final Long bytesReceived) {
        super(cmd, true, details);
        this.bytesReceived = bytesReceived;
        this.bytesSent = bytesSent;
        routerName = cmd.getDomRName();
    }

    public NetworkUsageAnswer(final Command command, final Exception e) {
        super(command, e);
    }

    public Long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(final Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(final Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public String getRouterName() {
        return routerName;
    }
}
