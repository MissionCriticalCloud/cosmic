package com.cloud.network.rules;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "firewall_rules")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "purpose", discriminatorType = DiscriminatorType.STRING, length = 32)
public class FirewallRuleVO implements FirewallRule {

    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = GenericDao.XID_COLUMN)
    String xId;
    @Column(name = "domain_id", updatable = false)
    long domainId;
    @Column(name = "account_id", updatable = false)
    long accountId;
    @Column(name = "ip_address_id", updatable = false)
    Long sourceIpAddressId;
    @Column(name = "port", updatable = false)
    Integer sourcePortStart;
    @Column(name = "protocol", updatable = false)
    String protocol = NetUtils.TCP_PROTO;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "purpose")
    Purpose purpose;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    State state;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "network_id")
    Long networkId;
    @Column(name = "icmp_code")
    Integer icmpCode;
    @Column(name = "icmp_type")
    Integer icmpType;
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    TrafficType trafficType;
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call firewallrules dao to load it.
    @Transient
    List<String> sourceCidrs;

    @Column(name = "uuid")
    String uuid;

    protected FirewallRuleVO() {
        uuid = UUID.randomUUID().toString();
    }

    public FirewallRuleVO(final String xId, final Long ipAddressId, final Integer portStart, final String protocol, final long networkId, final long accountId, final long domainId,
                          final Purpose purpose, final List<String> sourceCidrs, final Integer icmpCode, final Integer icmpType, final TrafficType trafficType) {
        this.xId = xId;
        if (xId == null) {
            this.xId = UUID.randomUUID().toString();
        }
        this.accountId = accountId;
        this.domainId = domainId;
        sourceIpAddressId = ipAddressId;
        sourcePortStart = portStart;
        this.protocol = protocol;
        this.purpose = purpose;
        this.networkId = networkId;
        state = State.Staged;
        this.icmpCode = icmpCode;
        this.icmpType = icmpType;
        this.sourceCidrs = sourceCidrs;
        uuid = UUID.randomUUID().toString();
        this.trafficType = trafficType;
    }

    public FirewallRuleVO(final String xId, final long ipAddressId, final int port, final String protocol, final long networkId, final long accountId, final long domainId, final Purpose purpose,
                          final List<String> sourceCidrs, final Integer icmpCode, final Integer icmpType) {
        this(xId, ipAddressId, port, protocol, networkId, accountId, domainId, purpose, sourceCidrs, icmpCode, icmpType, null);
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getXid() {
        return xId;
    }

    @Override
    public Integer getSourcePort() {
        return sourcePortStart;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Purpose getPurpose() {
        return purpose;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public Long getSourceIpAddressId() {
        return sourceIpAddressId;
    }

    @Override
    public Integer getIcmpCode() {
        return icmpCode;
    }

    @Override
    public Integer getIcmpType() {
        return icmpType;
    }

    @Override
    public List<String> getSourceCidrList() {
        return sourceCidrs;
    }

    public void setSourceCidrList(final List<String> sourceCidrs) {
        this.sourceCidrs = sourceCidrs;
    }

    @Override
    public TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return new StringBuilder("Rule[").append(id).append("-").append(purpose).append("-").append(state).append("]").toString();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return FirewallRule.class;
    }
}
