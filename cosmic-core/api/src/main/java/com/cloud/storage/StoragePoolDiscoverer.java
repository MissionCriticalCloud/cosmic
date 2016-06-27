package com.cloud.storage;

import com.cloud.exception.DiscoveryException;
import com.cloud.utils.component.Adapter;

import java.net.URI;
import java.util.Map;

/**
 * Discoverer to find new Storage Pools.
 */
public interface StoragePoolDiscoverer extends Adapter {

    Map<? extends StoragePool, Map<String, String>> find(long dcId, Long podId, URI uri, Map<String, String> details) throws DiscoveryException;

    Map<? extends StoragePool, Map<String, String>> find(long dcId, Long podId, URI uri, Map<String, String> details, String username, String password)
            throws DiscoveryException;
}
