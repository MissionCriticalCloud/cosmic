package org.apache.cloudstack.framework.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDetector implements MessageSubscriber {
    private static final Logger s_logger = LoggerFactory.getLogger(MessageDetector.class);

    private MessageBus _messageBus;
    private String[] _subjects;

    public MessageDetector() {
        _messageBus = null;
        _subjects = null;
    }

    public void waitAny(long timeoutInMiliseconds) {
        if (timeoutInMiliseconds < 100) {
            s_logger.warn("waitAny is passed with a too short time-out interval. " + timeoutInMiliseconds + "ms");
            timeoutInMiliseconds = 100;
        }

        synchronized (this) {
            try {
                wait(timeoutInMiliseconds);
            } catch (final InterruptedException e) {
                s_logger.debug("[ignored] interupted while waiting on any message.");
            }
        }
    }

    public void open(final MessageBus messageBus, final String[] subjects) {
        assert (messageBus != null);
        assert (subjects != null);

        _messageBus = messageBus;
        _subjects = subjects;

        if (subjects != null) {
            for (final String subject : subjects) {
                messageBus.subscribe(subject, this);
            }
        }
    }

    public void close() {
        if (_subjects != null) {
            assert (_messageBus != null);

            for (final String subject : _subjects) {
                _messageBus.unsubscribe(subject, this);
            }
        }
    }

    @Override
    public void onPublishMessage(final String senderAddress, final String subject, final Object args) {
        if (subjectMatched(subject)) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private boolean subjectMatched(final String subject) {
        if (_subjects != null) {
            for (final String sub : _subjects) {
                if (sub.equals(subject)) {
                    return true;
                }
            }
        }
        return false;
    }
}
