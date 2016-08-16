package com.cloud.managementserver;

import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.utils.net.AddressUtils;
import org.apache.cloudstack.config.ApiServiceConfiguration;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagementServerService {
    private static final Logger logger = LoggerFactory.getLogger(ManagementServerService.class);

    private final ManagementServerHostDao managementServerHostDao;

    @Autowired
    public ManagementServerService(final ManagementServerHostDao managementServerHostDao) {
        this.managementServerHostDao = managementServerHostDao;
    }

    public Stream<String> discoverManagementServerIps() {
        return discoverManagementServers().stream().map(getServerPublicIp()).distinct();
    }

    protected Function<ManagementServerHostVO, String> getServerPublicIp() {
        return managementServerHostVO -> {
            final String serviceIP = managementServerHostVO.getServiceIP();
            if (isAddressUsableForAgents(managementServerHostVO, serviceIP)) {
                return serviceIP;
            } else {
                logger.warn("Falling back to IP address present in global settings");
                return ApiServiceConfiguration.ManagementHostIPAdr.value();
            }
        };
    }

    private boolean isAddressUsableForAgents(final ManagementServerHostVO managementServerHostVO, final String serviceIP) {
        try {
            if (AddressUtils.isPublicAddress(serviceIP)) {
                return true;
            } else {
                logger.warn("IP address configured for management server {} is local and cannot be used for connectivity with agents", managementServerHostVO);
            }
        } catch (final UnknownHostException e) {
            logUnresolvableHostname(managementServerHostVO, e);
        }
        return false;
    }

    private void logUnresolvableHostname(final ManagementServerHostVO managementServerHostVO, final UnknownHostException e) {
        logger.warn("Not able to resolve management server IP " + managementServerHostVO.getServiceIP() + " for entry: " + managementServerHostVO, e);
    }

    private Collection<ManagementServerHostVO> discoverManagementServers() {
        return managementServerHostDao.listAll();
    }
}
