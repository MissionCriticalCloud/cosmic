//

//

package com.cloud.utils.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronizationEvent {
    protected final static Logger s_logger = LoggerFactory.getLogger(SynchronizationEvent.class);

    private boolean signalled;

    public SynchronizationEvent() {
        signalled = false;
    }

    public SynchronizationEvent(final boolean signalled) {
        this.signalled = signalled;
    }

    public void setEvent() {
        synchronized (this) {
            signalled = true;
            notifyAll();
        }
    }

    public void resetEvent() {
        synchronized (this) {
            signalled = false;
        }
    }

    public boolean waitEvent() throws InterruptedException {
        synchronized (this) {
            if (signalled) {
                return true;
            }

            while (true) {
                try {
                    wait();
                    assert (signalled);
                    return signalled;
                } catch (final InterruptedException e) {
                    s_logger.debug("unexpected awaken signal in wait()");
                    throw e;
                }
            }
        }
    }

    public boolean waitEvent(final long timeOutMiliseconds) throws InterruptedException {
        synchronized (this) {
            if (signalled) {
                return true;
            }

            try {
                wait(timeOutMiliseconds);
                return signalled;
            } catch (final InterruptedException e) {
                // TODO, we don't honor time out semantics when the waiting thread is interrupted
                s_logger.debug("unexpected awaken signal in wait(...)");
                throw e;
            }
        }
    }

    public boolean isSignalled() {
        synchronized (this) {
            return signalled;
        }
    }
}
