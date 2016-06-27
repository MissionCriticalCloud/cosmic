package com.cloud.network.dao;

import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkTrafficType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "physical_network_traffic_types")
public class PhysicalNetworkTrafficTypeVO implements PhysicalNetworkTrafficType {
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    TrafficType trafficType;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "physical_network_id")
    private long physicalNetworkId;
    @Column(name = "xenserver_network_label")
    private String xenNetworkLabel;

    @Column(name = "kvm_network_label")
    private String kvmNetworkLabel;

    @Column(name = "ovm_network_label")
    private String ovm3NetworkLabel;

    @Column(name = "vlan")
    private String vlan;

    public PhysicalNetworkTrafficTypeVO() {
    }

    public PhysicalNetworkTrafficTypeVO(final long physicalNetworkId, final TrafficType trafficType, final String xenLabel, final String kvmLabel,
                                        final String vlan, final String ovm3Label) {
        this.physicalNetworkId = physicalNetworkId;
        this.trafficType = trafficType;
        xenNetworkLabel = xenLabel;
        kvmNetworkLabel = kvmLabel;
        ovm3NetworkLabel = ovm3Label;
        setVlan(vlan);
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    @Override
    public TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public String getXenNetworkLabel() {
        return xenNetworkLabel;
    }

    public void setXenNetworkLabel(final String xenNetworkLabel) {
        this.xenNetworkLabel = xenNetworkLabel;
    }

    @Override
    public String getKvmNetworkLabel() {
        return kvmNetworkLabel;
    }

    public void setKvmNetworkLabel(final String kvmNetworkLabel) {
        this.kvmNetworkLabel = kvmNetworkLabel;
    }

    @Override
    public String getOvm3NetworkLabel() {
        return ovm3NetworkLabel;
    }

    public void setOvm3NetworkLabel(final String ovm3NetworkLabel) {
        this.ovm3NetworkLabel = ovm3NetworkLabel;
    }

    public String getVlan() {
        return vlan;
    }

    public void setVlan(final String vlan) {
        this.vlan = vlan;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
