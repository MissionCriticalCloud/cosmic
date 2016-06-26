package org.apache.cloudstack.framework.rpc;

import java.util.ArrayList;
import java.util.List;

public class RpcClientCallImpl implements RpcClientCall {

    private String _command;
    private Object _commandArg;

    private int _timeoutMilliseconds = DEFAULT_RPC_TIMEOUT;
    private Object _contextObject;
    private boolean _oneway = false;

    private final List<RpcCallbackListener> _callbackListeners = new ArrayList<>();

    private RpcCallbackDispatcher _callbackDispatcher;

    private final RpcProvider _rpcProvider;
    private long _startTickInMs;
    private long _callTag;
    private String _sourceAddress;
    private String _targetAddress;

    private final Object _responseLock = new Object();
    private boolean _responseDone = false;
    private Object _responseResult;

    public RpcClientCallImpl(final RpcProvider rpcProvider) {
        assert (rpcProvider != null);
        _rpcProvider = rpcProvider;
    }

    public void complete(final String result) {
        synchronized (_responseLock) {
            _responseResult = result;
            _responseDone = true;
            _responseLock.notifyAll();
        }

        if (_callbackListeners.size() > 0) {
            assert (_rpcProvider.getMessageSerializer() != null);
            final Object resultObject = _rpcProvider.getMessageSerializer().serializeFrom(result);
            for (final
            RpcCallbackListener listener : _callbackListeners) {
                listener.onSuccess(resultObject);
            }
        } else {
            if (_callbackDispatcher != null) {
                _callbackDispatcher.dispatch(this);
            }
        }
    }

    public void complete(final RpcException e) {

        synchronized (_responseLock) {
            _responseResult = e;
            _responseDone = true;
            _responseLock.notifyAll();
        }

        if (_callbackListeners.size() > 0) {
            for (final
            RpcCallbackListener listener : _callbackListeners) {
                listener.onFailure(e);
            }
        } else {
            if (_callbackDispatcher != null) {
                _callbackDispatcher.dispatch(this);
            }
        }
    }

    @Override
    public String getCommand() {
        return _command;
    }

    @Override
    public RpcClientCall setCommand(final String cmd) {
        _command = cmd;
        return this;
    }

    @Override
    public RpcClientCall setTimeout(final int timeoutMilliseconds) {
        _timeoutMilliseconds = timeoutMilliseconds;
        return this;
    }

    @Override
    public RpcClientCall setCommandArg(final Object arg) {
        _commandArg = arg;
        return this;
    }

    @Override
    public Object getCommandArg() {
        return _commandArg;
    }

    @Override
    public RpcClientCall setContext(final Object param) {
        _contextObject = param;
        return this;
    }

    @Override
    public <T> T getContext() {
        return (T) _contextObject;
    }

    @Override
    public <T> RpcClientCall addCallbackListener(final RpcCallbackListener<T> listener) {
        assert (listener != null);
        _callbackListeners.add(listener);
        return this;
    }

    @Override
    public RpcClientCall setCallbackDispatcher(final RpcCallbackDispatcher dispatcher) {
        _callbackDispatcher = dispatcher;
        return this;
    }

    @Override
    public RpcClientCall setOneway() {
        _oneway = true;
        return this;
    }

    public String getSourceAddress() {
        return _sourceAddress;
    }

    public void setSourceAddress(final String sourceAddress) {
        _sourceAddress = sourceAddress;
    }

    public String getTargetAddress() {
        return _targetAddress;
    }

    public void setTargetAddress(final String targetAddress) {
        _targetAddress = targetAddress;
    }

    public long getCallTag() {
        return _callTag;
    }

    public void setCallTag(final long callTag) {
        _callTag = callTag;
    }

    @Override
    public RpcClientCall apply() {
        // sanity check
        assert (_sourceAddress != null);
        assert (_targetAddress != null);

        if (!_oneway) {
            _rpcProvider.registerCall(this);
        }

        final RpcCallRequestPdu pdu = new RpcCallRequestPdu();
        pdu.setCommand(getCommand());
        if (_commandArg != null) {
            pdu.setSerializedCommandArg(_rpcProvider.getMessageSerializer().serializeTo(_commandArg.getClass(), _commandArg));
        }
        pdu.setRequestTag(this.getCallTag());

        _rpcProvider.sendRpcPdu(getSourceAddress(), getTargetAddress(), _rpcProvider.getMessageSerializer().serializeTo(RpcCallRequestPdu.class, pdu));

        return this;
    }

    @Override
    public void cancel() {
        _rpcProvider.cancelCall(this);
    }

    @Override
    public <T> T get() {
        if (!_oneway) {
            synchronized (_responseLock) {
                if (!_responseDone) {
                    long timeToWait = _timeoutMilliseconds - (System.currentTimeMillis() - _startTickInMs);
                    if (timeToWait < 0) {
                        timeToWait = 0;
                    }

                    try {
                        _responseLock.wait(timeToWait);
                    } catch (final InterruptedException e) {
                        throw new RpcTimeoutException("RPC call timed out");
                    }
                }

                assert (_responseDone);

                if (_responseResult == null) {
                    return null;
                }

                if (_responseResult instanceof RpcException) {
                    throw (RpcException) _responseResult;
                }

                assert (_rpcProvider.getMessageSerializer() != null);
                assert (_responseResult instanceof String);
                return _rpcProvider.getMessageSerializer().serializeFrom((String) _responseResult);
            }
        }
        return null;
    }
}
