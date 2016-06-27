package org.apache.cloudstack.framework.client;

import com.cloud.utils.concurrency.NamedThreadFactory;
import org.apache.cloudstack.framework.serializer.MessageSerializer;
import org.apache.cloudstack.framework.transport.TransportEndpoint;
import org.apache.cloudstack.framework.transport.TransportEndpointSite;
import org.apache.cloudstack.framework.transport.TransportProvider;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTransportProvider implements TransportProvider {
    public static final int DEFAULT_WORKER_POOL_SIZE = 5;
    final static Logger s_logger = LoggerFactory.getLogger(ClientTransportProvider.class);
    private final Map<Integer, ClientTransportEndpointSite> _endpointSites = new HashMap<>();
    private final Map<String, ClientTransportEndpointSite> _attachedMap = new HashMap<>();

    private MessageSerializer _messageSerializer;

    private ClientTransportConnection _connection;
    private String _serverAddress;
    private int _serverPort;

    private int _poolSize = DEFAULT_WORKER_POOL_SIZE;
    private ExecutorService _executor;

    private int _nextProviderKey = 1;

    public ClientTransportProvider() {
    }

    public ClientTransportProvider setPoolSize(final int poolSize) {
        _poolSize = poolSize;
        return this;
    }

    public void initialize(final String serverAddress, final int serverPort) {
        _serverAddress = serverAddress;
        _serverPort = serverPort;

        _executor = Executors.newFixedThreadPool(_poolSize, new NamedThreadFactory("Transport-Worker"));
        _connection = new ClientTransportConnection(this);

        _executor.execute(new ManagedContextRunnable() {
            @Override
            protected void runInContext() {
                try {
                    _connection.connect(_serverAddress, _serverPort);
                } catch (final Throwable e) {
                    s_logger.info("[ignored]"
                            + "error during ipc client initialization: " + e.getLocalizedMessage());
                }
            }
        });
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

        ClientTransportEndpointSite endpointSite;
        synchronized (this) {
            endpointSite = getEndpointSite(endpoint);
            if (endpointSite != null) {
                // already attached
                return endpointSite;
            }

            endpointSite = new ClientTransportEndpointSite(this, endpoint, predefinedAddress, getNextProviderKey());
            _endpointSites.put(endpointSite.getProviderKey(), endpointSite);
        }

        return endpointSite;
    }

    @Override
    public boolean detach(final TransportEndpoint endpoint) {
        // TODO Auto-generated method stub

        return false;
    }

    @Override
    public void requestSiteOutput(final TransportEndpointSite site) {
        // ???
    }

    @Override
    public void sendMessage(final String soureEndpointAddress, final String targetEndpointAddress, final String multiplexier, final String message) {
        // TODO
    }

    private ClientTransportEndpointSite getEndpointSite(final TransportEndpoint endpoint) {
        synchronized (this) {
            for (final ClientTransportEndpointSite endpointSite : _endpointSites.values()) {
                if (endpointSite.getEndpoint() == endpoint) {
                    return endpointSite;
                }
            }
        }

        return null;
    }

    public int getNextProviderKey() {
        synchronized (this) {
            return _nextProviderKey++;
        }
    }
}
