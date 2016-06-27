package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("load_balancer_vm_map"))
public class LoadBalancerVMMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "load_balancer_id")
    private long loadBalancerId;

    @Column(name = "instance_id")
    private long instanceId;

    @Column(name = "instance_ip")
    private String instanceIp;

    @Column(name = "revoke")
    private boolean revoke = false;

    @Column(name = "state")
    private String state;

    public LoadBalancerVMMapVO() {
    }

    public LoadBalancerVMMapVO(final long loadBalancerId, final long instanceId) {
        this.loadBalancerId = loadBalancerId;
        this.instanceId = instanceId;
    }

    public LoadBalancerVMMapVO(final long loadBalancerId, final long instanceId, final boolean revoke) {
        this.loadBalancerId = loadBalancerId;
        this.instanceId = instanceId;
        this.revoke = revoke;
    }

    public LoadBalancerVMMapVO(final long loadBalancerId, final long instanceId, final String vmIp, final boolean revoke) {
        this.loadBalancerId = loadBalancerId;
        this.instanceId = instanceId;
        this.instanceIp = vmIp;
        this.revoke = revoke;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getLoadBalancerId() {
        return loadBalancerId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getInstanceIp() {
        return instanceIp;
    }

    public void setInstanceIp(final String instanceIp) {
        this.instanceIp = instanceIp;
    }
}
