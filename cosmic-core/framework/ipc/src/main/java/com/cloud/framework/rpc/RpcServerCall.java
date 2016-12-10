package com.cloud.framework.rpc;

public interface RpcServerCall {
    String getCommand();

    <T> T getCommandArgument();

    // for receiver to response call
    void completeCall(Object returnObject);
}
