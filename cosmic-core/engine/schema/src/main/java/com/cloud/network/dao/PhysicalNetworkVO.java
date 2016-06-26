package com.cloud.network.dao;

import com.cloud.network.PhysicalNetwork;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * NetworkConfigurationVO contains information about a specific physical network.
 */
@Entity
@Table(name = "physical_network")
public class PhysicalNetworkVO implements PhysicalNetwork {
    @Id
    @TableGenerator(name = "physical_networks_sq",
            table = "sequence",
            pkColumnName = "name",
            valueColumnName = "value",
            pkColumnValue = "physical_networks_seq",
            allocationSize = 1)
    @Column(name = "id")
    long id;
    @Column(name = "data_center_id")
    long dataCenterId;
    @Column(name = "domain_id")
    Long domainId = null;
    @Column(name = "broadcast_domain_range")
    @Enumerated(value = EnumType.STRING)
    BroadcastDomainRange broadcastDomainRange;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @Column(name = "tag")
    @CollectionTable(name = "physical_network_tags", joinColumns = @JoinColumn(name = "physical_network_id"))
    List<String> tags;
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @Column(name = "isolation_method")
    @CollectionTable(name = "physical_network_isolation_methods", joinColumns = @JoinColumn(name = "physical_network_id"))
    List<String> isolationMethods;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "name")
    private String name;
    @Column(name = "vnet")
    private String vnet = null;
    @Column(name = "speed")
    private String speed = null;

    public PhysicalNetworkVO() {

    }

    public PhysicalNetworkVO(final long id, final long dataCenterId, final String vnet, final String speed, final Long domainId, final BroadcastDomainRange broadcastDomainRange,
                             final String name) {
        this.dataCenterId = dataCenterId;
        this.setVnet(vnet);
        this.setSpeed(speed);
        this.domainId = domainId;
        if (broadcastDomainRange != null) {
            this.broadcastDomainRange = broadcastDomainRange;
        } else {
            this.broadcastDomainRange = BroadcastDomainRange.POD;
        }
        this.state = State.Disabled;
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public void addTag(final String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }

    @Override
    public BroadcastDomainRange getBroadcastDomainRange() {
        return broadcastDomainRange;
    }

    public void setBroadcastDomainRange(final BroadcastDomainRange broadcastDomainRange) {
        this.broadcastDomainRange = broadcastDomainRange;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    @Override
    public List<String> getIsolationMethods() {
        return isolationMethods != null ? isolationMethods : new ArrayList<>();
    }

    @Override
    public Long getDomainId() {
        return domainId;
    }

    @Override
    public List<Pair<Integer, Integer>> getVnet() {
        final List<Pair<Integer, Integer>> vnetList = new ArrayList<>();
        if (vnet != null) {
            final String[] Temp = vnet.split(",");
            String[] vnetSplit = null;
            for (final String vnetRange : Temp) {
                vnetSplit = vnetRange.split("-");
                vnetList.add(new Pair<>(Integer.parseInt(vnetSplit[0]), Integer.parseInt(vnetSplit[1])));
            }
        }
        return vnetList;
    }

    public void setVnet(final String vnet) {
        this.vnet = vnet;
    }

    @Override
    public String getVnetString() {
        return vnet;
    }

    @Override
    public String getSpeed() {
        return speed;
    }

    public void setSpeed(final String speed) {
        this.speed = speed;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setIsolationMethods(final List<String> isolationMethods) {
        this.isolationMethods = isolationMethods;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void addIsolationMethod(final String isolationMethod) {
        if (isolationMethods == null) {
            isolationMethods = new ArrayList<>();
        }
        isolationMethods.add(isolationMethod);
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
