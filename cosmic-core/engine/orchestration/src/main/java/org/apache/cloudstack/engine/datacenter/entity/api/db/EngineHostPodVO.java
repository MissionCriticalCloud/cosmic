package org.apache.cloudstack.engine.datacenter.entity.api.db;

import com.cloud.org.Grouping;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.StateMachine;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "host_pod_ref")
public class EngineHostPodVO implements EnginePod, Identity {
    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Column(name = "lastUpdated", updatable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdated;
    /**
     * Note that state is intentionally missing the setter.  Any updates to
     * the state machine needs to go through the DAO object because someone
     * else could be updating it as well.
     */
    @Enumerated(value = EnumType.STRING)
    @StateMachine(state = State.class, event = Event.class)
    @Column(name = "engine_state", updatable = true, nullable = false, length = 32)
    protected State state = null;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    AllocationState allocationState;
    @Column(name = "name")
    private String name = null;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "gateway")
    private String gateway;
    @Column(name = "cidr_address")
    private String cidrAddress;
    @Column(name = "cidr_size")
    private int cidrSize;
    @Column(name = "description")
    private String description;
    @Column(name = "external_dhcp")
    private Boolean externalDhcp;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "uuid")
    private String uuid;
    //orchestration
    @Column(name = "owner")
    private String owner = null;

    public EngineHostPodVO(final String name, final long dcId, final String gateway, final String cidrAddress, final int cidrSize, final String description) {
        this.name = name;
        this.dataCenterId = dcId;
        this.gateway = gateway;
        this.cidrAddress = cidrAddress;
        this.cidrSize = cidrSize;
        this.description = description;
        this.allocationState = Grouping.AllocationState.Enabled;
        this.externalDhcp = false;
        this.uuid = UUID.randomUUID().toString();
        this.state = State.Disabled;
    }

    /*
     * public HostPodVO(String name, long dcId) { this(null, name, dcId); }
     */
    protected EngineHostPodVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Use for comparisons only.
    public EngineHostPodVO(final Long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getCidrAddress() {
        return cidrAddress;
    }

    public void setCidrAddress(final String cidrAddress) {
        this.cidrAddress = cidrAddress;
    }

    @Override
    public int getCidrSize() {
        return cidrSize;
    }

    public void setCidrSize(final int cidrSize) {
        this.cidrSize = cidrSize;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public AllocationState getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final AllocationState allocationState) {
        this.allocationState = allocationState;
    }

    @Override
    public boolean getExternalDhcp() {
        return externalDhcp;
    }

    @Override
    public boolean belongsToDataCenter(final long dataCenterId) {
        return this.dataCenterId == dataCenterId;
    }

    public void setExternalDhcp(final boolean use) {
        externalDhcp = use;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof EngineHostPodVO) {
            return id == ((EngineHostPodVO) obj).id;
        } else {
            return false;
        }
    }

    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public State getState() {
        return state;
    }
}
