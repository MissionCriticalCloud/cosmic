package org.apache.cloudstack.ratelimit;

/**
 * Interface to define how an api limit store should work.
 */
public interface LimitStore {

    /**
     * Returns a store entry for the given account. A value of null means that there is no
     * such entry and the calling client must call create to avoid
     * other clients potentially being blocked without any hope of progressing. A non-null
     * entry means that it has not expired and can be used to determine whether the current client should be allowed to
     * proceed with the rate-limited action or not.
     */
    StoreEntry get(Long account);

    /**
     * Creates a new store entry
     *
     * @param account          the user account, key to the store
     * @param timeToLiveInSecs the positive time-to-live in seconds
     * @return a non-null entry
     */
    StoreEntry create(Long account, int timeToLiveInSecs);

    void resetCounters();
}
