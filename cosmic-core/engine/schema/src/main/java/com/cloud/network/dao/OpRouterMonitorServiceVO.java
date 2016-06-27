package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "op_router_monitoring_services")
public class OpRouterMonitorServiceVO implements InternalIdentity {

    @Id
    @Column(name = "vm_id")
    Long id;

    @Column(name = "router_name")
    private String name;

    @Column(name = "last_alert_timestamp")
    private String lastAlertTimestamp;

    public OpRouterMonitorServiceVO() {
    }

    public OpRouterMonitorServiceVO(final long vmId, final String name, final String lastAlertTimestamp) {
        this.id = vmId;
        this.name = name;
        this.lastAlertTimestamp = lastAlertTimestamp;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastAlertTimestamp() {
        return lastAlertTimestamp;
    }

    public void setLastAlertTimestamp(final String timestamp) {
        this.lastAlertTimestamp = timestamp;
    }
}
