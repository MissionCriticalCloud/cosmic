package org.apache.cloudstack.framework.rpc;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "RpcRequest")
public class RpcCallRequestPdu {

    private long requestTag;
    private long requestStartTick;

    private String command;
    private String serializedCommandArg;

    public RpcCallRequestPdu() {
        requestTag = 0;
        requestStartTick = System.currentTimeMillis();
    }

    public long getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(final long requestTag) {
        this.requestTag = requestTag;
    }

    public long getRequestStartTick() {
        return requestStartTick;
    }

    public void setRequestStartTick(final long requestStartTick) {
        this.requestStartTick = requestStartTick;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public String getSerializedCommandArg() {
        return serializedCommandArg;
    }

    public void setSerializedCommandArg(final String serializedCommandArg) {
        this.serializedCommandArg = serializedCommandArg;
    }
}
