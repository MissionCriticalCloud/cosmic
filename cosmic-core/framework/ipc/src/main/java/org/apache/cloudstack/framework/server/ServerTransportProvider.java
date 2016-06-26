package org.apache.cloudstack.framework.server;

import com.cloud.utils.concurrency.NamedThreadFactory;
import org.apache.cloudstack.framework.serializer.MessageSerializer;
import org.apache.cloudstack.framework.transport.TransportAddress;
import org.apache.cloudstack.framework.transport.TransportDataPdu;
import org.apache.cloudstack.framework.transport.TransportEndpoint;
import org.apache.cloudstack.framework.transport.TransportEndpointSite;
import org.apache.cloudstack.framework.transport.TransportPdu;
import org.apache.cloudstack.framework.transport.TransportProvider;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTransportProvider implements TransportProvider {
    public static final int DEFAULT_WORKER_POOL_SIZE = 5;
    private static final Logger s_logger = LoggerFactory.getLogger(ServerTransportProvider.class);
    private final SecureRandom randomGenerator;
    private String _nodeId;
    private final Map<String, TransportEndpointSite> _endpointMap = new HashMap<>();
    private int _poolSize = DEFAULT_WORKER_POOL_SIZE;
    private ExecutorService _executor;
    private int _nextEndpointId;

    private MessageSerializer _messageSerializer;

    public ServerTransportProvider() {
        randomGenerator = new SecureRandom();
        _nextEndpointId = randomGenerator.nextInt();
    }

    public String getNodeId() {
        return _nodeId;
    }

    public ServerTransportProvider setNodeId(final String nodeId) {
        _nodeId = nodeId;
        return this;
    }

    public int getWorkerPoolSize() {
        return _poolSize;
    }

    public ServerTransportProvider setWorkerPoolSize(final int poolSize) {
        assert (poolSize > 0);

        _poolSize = poolSize;
        return this;
    }

    public void initialize() {
        _executor = Executors.newFixedThreadPool(_poolSize, new NamedThreadFactory("Transport-Worker"));
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
    public TransportEndpointSite attach(final TransportEndpoint endpoint, final String predefinedAddress) {

        final TransportAddress transportAddress;
        final String endpointId;
        if (predefinedAddress != null && !predefinedAddress.isEmpty()) {
            endpointId = predefinedAddress;
            transportAddress = new TransportAddress(_nodeId, TransportAddress.LOCAL_SERVICE_CONNECTION, endpointId, 0);
        } else {
            endpointId = String.valueOf(getNextEndpointId());
            transportAddress = new TransportAddress(_nodeId, TransportAddress.LOCAL_SERVICE_CONNECTION, endpointId);
        }

        TransportEndpointSite endpointSite;
        synchronized (this) {
            endpointSite = _endpointMap.get(endpointId);
            if (endpointSite != null) {
                // already attached
                return endpointSite;
            }
            endpointSite = new TransportEndpointSite(this, endpoint, transportAddress);
            _endpointMap.put(endpointId, endpointSite);
        }

        endpoint.onAttachConfirm(true, transportAddress.toString());
        return endpointSite;
    }

    @Override
    public boolean detach(final TransportEndpoint endpoint) {
        synchronized (this) {
            for (final Map.Entry<String, TransportEndpointSite> entry : _endpointMap.entrySet()) {
                if (entry.getValue().getEndpoint() == endpoint) {
                    _endpointMap.remove(entry.getKey());
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void requestSiteOutput(final TransportEndpointSite site) {
        _executor.execute(new ManagedContextRunnable() {
            @Override
            protected void runInContext() {
                try {
                    site.processOutput();
                    site.ackOutputProcessSignal();
                } catch (final Throwable e) {
                    s_logger.error("Unhandled exception", e);
                }
            }
        });
    }

    @Override
    public void sendMessage(final String sourceEndpointAddress, final String targetEndpointAddress, final String multiplexier, final String message) {

        final TransportDataPdu pdu = new TransportDataPdu();
        pdu.setSourceAddress(sourceEndpointAddress);
        pdu.setDestAddress(targetEndpointAddress);
        pdu.setMultiplexier(multiplexier);
        pdu.setContent(message);

        dispatchPdu(pdu);
    }

    private void dispatchPdu(final TransportPdu pdu) {

        final TransportAddress transportAddress = TransportAddress.fromAddressString(pdu.getDestAddress());

        if (isLocalAddress(transportAddress)) {
            TransportEndpointSite endpointSite = null;
            synchronized (this) {
                endpointSite = _endpointMap.get(transportAddress.getEndpointId());
            }

            if (endpointSite != null) {
                endpointSite.addOutputPdu(pdu);
            }
        } else {
            // do cross-node forwarding
            // ???
        }
    }

    private boolean isLocalAddress(final TransportAddress address) {
        if (address.getNodeId().equals(_nodeId) || address.getNodeId().equals(TransportAddress.LOCAL_SERVICE_NODE)) {
            return true;
        }

        return false;
    }

    private synchronized int getNextEndpointId() {
        return _nextEndpointId++;
    }
}
