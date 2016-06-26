package org.apache.cloudstack.engine.service.api;

import com.cloud.utils.component.PluggableService;

import java.net.URI;
import java.util.List;

public interface DirectoryService {
    void registerService(String serviceName, URI endpoint);

    void unregisterService(String serviceName, URI endpoint);

    List<URI> getEndPoints(String serviceName);

    URI getLoadBalancedEndPoint(String serviceName);

    List<PluggableService> listServices();
}
