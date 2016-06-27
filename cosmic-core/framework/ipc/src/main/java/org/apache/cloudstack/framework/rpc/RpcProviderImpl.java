package org.apache.cloudstack.framework.rpc;

import org.apache.cloudstack.framework.serializer.MessageSerializer;
import org.apache.cloudstack.framework.transport.TransportAddress;
import org.apache.cloudstack.framework.transport.TransportAddressMapper;
import org.apache.cloudstack.framework.transport.TransportEndpoint;
import org.apache.cloudstack.framework.transport.TransportEndpointSite;
import org.apache.cloudstack.framework.transport.TransportProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpcProviderImpl implements RpcProvider {
    public static final String RPC_MULTIPLEXIER = "rpc";

    private TransportProvider _transportProvider;
    private String _transportAddress;
    private final RpcTransportEndpoint _transportEndpoint = new RpcTransportEndpoint();    // transport attachment at RPC layer

    private MessageSerializer _messageSerializer;
    private final List<RpcServiceEndpoint> _serviceEndpoints = new ArrayList<>();
    private final Map<Long, RpcClientCall> _outstandingCalls = new HashMap<>();

    private long _nextCallTag = System.currentTimeMillis();

    public RpcProviderImpl() {
    }

    public RpcProviderImpl(final TransportProvider transportProvider) {
        _transportProvider = transportProvider;
    }

    public TransportProvider getTransportProvider() {
        return _transportProvider;
    }

    public void setTransportProvider(final TransportProvider transportProvider) {
        _transportProvider = transportProvider;
    }

    @Override
    public void onTransportMessage(final String senderEndpointAddress, final String targetEndpointAddress, final String multiplexer, final String message) {
        assert (_messageSerializer != null);

        final Object pdu = _messageSerializer.serializeFrom(message);
        if (pdu instanceof RpcCallRequestPdu) {
            handleCallRequestPdu(senderEndpointAddress, targetEndpointAddress, (RpcCallRequestPdu) pdu);
        } else if (pdu instanceof RpcCallResponsePdu) {
            handleCallResponsePdu(senderEndpointAddress, targetEndpointAddress, (RpcCallResponsePdu) pdu);
        } else {
            assert (false);
        }
    }

    private void handleCallRequestPdu(final String sourceAddress, final String targetAddress, final RpcCallRequestPdu pdu) {
        try {
            final RpcServerCall call = new RpcServerCallImpl(this, sourceAddress, targetAddress, pdu);

            // TODO, we are trying to avoid locking when calling into callbacks
            // this should be optimized later
            final List<RpcServiceEndpoint> endpoints = new ArrayList<>();
            synchronized (_serviceEndpoints) {
                endpoints.addAll(_serviceEndpoints);
            }

            for (final RpcServiceEndpoint endpoint : endpoints) {
                if (endpoint.onCallReceive(call)) {
                    return;
                }
            }

            final RpcCallResponsePdu responsePdu = new RpcCallResponsePdu();
            responsePdu.setCommand(pdu.getCommand());
            responsePdu.setRequestStartTick(pdu.getRequestStartTick());
            responsePdu.setRequestTag(pdu.getRequestTag());
            responsePdu.setResult(RpcCallResponsePdu.RESULT_HANDLER_NOT_EXIST);
            sendRpcPdu(targetAddress, sourceAddress, _messageSerializer.serializeTo(RpcCallResponsePdu.class, responsePdu));
        } catch (final Throwable e) {

            final RpcCallResponsePdu responsePdu = new RpcCallResponsePdu();
            responsePdu.setCommand(pdu.getCommand());
            responsePdu.setRequestStartTick(pdu.getRequestStartTick());
            responsePdu.setRequestTag(pdu.getRequestTag());
            responsePdu.setResult(RpcCallResponsePdu.RESULT_HANDLER_EXCEPTION);

            sendRpcPdu(targetAddress, sourceAddress, _messageSerializer.serializeTo(RpcCallResponsePdu.class, responsePdu));
        }
    }

    private void handleCallResponsePdu(final String sourceAddress, final String targetAddress, final RpcCallResponsePdu pdu) {
        RpcClientCallImpl call = null;

        synchronized (this) {
            call = (RpcClientCallImpl) _outstandingCalls.remove(pdu.getRequestTag());
        }

        if (call != null) {
            switch (pdu.getResult()) {
                case RpcCallResponsePdu.RESULT_SUCCESSFUL:
                    call.complete(pdu.getSerializedResult());
                    break;

                case RpcCallResponsePdu.RESULT_HANDLER_NOT_EXIST:
                    call.complete(new RpcException("Handler does not exist"));
                    break;

                case RpcCallResponsePdu.RESULT_HANDLER_EXCEPTION:
                    call.complete(new RpcException("Exception in handler"));
                    break;

                default:
                    assert (false);
                    break;
            }
        }
    }

    @Override
    public MessageSerializer getMessageSerializer() {
        return _messageSerializer;
    }

    @Override
    public void setMessageSerializer(final MessageSerializer messageSerializer) {
        assert (messageSerializer != null);
        _messageSerializer = messageSerializer;
    }

    @Override
    public boolean initialize() {
        if (_transportProvider == null) {
            return false;
        }
        final TransportEndpointSite endpointSite = _transportProvider.attach(_transportEndpoint, "RpcProvider");
        endpointSite.registerMultiplexier(RPC_MULTIPLEXIER, this);
        return true;
    }

    @Override
    public void registerRpcServiceEndpoint(final RpcServiceEndpoint rpcEndpoint) {
        synchronized (_serviceEndpoints) {
            _serviceEndpoints.add(rpcEndpoint);
        }
    }

    @Override
    public void unregisteRpcServiceEndpoint(final RpcServiceEndpoint rpcEndpoint) {
        synchronized (_serviceEndpoints) {
            _serviceEndpoints.remove(rpcEndpoint);
        }
    }

    @Override
    public RpcClientCall newCall() {
        return newCall(TransportAddress.getLocalPredefinedTransportAddress("RpcProvider").toString());
    }

    @Override
    public RpcClientCall newCall(final String targetAddress) {

        final long callTag = getNextCallTag();
        final RpcClientCallImpl call = new RpcClientCallImpl(this);
        call.setSourceAddress(_transportAddress);
        call.setTargetAddress(targetAddress);
        call.setCallTag(callTag);

        return call;
    }

    @Override
    public RpcClientCall newCall(final TransportAddressMapper targetAddress) {
        return newCall(targetAddress.getAddress());
    }

    @Override
    public void registerCall(final RpcClientCall call) {
        assert (call != null);
        synchronized (this) {
            _outstandingCalls.put(((RpcClientCallImpl) call).getCallTag(), call);
        }
    }

    @Override
    public void cancelCall(final RpcClientCall call) {
        synchronized (this) {
            _outstandingCalls.remove(((RpcClientCallImpl) call).getCallTag());
        }

        ((RpcClientCallImpl) call).complete(new RpcException("Call is cancelled"));
    }

    @Override
    public void sendRpcPdu(final String sourceAddress, final String targetAddress, final String serializedPdu) {
        assert (_transportProvider != null);
        _transportProvider.sendMessage(sourceAddress, targetAddress, RpcProvider.RPC_MULTIPLEXIER, serializedPdu);
    }

    protected synchronized long getNextCallTag() {
        long tag = _nextCallTag++;
        if (tag == 0) {
            tag++;
        }

        return tag;
    }

    private class RpcTransportEndpoint implements TransportEndpoint {

        @Override
        public void onAttachConfirm(final boolean bSuccess, final String endpointAddress) {
            if (bSuccess) {
                _transportAddress = endpointAddress;
            }
        }

        @Override
        public void onDetachIndication(final String endpointAddress) {
            if (_transportAddress != null && _transportAddress.equals(endpointAddress)) {
                _transportAddress = null;
            }
        }

        @Override
        public void onTransportMessage(final String senderEndpointAddress, final String targetEndpointAddress, final String multiplexer, final String message) {

            // we won't handle generic transport message toward RPC transport endpoint
        }
    }
}
