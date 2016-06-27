package org.apache.cloudstack.framework.rpc;

public interface RpcClientCall {
    final static int DEFAULT_RPC_TIMEOUT = 10000;

    String getCommand();

    RpcClientCall setCommand(String cmd);

    RpcClientCall setTimeout(int timeoutMilliseconds);

    Object getCommandArg();

    RpcClientCall setCommandArg(Object arg);

    <T> T getContext();

    RpcClientCall setContext(Object param);

    <T> RpcClientCall addCallbackListener(RpcCallbackListener<T> listener);

    RpcClientCall setCallbackDispatcher(RpcCallbackDispatcher dispatcher);

    RpcClientCall setOneway();

    RpcClientCall apply();

    void cancel();

    /**
     * @return the result object， it may also throw RpcException to indicate RPC failures
     */
    <T> T get();
}
