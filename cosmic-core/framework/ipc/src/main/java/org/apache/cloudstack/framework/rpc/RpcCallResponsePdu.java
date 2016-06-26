package org.apache.cloudstack.framework.rpc;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "RpcResponse")
public class RpcCallResponsePdu {
    public static final int RESULT_SUCCESSFUL = 0;
    public static final int RESULT_HANDLER_NOT_EXIST = 1;
    public static final int RESULT_HANDLER_EXCEPTION = 2;

    private long requestTag;
    private long requestStartTick;

    private int result;
    private String command;
    private String serializedResult;

    public RpcCallResponsePdu() {
        requestTag = 0;
        requestStartTick = 0;
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

    public int getResult() {
        return result;
    }

    public void setResult(final int result) {
        this.result = result;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public String getSerializedResult() {
        return serializedResult;
    }

    public void setSerializedResult(final String serializedResult) {
        this.serializedResult = serializedResult;
    }
}
