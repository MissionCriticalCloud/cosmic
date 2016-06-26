package com.cloud.network.dao;

import com.cloud.network.MonitoringService;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "monitoring_services")
public class MonitoringServiceVO implements MonitoringService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "service")
    String service;
    @Column(name = "process_name", updatable = false)
    String processName;
    @Column(name = "service_name", updatable = false)
    String serviceName;
    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();
    @Column(name = "service_path", updatable = false)
    private String servicePath;
    @Column(name = "pidFile", updatable = false)
    private String servicePidFile;
    @Column(name = "isDefault")
    private boolean defaultService;

    public MonitoringServiceVO(final String service, final String processName, final String serviceName, final String servicePath, final String servicePidFile, final boolean
            defaultService) {
        this.service = service;
        this.processName = processName;
        this.serviceName = serviceName;
        this.servicePath = servicePath;
        this.servicePidFile = servicePidFile;
        this.defaultService = defaultService;
    }

    protected MonitoringServiceVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getServiceName() {
        return serviceName;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getServicePidFile() {
        return servicePidFile;
    }

    @Override
    public String getServicePath() {
        return servicePidFile;
    }

    @Override
    public String getUuid() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getAccountId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getDomainId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDefaultService() {
        return defaultService;
    }

    public String getProcessName() {
        return processName;
    }

    @Override
    public Class<?> getEntityType() {
        return MonitoringService.class;
    }
}
