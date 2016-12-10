package com.cloud.framework.messagebus;

import com.cloud.framework.serializer.MessageSerializer;

public interface MessageBus {
    MessageSerializer getMessageSerializer();

    void setMessageSerializer(MessageSerializer messageSerializer);

    void subscribe(String subject, MessageSubscriber subscriber);

    void unsubscribe(String subject, MessageSubscriber subscriber);

    void clearAll();

    void publish(String senderAddress, String subject, PublishScope scope, Object args);
}
