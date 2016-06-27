package org.apache.cloudstack.framework.messagebus;

public interface MessageSubscriber {
    void onPublishMessage(String senderAddress, String subject, Object args);
}
