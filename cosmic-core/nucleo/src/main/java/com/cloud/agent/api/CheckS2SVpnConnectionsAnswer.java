//

//

package com.cloud.agent.api;

import java.util.HashMap;
import java.util.Map;

public class CheckS2SVpnConnectionsAnswer extends Answer {
    Map<String, Boolean> ipToConnected;
    Map<String, String> ipToDetail;
    String details;

    protected CheckS2SVpnConnectionsAnswer() {
        ipToConnected = new HashMap<>();
        ipToDetail = new HashMap<>();
    }

    public CheckS2SVpnConnectionsAnswer(final CheckS2SVpnConnectionsCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
        ipToConnected = new HashMap<>();
        ipToDetail = new HashMap<>();
        this.details = details;
        if (result) {
            parseDetails(details);
        }
    }

    protected void parseDetails(final String details) {
        final String[] lines = details.split("&");
        for (final String line : lines) {
            final String[] words = line.split(":");
            if (words.length != 3) {
                //Not something we can parse
                return;
            }
            final String ip = words[0];
            final boolean connected = words[1].equals("0");
            final String detail = words[2];
            ipToConnected.put(ip, connected);
            ipToDetail.put(ip, detail);
        }
    }

    public boolean isConnected(final String ip) {
        if (this.getResult()) {
            final Boolean status = ipToConnected.get(ip);

            if (status != null) {
                return status;
            }
        }
        return false;
    }

    public String getDetail(final String ip) {
        if (this.getResult()) {
            return ipToDetail.get(ip);
        }
        return null;
    }
}
