package com.cloud.resource;

import com.cloud.exception.DiscoveryException;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.utils.component.Adapter;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Discoverer encapsulates interfaces that will discover resources.
 */
public interface Discoverer extends Adapter {
    /**
     * Given an accessible ip address, find out what it is.
     *
     * @param url
     * @param username
     * @param password
     * @return ServerResource
     */
    Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI uri, String username, String password, List<String> hostTags)
            throws DiscoveryException;

    void postDiscovery(List<HostVO> hosts, long msId) throws DiscoveryException;

    boolean matchHypervisor(String hypervisor);

    Hypervisor.HypervisorType getHypervisorType();

    public void putParam(Map<String, String> params);

    ServerResource reloadResource(HostVO host);
}
