package org.apache.cloudstack.framework.rpc;

public interface RpcServiceEndpoint {
    /*
     * @return
     *         true call has been handled
     *         false can not find the call handler
     * @throws
     *      RpcException, exception when
     */
    boolean onCallReceive(RpcServerCall call);
}
