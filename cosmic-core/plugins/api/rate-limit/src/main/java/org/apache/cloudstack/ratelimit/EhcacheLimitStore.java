package org.apache.cloudstack.ratelimit;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

/**
 * A Limit store implementation using Ehcache.
 */
public class EhcacheLimitStore implements LimitStore {

    private BlockingCache cache;

    public void setCache(final Ehcache cache) {
        final BlockingCache ref;

        if (!(cache instanceof BlockingCache)) {
            ref = new BlockingCache(cache);
            cache.getCacheManager().replaceCacheWithDecoratedCache(cache, new BlockingCache(cache));
        } else {
            ref = (BlockingCache) cache;
        }

        this.cache = ref;
    }

    @Override
    public StoreEntry get(final Long key) {

        Element entry = null;

        try {

            /* This may block. */
            entry = cache.get(key);
        } catch (final LockTimeoutException e) {
            throw new RuntimeException();
        } catch (final RuntimeException e) {

            /* Release the lock that may have been acquired. */
            cache.put(new Element(key, null));
        }

        StoreEntry result = null;

        if (entry != null) {

            /*
             * We don't need to check isExpired() on the result, since ehcache takes care of expiring entries for us.
             * c.f. the get(Key) implementation in this class.
             */
            result = (StoreEntry) entry.getObjectValue();
        }

        return result;
    }

    @Override
    public StoreEntry create(final Long key, final int timeToLive) {
        final StoreEntryImpl result = new StoreEntryImpl(timeToLive);
        final Element element = new Element(key, result);
        element.setTimeToLive(timeToLive);
        cache.put(element);
        return result;
    }

    @Override
    public void resetCounters() {
        cache.removeAll();
    }
}
