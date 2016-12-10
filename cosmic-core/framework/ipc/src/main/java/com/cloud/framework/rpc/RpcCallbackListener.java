package com.cloud.framework.rpc;

public interface RpcCallbackListener<T> {
    void onSuccess(T result);

    void onFailure(RpcException e);
}
