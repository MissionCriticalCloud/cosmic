package com.cloud.network.vpc;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

@Entity
@Table(name = "network_acl_item")
public class NetworkACLItemVO implements NetworkACLItem {

    /**
     *
     */
    private static final long serialVersionUID = 2790623532888742060L;
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "start_port", updatable = false)
    Integer sourcePortStart;
    @Column(name = "end_port", updatable = false)
    Integer sourcePortEnd;
    @Column(name = "protocol", updatable = false)
    String protocol = NetUtils.TCP_PROTO;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    State state;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "acl_id")
    long aclId;
    @Column(name = "icmp_code")
    Integer icmpCode;
    @Column(name = "icmp_type")
    Integer icmpType;
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    TrafficType trafficType;
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call the NetworkACLItem dao to load it.
    @Transient
    List<String> sourceCidrs;
    @Column(name = "uuid")
    String uuid;
    @Column(name = "number")
    int number;
    @Column(name = "action")
    @Enumerated(value = EnumType.STRING)
    Action action;

    public NetworkACLItemVO() {
        uuid = UUID.randomUUID().toString();
    }

    public NetworkACLItemVO(final Integer portStart, final Integer portEnd, final String protocol, final long aclId, final List<String> sourceCidrs, final Integer icmpCode,
                            final Integer icmpType,
                            final TrafficType trafficType, final Action action, final int number) {
        sourcePortStart = portStart;
        sourcePortEnd = portEnd;
        this.protocol = protocol;
        this.aclId = aclId;
        state = State.Staged;
        this.icmpCode = icmpCode;
        this.icmpType = icmpType;
        setSourceCidrList(sourceCidrs);
        uuid = UUID.randomUUID().toString();
        this.trafficType = trafficType;
        this.action = action;
        this.number = number;
    }

    @Override
    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return new StringBuilder("Rule[").append(id).append("-").append("NetworkACL").append("-").append(state).append("]").toString();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public int getNumber() {
        return number;
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
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public long getAclId() {
        return aclId;
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

    public void setTrafficType(final TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setIcmpType(final Integer icmpType) {
        this.icmpType = icmpType;
    }

    public void setIcmpCode(final Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setSourcePortEnd(final Integer sourcePortEnd) {
        this.sourcePortEnd = sourcePortEnd;
    }

    public void setSourcePortStart(final Integer sourcePortStart) {
        this.sourcePortStart = sourcePortStart;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setSourceCidrs(final String sourceCidrs) {
        final List<String> srcCidrs = new LinkedList<>();
        final StringTokenizer st = new StringTokenizer(sourceCidrs, ",;");
        while (st.hasMoreTokens()) {
            srcCidrs.add(st.nextToken());
        }
        this.sourceCidrs = srcCidrs;
    }
}
