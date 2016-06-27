//

//

package com.cloud.agent.api.proxy;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public class ConsoleProxyLoadAnswer extends Answer {

    private long proxyVmId;
    private String proxyVmName;

    protected ConsoleProxyLoadAnswer() {
    }

    public ConsoleProxyLoadAnswer(final Command command, final long proxyVmId, final String proxyVmName, final boolean success, final String details) {
        super(command, success, details);

        this.proxyVmId = proxyVmId;
        this.proxyVmName = proxyVmName;
    }

    public long getProxyVmId() {
        return proxyVmId;
    }

    public String getProxyVmName() {
        return proxyVmName;
    }
}
