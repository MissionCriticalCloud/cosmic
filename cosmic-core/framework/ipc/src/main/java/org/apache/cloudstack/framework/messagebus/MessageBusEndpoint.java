package org.apache.cloudstack.framework.messagebus;

public class MessageBusEndpoint {
    private MessageBus _eventBus;
    private String _sender;
    private PublishScope _scope;

    public MessageBusEndpoint(final MessageBus eventBus, final String sender, final PublishScope scope) {
        _eventBus = eventBus;
        _sender = sender;
        _scope = scope;
    }

    public MessageBusEndpoint setEventBus(final MessageBus eventBus) {
        _eventBus = eventBus;
        return this;
    }

    public PublishScope getScope() {
        return _scope;
    }

    public MessageBusEndpoint setScope(final PublishScope scope) {
        _scope = scope;
        return this;
    }

    public String getSender() {
        return _sender;
    }

    public MessageBusEndpoint setSender(final String sender) {
        _sender = sender;
        return this;
    }

    public void Publish(final String subject, final Object args) {
        assert (_eventBus != null);
        _eventBus.publish(_sender, subject, _scope, args);
    }
}
