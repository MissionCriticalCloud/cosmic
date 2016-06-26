package org.apache.cloudstack.framework.messagebus;

import org.apache.cloudstack.framework.serializer.MessageSerializer;

public interface MessageBus {
    MessageSerializer getMessageSerializer();

    void setMessageSerializer(MessageSerializer messageSerializer);

    void subscribe(String subject, MessageSubscriber subscriber);

    void unsubscribe(String subject, MessageSubscriber subscriber);

    void clearAll();

    void prune();

    void publish(String senderAddress, String subject, PublishScope scope, Object args);
}
