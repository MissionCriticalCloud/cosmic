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
    @Column(name = "start_port", updatable = false)
    Integer sourcePortStart;
    @Column(name = "end_port", updatable = false)
    Integer sourcePortEnd;
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
    @Column(name = "related")
    Long related;
    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    FirewallRuleType type;
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

    public FirewallRuleVO(final String xId, final Long ipAddressId, final Integer portStart, final Integer portEnd, final String protocol, final long networkId, final long
            accountId, final long domainId,
                          final Purpose purpose, final List<String> sourceCidrs, final Integer icmpCode, final Integer icmpType, final Long related, final TrafficType
                                  trafficType, final FirewallRuleType type) {
        this(xId, ipAddressId, portStart, portEnd, protocol, networkId, accountId, domainId, purpose, sourceCidrs, icmpCode, icmpType, related, trafficType);
        this.type = type;
    }

    public FirewallRuleVO(final String xId, final Long ipAddressId, final Integer portStart, final Integer portEnd, final String protocol, final long networkId, final long
            accountId, final long domainId,
                          final Purpose purpose, final List<String> sourceCidrs, final Integer icmpCode, final Integer icmpType, final Long related, final TrafficType
                                  trafficType) {
        this.xId = xId;
        if (xId == null) {
            this.xId = UUID.randomUUID().toString();
        }
        this.accountId = accountId;
        this.domainId = domainId;
        sourceIpAddressId = ipAddressId;
        sourcePortStart = portStart;
        sourcePortEnd = portEnd;
        this.protocol = protocol;
        this.purpose = purpose;
        this.networkId = networkId;
        state = State.Staged;
        this.icmpCode = icmpCode;
        this.icmpType = icmpType;
        this.sourceCidrs = sourceCidrs;

        if (related != null) {
            assert (purpose == Purpose.Firewall) : "related field can be set for rule of purpose " + Purpose.Firewall + " only";
        }

        this.related = related;
        uuid = UUID.randomUUID().toString();
        type = FirewallRuleType.User;
        this.trafficType = trafficType;
    }

    public FirewallRuleVO(final String xId, final long ipAddressId, final int port, final String protocol, final long networkId, final long accountId, final long domainId, final
    Purpose purpose,
                          final List<String> sourceCidrs, final Integer icmpCode, final Integer icmpType, final Long related) {
        this(xId, ipAddressId, port, port, protocol, networkId, accountId, domainId, purpose, sourceCidrs, icmpCode, icmpType, related, null);
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
    public Integer getSourcePortStart() {
        return sourcePortStart;
    }

    @Override
    public Integer getSourcePortEnd() {
        return sourcePortEnd;
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
    public Long getRelated() {
        return related;
    }

    @Override
    public FirewallRuleType getType() {
        return type;
    }

    public void setType(final FirewallRuleType type) {
        this.type = type;
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
