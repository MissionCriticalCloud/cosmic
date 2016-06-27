package org.apache.cloudstack.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of limit store entry.
 */
public class StoreEntryImpl implements StoreEntry {

    private final long expiry;

    private final AtomicInteger counter;

    StoreEntryImpl(final int timeToLive) {
        this.expiry = System.currentTimeMillis() + timeToLive * 1000;
        this.counter = new AtomicInteger(0);
    }

    @Override
    public int getCounter() {
        return this.counter.get();
    }

    @Override
    public int incrementAndGet() {
        return this.counter.incrementAndGet();
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() > expiry;
    }

    @Override
    public long getExpireDuration() {
        if (isExpired()) {
            return 0; // already expired
        } else {
            return expiry - System.currentTimeMillis();
        }
    }
}
