package org.apache.cloudstack.framework.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportEndpointSite {
    private final TransportProvider _provider;
    private final TransportEndpoint _endpoint;
    private TransportAddress _address;

    private final List<TransportPdu> _outputQueue = new ArrayList<>();
    private final Map<String, TransportMultiplexier> _multiplexierMap = new HashMap<>();

    private int _outstandingSignalRequests;

    public TransportEndpointSite(final TransportProvider provider, final TransportEndpoint endpoint, final TransportAddress address) {
        assert (provider != null);
        assert (endpoint != null);
        assert (address != null);

        _provider = provider;
        _endpoint = endpoint;
        _address = address;

        _outstandingSignalRequests = 0;
    }

    public TransportEndpointSite(final TransportProvider provider, final TransportEndpoint endpoint) {
        assert (provider != null);
        assert (endpoint != null);

        _provider = provider;
        _endpoint = endpoint;

        _outstandingSignalRequests = 0;
    }

    public TransportAddress getAddress() {
        return _address;
    }

    public void setAddress(final TransportAddress address) {
        _address = address;
    }

    public void registerMultiplexier(final String name, final TransportMultiplexier multiplexier) {
        assert (name != null);
        assert (multiplexier != null);
        assert (_multiplexierMap.get(name) == null);

        _multiplexierMap.put(name, multiplexier);
    }

    public void unregisterMultiplexier(final String name) {
        assert (name != null);
        _multiplexierMap.remove(name);
    }

    public void addOutputPdu(final TransportPdu pdu) {
        synchronized (this) {
            _outputQueue.add(pdu);
        }

        signalOutputProcessRequest();
    }

    private void signalOutputProcessRequest() {
        boolean proceed = false;
        synchronized (this) {
            if (_outstandingSignalRequests == 0) {
                _outstandingSignalRequests++;
                proceed = true;
            }
        }

        if (proceed) {
            _provider.requestSiteOutput(this);
        }
    }

    public void processOutput() {
        TransportPdu pdu;
        final TransportEndpoint endpoint = getEndpoint();

        if (endpoint != null) {
            while ((pdu = getNextOutputPdu()) != null) {
                if (pdu instanceof TransportDataPdu) {
                    final String multiplexierName = ((TransportDataPdu) pdu).getMultiplexier();
                    final TransportMultiplexier multiplexier = getRoutedMultiplexier(multiplexierName);
                    assert (multiplexier != null);
                    multiplexier.onTransportMessage(pdu.getSourceAddress(), pdu.getDestAddress(), multiplexierName, ((TransportDataPdu) pdu).getContent());
                }
            }
        }
    }

    public TransportEndpoint getEndpoint() {
        return _endpoint;
    }

    public TransportPdu getNextOutputPdu() {
        synchronized (this) {
            if (_outputQueue.size() > 0) {
                return _outputQueue.remove(0);
            }
        }

        return null;
    }

    private TransportMultiplexier getRoutedMultiplexier(final String multiplexierName) {
        TransportMultiplexier multiplexier = _multiplexierMap.get(multiplexierName);
        if (multiplexier == null) {
            multiplexier = _endpoint;
        }

        return multiplexier;
    }

    public void ackOutputProcessSignal() {
        synchronized (this) {
            assert (_outstandingSignalRequests == 1);
            _outstandingSignalRequests--;
        }
    }
}
