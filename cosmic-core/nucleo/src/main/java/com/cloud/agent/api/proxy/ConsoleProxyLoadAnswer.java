package com.cloud.agent.api.proxy;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

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
