package com.cloud.framework.rpc;

import com.cloud.framework.serializer.MessageSerializer;
import com.cloud.framework.transport.TransportAddressMapper;
import com.cloud.framework.transport.TransportMultiplexier;

public interface RpcProvider extends TransportMultiplexier {
    final static String RPC_MULTIPLEXIER = "rpc";

    MessageSerializer getMessageSerializer();

    void setMessageSerializer(MessageSerializer messageSerializer);

    boolean initialize();

    void registerRpcServiceEndpoint(RpcServiceEndpoint rpcEndpoint);

    void unregisteRpcServiceEndpoint(RpcServiceEndpoint rpcEndpoint);

    RpcClientCall newCall();

    RpcClientCall newCall(String targetAddress);

    RpcClientCall newCall(TransportAddressMapper targetAddress);

    //
    // low-level public API
    //
    void registerCall(RpcClientCall call);

    void cancelCall(RpcClientCall call);

    void sendRpcPdu(String sourceAddress, String targetAddress, String serializedPdu);
}
