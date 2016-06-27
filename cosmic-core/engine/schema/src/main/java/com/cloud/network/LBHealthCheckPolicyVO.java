package com.cloud.network;

import com.cloud.network.rules.HealthCheckPolicy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ("load_balancer_healthcheck_policies"))
@PrimaryKeyJoinColumn(name = "load_balancer_id", referencedColumnName = "id")
public class LBHealthCheckPolicyVO implements HealthCheckPolicy {
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "load_balancer_id")
    private long loadBalancerId;
    @Column(name = "pingpath")
    private String pingPath;
    @Column(name = "description")
    private String description;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "response_time")
    private int responseTime;
    @Column(name = "healthcheck_interval")
    private int healthcheckInterval;
    @Column(name = "healthcheck_thresshold")
    private int healthcheckThresshold;
    @Column(name = "unhealth_thresshold")
    private int unhealthThresshold;
    @Column(name = "revoke")
    private boolean revoke = false;

    protected LBHealthCheckPolicyVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public LBHealthCheckPolicyVO(final long loadBalancerId, final String pingPath, final String description, final int responseTime, final int healthcheckInterval, final int
            healthcheckThresshold,
                                 final int unhealthThresshold) {
        this.loadBalancerId = loadBalancerId;

        if (pingPath == null || pingPath.isEmpty()) {
            this.pingPath = "/";
        } else {
            this.pingPath = pingPath;
        }

        if (responseTime == 0) {
            this.responseTime = 2;
        } else {
            this.responseTime = responseTime;
        }

        if (healthcheckInterval == 0) {
            this.healthcheckInterval = 5;
        } else {
            this.healthcheckInterval = healthcheckInterval;
        }

        if (healthcheckThresshold == 0) {
            this.healthcheckThresshold = 2;
        } else {
            this.healthcheckThresshold = healthcheckThresshold;
        }

        if (unhealthThresshold == 0) {
            this.unhealthThresshold = 1;
        } else {
            this.unhealthThresshold = unhealthThresshold;
        }
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getLoadBalancerId() {
        return loadBalancerId;
    }

    @Override
    public String getpingpath() {
        return pingPath;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getResponseTime() {
        return responseTime;
    }

    @Override
    public int getHealthcheckInterval() {
        return healthcheckInterval;
    }

    @Override
    public int getHealthcheckThresshold() {
        return healthcheckThresshold;
    }

    @Override
    public int getUnhealthThresshold() {
        return unhealthThresshold;
    }

    @Override
    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
