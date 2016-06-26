package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjaxFIFOImageCache {
    private static final Logger s_logger = Logger.getLogger(AjaxFIFOImageCache.class);

    private final List<Integer> fifoQueue;
    private final Map<Integer, byte[]> cache;
    private final int cacheSize;
    private int nextKey = 0;

    public AjaxFIFOImageCache(final int cacheSize) {
        this.cacheSize = cacheSize;
        fifoQueue = new ArrayList<>();
        cache = new HashMap<>();
    }

    public synchronized void clear() {
        fifoQueue.clear();
        cache.clear();
    }

    public synchronized int putImage(final byte[] image) {
        while (cache.size() >= cacheSize) {
            final Integer keyToRemove = fifoQueue.remove(0);
            cache.remove(keyToRemove);

            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Remove image from cache, key: " + keyToRemove);
            }
        }

        final int key = getNextKey();

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Add image to cache, key: " + key);
        }

        cache.put(key, image);
        fifoQueue.add(key);
        return key;
    }

    public synchronized int getNextKey() {
        return ++nextKey;
    }

    public synchronized byte[] getImage(int key) {
        if (key == 0) {
            key = nextKey;
        }
        if (cache.containsKey(key)) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Retrieve image from cache, key: " + key);
            }

            return cache.get(key);
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Image is no long in cache, key: " + key);
        }
        return null;
    }
}
