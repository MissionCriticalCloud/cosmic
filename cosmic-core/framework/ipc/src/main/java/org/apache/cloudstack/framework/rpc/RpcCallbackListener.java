package org.apache.cloudstack.framework.rpc;

public interface RpcCallbackListener<T> {
    void onSuccess(T result);

    void onFailure(RpcException e);
}
