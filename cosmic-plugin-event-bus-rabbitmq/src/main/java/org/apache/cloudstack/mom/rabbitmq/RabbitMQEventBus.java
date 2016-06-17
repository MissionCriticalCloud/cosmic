package org.apache.cloudstack.mom.rabbitmq;

import java.io.IOException;
import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.naming.ConfigurationException;

import com.cloud.utils.Ternary;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.cloudstack.framework.events.Event;
import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;
import org.apache.cloudstack.framework.events.EventSubscriber;
import org.apache.cloudstack.framework.events.EventTopic;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQEventBus extends ManagerBase implements EventBus {

    private static final Logger s_logger = LoggerFactory.getLogger(RabbitMQEventBus.class);
    // details of AMQP server
    private static String amqpHost;
    private static Integer port;
    private static String username;
    private static String password;
    private static final String secureProtocol = "TLSv1";
    private static String virtualHost;
    private static String useSsl;
    // AMQP exchange name where all CloudStack events will be published
    private static String amqpExchangeName;
    private static Integer retryInterval;
    // hashmap to book keep the registered subscribers
    private static ConcurrentHashMap<String, Ternary<String, Channel, EventSubscriber>> s_subscribers;
    // connection to AMQP server,
    private static Connection s_connection = null;
    // AMQP server should consider messages acknowledged once delivered if _autoAck is true
    private static final boolean s_autoAck = true;
    private static DisconnectHandler disconnectHandler;
    private static BlockedConnectionHandler blockedConnectionHandler;
    private ExecutorService executorService;

    public static void setServer(final String amqpHost) {
        RabbitMQEventBus.amqpHost = amqpHost;
    }

    public static void setUsername(final String username) {
        RabbitMQEventBus.username = username;
    }

    public static void setPassword(final String password) {
        RabbitMQEventBus.password = password;
    }

    public static void setPort(final Integer port) {
        RabbitMQEventBus.port = port;
    }

    // publish event on to the exchange created on AMQP server
    @Override
    public void publish(final Event event) throws EventBusException {

        final String routingKey = createRoutingKey(event);
        final String eventDescription = event.getDescription();

        try {
            final Connection connection = getConnection();
            final Channel channel = createChannel(connection);
            createExchange(channel, amqpExchangeName);
            publishEventToExchange(channel, amqpExchangeName, routingKey, eventDescription);
            channel.close();
        } catch (final AlreadyClosedException e) {
            closeConnection();
            throw new EventBusException("Failed to publish event to message broker as connection to AMQP broker in lost");
        } catch (final Exception e) {
            throw new EventBusException("Failed to publish event to message broker due to " + e.getMessage());
        }
    }

    /**
     * Call to subscribe to interested set of events
     *
     * @param topic      defines category and type of the events being subscribed to
     * @param subscriber subscriber that intends to receive event notification
     * @return UUID that represents the subscription with event bus
     * @throws EventBusException
     */
    @Override
    public UUID subscribe(final EventTopic topic, final EventSubscriber subscriber) throws EventBusException {

        if (subscriber == null || topic == null) {
            throw new EventBusException("Invalid EventSubscriber/EventTopic object passed.");
        }

        // create a UUID, that will be used for managing subscriptions and also used as queue name
        // for on the queue used for the subscriber on the AMQP broker
        final UUID queueId = UUID.randomUUID();
        final String queueName = queueId.toString();

        try {
            final String bindingKey = createBindingKey(topic);

            // store the subscriber details before creating channel
            s_subscribers.put(queueName, new Ternary<>(bindingKey, null, subscriber));

            // create a channel dedicated for this subscription
            final Connection connection = getConnection();
            final Channel channel = createChannel(connection);

            // create a queue and bind it to the exchange with binding key formed from event topic
            createExchange(channel, amqpExchangeName);
            channel.queueDeclare(queueName, false, false, false, null);
            channel.queueBind(queueName, amqpExchangeName, bindingKey);

            // register a callback handler to receive the events that a subscriber subscribed to
            channel.basicConsume(queueName, s_autoAck, queueName, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(final String queueName, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) throws IOException {
                    RabbitMQEventBus.this.handleDelivery(queueName, envelope, body);
                }
            });

            // update the channel details for the subscription
            final Ternary<String, Channel, EventSubscriber> queueDetails = s_subscribers.get(queueName);
            queueDetails.second(channel);
            s_subscribers.put(queueName, queueDetails);

        } catch (final AlreadyClosedException | ConnectException | NoSuchAlgorithmException | KeyManagementException | TimeoutException e) {
            s_logger.warn("Connection to AMQP service is lost. Subscription:" + queueName + " will be active after reconnection", e);
        } catch (final IOException e) {
            throw new EventBusException("Failed to subscribe to event due to " + e.getMessage(), e);
        }

        return queueId;
    }

    private void handleDelivery(final String queueName, final Envelope envelope, final byte[] body) {
        final Ternary<String, Channel, EventSubscriber> queueDetails = s_subscribers.get(queueName);
        if (queueDetails != null) {
            final EventSubscriber subscriber = queueDetails.third();
            final String routingKey = envelope.getRoutingKey();
            final String eventSource = getEventSourceFromRoutingKey(routingKey);
            final String eventCategory = getEventCategoryFromRoutingKey(routingKey);
            final String eventType = getEventTypeFromRoutingKey(routingKey);
            final String resourceType = getResourceTypeFromRoutingKey(routingKey);
            final String resourceUUID = getResourceUUIDFromRoutingKey(routingKey);
            final Event event = new Event(eventSource, eventCategory, eventType, resourceType, resourceUUID);
            event.setDescription(new String(body));

            // deliver the event to call back object provided by subscriber
            subscriber.onEvent(event);
        }
    }

    @Override
    public void unsubscribe(final UUID subscriberId, final EventSubscriber subscriber) throws EventBusException {
        try {
            final String classname = subscriber.getClass().getName();
            final String queueName = UUID.nameUUIDFromBytes(classname.getBytes()).toString();
            final Ternary<String, Channel, EventSubscriber> queueDetails = s_subscribers.get(queueName);
            final Channel channel = queueDetails.second();
            channel.basicCancel(queueName);
            s_subscribers.remove(queueName, queueDetails);
        } catch (final IOException e) {
            throw new EventBusException("Failed to unsubscribe from event bus due to " + e.getMessage(), e);
        }
    }

    /**
     * creates a binding key from the event topic that subscriber specified
     * binding key will be used to bind the queue created for subscriber to exchange on AMQP server
     */
    private String createBindingKey(final EventTopic topic) {
        final String eventSource = makeKeyValue(topic.getEventSource());
        final String eventCategory = makeKeyValue(topic.getEventCategory());
        final String eventType = makeKeyValue(topic.getEventType());
        final String resourceType = makeKeyValue(topic.getResourceType());
        final String resourceUuid = makeKeyValue(topic.getResourceUUID());

        return buildKey(eventSource, eventCategory, eventType, resourceType, resourceUuid);
    }

    private String makeKeyValue(final String value) {
        return replaceNullWithWildcard(value).replace(".", "-");
    }

    private String buildKey(final String eventSource, final String eventCategory, final String eventType, final String resourceType, final String resourceUuid) {
        return eventSource + "." + eventCategory + "." + eventType + "." + resourceType + "." + resourceUuid;
    }

    private synchronized Connection getConnection() throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        if (s_connection == null) {
            try {
                return createConnection();
            } catch (KeyManagementException | NoSuchAlgorithmException | IOException | TimeoutException e) {
                s_logger.warn("Failed to create a connection to AMQP server due to " + e.getMessage());
                throw e;
            }
        } else {
            return s_connection;
        }
    }

    private Channel createChannel(final Connection connection) throws IOException {
        try {
            return connection.createChannel();
        } catch (final IOException e) {
            s_logger.warn("Failed to create a channel due to " + e.getMessage(), e);
            throw e;
        }
    }

    private void createExchange(final Channel channel, final String exchangeName) throws IOException {
        try {
            channel.exchangeDeclare(exchangeName, "topic", true);
        } catch (final IOException e) {
            s_logger.warn("Failed to create exchange" + e + " on RabbitMQ server");
            throw e;
        }
    }

    private String getEventSourceFromRoutingKey(final String routingKey) {
        final String[] keyParts = routingKey.split("\\.");
        return keyParts[0];
    }

    private String getEventCategoryFromRoutingKey(final String routingKey) {
        final String[] keyParts = routingKey.split("\\.");
        return keyParts[1];
    }

    private String getEventTypeFromRoutingKey(final String routingKey) {
        final String[] keyParts = routingKey.split("\\.");
        return keyParts[2];
    }

    private String getResourceTypeFromRoutingKey(final String routingKey) {
        final String[] keyParts = routingKey.split("\\.");
        return keyParts[3];
    }

    private String getResourceUUIDFromRoutingKey(final String routingKey) {
        final String[] keyParts = routingKey.split("\\.");
        return keyParts[4];
    }

    private String replaceNullWithWildcard(final String key) {
        if (key == null || key.isEmpty()) {
            return "*";
        } else {
            return key;
        }
    }

    private synchronized Connection createConnection() throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setHost(amqpHost);
        factory.setPort(port);

        if (virtualHost != null && !virtualHost.isEmpty()) {
            factory.setVirtualHost(virtualHost);
        } else {
            factory.setVirtualHost("/");
        }

        if (useSsl != null && !useSsl.isEmpty() && useSsl.equalsIgnoreCase("true")) {
            factory.useSslProtocol(secureProtocol);
        }
        final Connection connection = factory.newConnection();
        connection.addShutdownListener(disconnectHandler);
        connection.addBlockedListener(blockedConnectionHandler);
        s_connection = connection;
        return s_connection;
    }

    /**
     * creates a routing key from the event details.
     * created routing key will be used while publishing the message to exchange on AMQP server
     */
    private String createRoutingKey(final Event event) {
        final String eventSource = makeKeyValue(event.getEventSource());
        final String eventCategory = makeKeyValue(event.getEventCategory());
        final String eventType = makeKeyValue(event.getEventType());
        final String resourceType = makeKeyValue(event.getResourceType());
        final String resourceUuid = makeKeyValue(event.getResourceUUID());

        return buildKey(eventSource, eventCategory, eventType, resourceType, resourceUuid);
    }

    private void publishEventToExchange(final Channel channel, final String exchangeName, final String routingKey, final String eventDescription) throws IOException {
        final byte[] messageBodyBytes = eventDescription.getBytes();
        try {
            channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
        } catch (final IOException e) {
            s_logger.warn("Failed to publish event " + routingKey + " on exchange " + exchangeName + "  of message broker due to " + e.getMessage(), e);
            throw e;
        }
    }

    private synchronized void closeConnection() {
        try {
            if (s_connection != null) {
                s_connection.close();
            }
        } catch (final Exception e) {
            s_logger.warn("Failed to close connection to AMQP server due to " + e.getMessage());
        }
        s_connection = null;
    }

    private synchronized void abortConnection() {
        if (s_connection == null)
            return;

        try {
            s_connection.abort();
        } catch (final Exception e) {
            s_logger.warn("Failed to abort connection due to " + e.getMessage());
        }
        s_connection = null;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        try {
            if (amqpHost == null || amqpHost.isEmpty()) {
                throw new ConfigurationException("Unable to get the AMQP server details");
            }

            if (username == null || username.isEmpty()) {
                throw new ConfigurationException("Unable to get the username details");
            }

            if (password == null || password.isEmpty()) {
                throw new ConfigurationException("Unable to get the password details");
            }

            if (amqpExchangeName == null || amqpExchangeName.isEmpty()) {
                throw new ConfigurationException("Unable to get the _exchange details on the AMQP server");
            }

            if (port == null) {
                throw new ConfigurationException("Unable to get the port details of AMQP server");
            }

            if (useSsl != null && !useSsl.isEmpty()) {
                if (!useSsl.equalsIgnoreCase("true") && !useSsl.equalsIgnoreCase("false")) {
                    throw new ConfigurationException("Invalid configuration parameter for 'ssl'.");
                }
            }

            if (retryInterval == null) {
                retryInterval = 10000;// default to 10s to try out reconnect
            }

        } catch (final NumberFormatException e) {
            throw new ConfigurationException("Invalid port number/retry interval");
        }

        s_subscribers = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool();
        disconnectHandler = new DisconnectHandler();
        blockedConnectionHandler = new BlockedConnectionHandler();

        return true;
    }

    @Override
    public boolean start() {
        final ReconnectionTask reconnect = new ReconnectionTask(); // initiate connection to AMQP server
        executorService.submit(reconnect);
        return true;
    }

    @Override
    public synchronized boolean stop() {
        if (s_connection.isOpen()) {
            for (final String subscriberId : s_subscribers.keySet()) {
                final Ternary<String, Channel, EventSubscriber> subscriberDetails = s_subscribers.get(subscriberId);
                final Channel channel = subscriberDetails.second();
                try {
                    channel.queueDelete(subscriberId);
                    channel.abort();
                } catch (final IOException ioe) {
                    s_logger.warn("Failed to delete queue: " + subscriberId + " on AMQP server due to " + ioe.getMessage());
                }
            }
        }

        closeConnection();
        return true;
    }

    //logic to deal with blocked connection. connections are blocked for example when the rabbitmq server is out of space. https://www.rabbitmq.com/connection-blocked.html
    private class BlockedConnectionHandler implements BlockedListener {

        @Override
        public void handleBlocked(final String reason) throws IOException {
            s_logger.error("rabbitmq connection is blocked with reason: " + reason);
            closeConnection();
            throw new CloudRuntimeException("unblocking the parent thread as publishing to rabbitmq server is blocked with reason: " + reason);
        }

        @Override
        public void handleUnblocked() throws IOException {
            s_logger.info("rabbitmq connection in unblocked");
        }
    }

    // logic to deal with loss of connection to AMQP server
    private class DisconnectHandler implements ShutdownListener {

        @Override
        public void shutdownCompleted(final ShutdownSignalException shutdownSignalException) {
            if (!shutdownSignalException.isInitiatedByApplication()) {

                for (final String subscriberId : s_subscribers.keySet()) {
                    final Ternary<String, Channel, EventSubscriber> subscriberDetails = s_subscribers.get(subscriberId);
                    subscriberDetails.second(null);
                    s_subscribers.put(subscriberId, subscriberDetails);
                }

                abortConnection(); // disconnected to AMQP server, so abort the connection and channels
                s_logger.warn("Connection has been shutdown by AMQP server. Attempting to reconnect.");

                // initiate re-connect process
                final ReconnectionTask reconnect = new ReconnectionTask();
                executorService.submit(reconnect);
            }
        }
    }

    // retry logic to connect back to AMQP server after loss of connection
    private class ReconnectionTask extends ManagedContextRunnable {

        boolean connected = false;
        Connection connection = null;

        @Override
        protected void runInContext() {

            while (!connected) {
                try {
                    Thread.sleep(retryInterval);
                } catch (final InterruptedException ie) {
                    // ignore timer interrupts
                }

                try {
                    try {
                        connection = createConnection();
                        connected = true;
                    } catch (final IOException | TimeoutException | NoSuchAlgorithmException | KeyManagementException e) {
                        s_logger.warn("Can't establish connection to AMQP server yet, so continue", e);
                        continue;
                    }

                    // prepare consumer on AMQP server for each of subscriber
                    for (final String subscriberId : s_subscribers.keySet()) {
                        final Ternary<String, Channel, EventSubscriber> subscriberDetails = s_subscribers.get(subscriberId);
                        final String bindingKey = subscriberDetails.first();

                        /** create a queue with subscriber ID as queue name and bind it to the exchange
                         *  with binding key formed from event topic
                         */
                        final Channel channel = createChannel(connection);
                        createExchange(channel, amqpExchangeName);
                        channel.queueDeclare(subscriberId, false, false, false, null);
                        channel.queueBind(subscriberId, amqpExchangeName, bindingKey);

                        // register a callback handler to receive the events that a subscriber subscribed to
                        channel.basicConsume(subscriberId, s_autoAck, subscriberId, new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(final String queueName, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) throws IOException {
                                RabbitMQEventBus.this.handleDelivery(queueName, envelope, body);
                            }
                        });

                        // update the channel details for the subscription
                        subscriberDetails.second(channel);
                        s_subscribers.put(subscriberId, subscriberDetails);
                    }
                } catch (final IOException e) {
                    s_logger.warn("Failed to recreate queues and binding for the subscribers due to " + e.getMessage(), e);
                }
            }
        }
    }
}