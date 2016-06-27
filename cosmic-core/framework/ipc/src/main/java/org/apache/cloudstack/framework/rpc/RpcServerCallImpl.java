package org.apache.cloudstack.framework.rpc;

public class RpcServerCallImpl implements RpcServerCall {

    private final RpcProvider _rpcProvider;
    private final String _sourceAddress;
    private final String _targetAddress;

    private final RpcCallRequestPdu _requestPdu;

    public RpcServerCallImpl(final RpcProvider provider, final String sourceAddress, final String targetAddress, final RpcCallRequestPdu requestPdu) {

        _rpcProvider = provider;
        _sourceAddress = sourceAddress;
        _targetAddress = targetAddress;
        _requestPdu = requestPdu;
    }

    @Override
    public String getCommand() {
        assert (_requestPdu != null);
        return _requestPdu.getCommand();
    }

    @Override
    public <T> T getCommandArgument() {
        if (_requestPdu.getSerializedCommandArg() == null) {
            return null;
        }

        assert (_rpcProvider.getMessageSerializer() != null);
        return _rpcProvider.getMessageSerializer().serializeFrom(_requestPdu.getSerializedCommandArg());
    }

    @Override
    public void completeCall(final Object returnObject) {
        assert (_sourceAddress != null);
        assert (_targetAddress != null);

        final RpcCallResponsePdu pdu = new RpcCallResponsePdu();
        pdu.setCommand(_requestPdu.getCommand());
        pdu.setRequestTag(_requestPdu.getRequestTag());
        pdu.setRequestStartTick(_requestPdu.getRequestStartTick());
        pdu.setRequestStartTick(RpcCallResponsePdu.RESULT_SUCCESSFUL);
        if (returnObject != null) {
            assert (_rpcProvider.getMessageSerializer() != null);
            pdu.setSerializedResult(_rpcProvider.getMessageSerializer().serializeTo(returnObject.getClass(), returnObject));
        }

        _rpcProvider.sendRpcPdu(_sourceAddress, _targetAddress, _rpcProvider.getMessageSerializer().serializeTo(RpcCallResponsePdu.class, pdu));
    }
}
