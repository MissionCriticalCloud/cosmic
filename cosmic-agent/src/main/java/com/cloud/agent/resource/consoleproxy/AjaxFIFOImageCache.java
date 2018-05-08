package com.cloud.agent.resource.consoleproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AjaxFIFOImageCache {
    private static final Logger s_logger = LoggerFactory.getLogger(AjaxFIFOImageCache.class);

    private final List<Integer> fifoQueue;
    private final Map<Integer, byte[]> cache;
    private final int cacheSize;
    private int nextKey = 0;

    public AjaxFIFOImageCache(final int cacheSize) {
        this.cacheSize = cacheSize;
        this.fifoQueue = new ArrayList<>();
        this.cache = new HashMap<>();
    }

    public synchronized void clear() {
        this.fifoQueue.clear();
        this.cache.clear();
    }

    public synchronized int putImage(final byte[] image) {
        while (this.cache.size() >= this.cacheSize) {
            final Integer keyToRemove = this.fifoQueue.remove(0);
            this.cache.remove(keyToRemove);

            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Remove image from cache, key: " + keyToRemove);
            }
        }

        final int key = getNextKey();

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Add image to cache, key: " + key);
        }

        this.cache.put(key, image);
        this.fifoQueue.add(key);
        return key;
    }

    public synchronized int getNextKey() {
        return ++this.nextKey;
    }

    public synchronized byte[] getImage(int key) {
        if (key == 0) {
            key = this.nextKey;
        }
        if (this.cache.containsKey(key)) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Retrieve image from cache, key: " + key);
            }

            return this.cache.get(key);
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Image is no long in cache, key: " + key);
        }
        return null;
    }
}
