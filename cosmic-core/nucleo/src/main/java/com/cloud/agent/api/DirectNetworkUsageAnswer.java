//

//

package com.cloud.agent.api;

import java.util.HashMap;
import java.util.Map;

public class DirectNetworkUsageAnswer extends Answer {

    Map<String, long[]> ipBytesSentAndReceived;

    protected DirectNetworkUsageAnswer() {
    }

    public DirectNetworkUsageAnswer(final Command command) {
        super(command);
        this.ipBytesSentAndReceived = new HashMap<>();
    }

    public DirectNetworkUsageAnswer(final Command command, final Exception e) {
        super(command, e);
        this.ipBytesSentAndReceived = null;
    }

    public void put(final String ip, final long[] bytesSentAndReceived) {
        this.ipBytesSentAndReceived.put(ip, bytesSentAndReceived);
    }

    public long[] get(final String ip) {
        final long[] entry = ipBytesSentAndReceived.get(ip);
        if (entry == null) {
            ipBytesSentAndReceived.put(ip, new long[]{0, 0});
            return ipBytesSentAndReceived.get(ip);
        } else {
            return entry;
        }
    }

    public Map<String, long[]> getIpBytesSentAndReceived() {
        return ipBytesSentAndReceived;
    }
}
