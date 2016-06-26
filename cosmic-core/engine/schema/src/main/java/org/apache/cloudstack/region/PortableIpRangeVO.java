package org.apache.cloudstack.region;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "portable_ip_range")
public class PortableIpRangeVO implements PortableIpRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "uuid")
    String uuid;

    @Column(name = "region_id")
    int regionId;

    @Column(name = "vlan_id")
    String vlan;

    @Column(name = "gateway")
    String gateway;

    @Column(name = "netmask")
    String netmask;

    @Column(name = "start_ip")
    String startIp;

    @Column(name = "end_ip")
    String endIp;

    public PortableIpRangeVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public PortableIpRangeVO(final int regionId, final String vlan, final String gateway, final String netmask, final String startIp, final String endIp) {
        this.uuid = UUID.randomUUID().toString();
        this.regionId = regionId;
        this.vlan = vlan;
        this.gateway = gateway;
        this.netmask = netmask;
        this.startIp = startIp;
        this.endIp = endIp;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getVlanTag() {
        return vlan;
    }

    public void setVlanTag(final String vlan) {
        this.vlan = vlan;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    @Override
    public String getNetmask() {
        return netmask;
    }

    @Override
    public int getRegionId() {
        return regionId;
    }

    @Override
    public String getIpRange() {
        return startIp + "-" + endIp;
    }
}
