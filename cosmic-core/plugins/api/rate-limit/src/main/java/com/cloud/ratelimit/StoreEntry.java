package com.cloud.ratelimit;

/**
 * Interface for each entry in LimitStore.
 */
public interface StoreEntry {

    int getCounter();

    int incrementAndGet();

    boolean isExpired();

    long getExpireDuration(); /* seconds to reset counter */
}
