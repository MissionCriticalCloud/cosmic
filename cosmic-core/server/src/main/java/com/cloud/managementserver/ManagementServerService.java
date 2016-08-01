package com.cloud.managementserver;

import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.dao.ManagementServerHostDao;

import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagementServerService {
    private final ManagementServerHostDao managementServerHostDao;

    @Autowired
    public ManagementServerService(final ManagementServerHostDao managementServerHostDao) {
        this.managementServerHostDao = managementServerHostDao;
    }

    public Stream<String> discoverManagementServerIps() {
        return discoverManagementServers().stream()
                                          .map(ManagementServerHostVO::getServiceIP);
    }

    private Collection<ManagementServerHostVO> discoverManagementServers() {
        return managementServerHostDao.listAll();
    }
}
