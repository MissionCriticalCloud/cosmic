package com.cloud.agent.api.to;

import com.cloud.network.vpc.NetworkACLItem;
import com.cloud.network.vpc.NetworkACLItem.TrafficType;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.ArrayList;
import java.util.List;

public class NetworkACLTO implements InternalIdentity {
    long id;
    String vlanTag;
    String protocol;
    int[] portRange;
    boolean revoked;
    boolean alreadyAdded;
    String action;
    int number;
    private List<String> cidrList;
    private Integer icmpType;
    private Integer icmpCode;
    private TrafficType trafficType;

    protected NetworkACLTO() {
    }

    public NetworkACLTO(final NetworkACLItem rule, final String vlanTag, final NetworkACLItem.TrafficType trafficType) {
        this(rule.getId(),
                vlanTag,
                rule.getProtocol(),
                rule.getSourcePortStart(),
                rule.getSourcePortEnd(),
                rule.getState() == NetworkACLItem.State.Revoke,
                rule.getState() == NetworkACLItem.State.Active,
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode(),
                trafficType,
                rule.getAction() == NetworkACLItem.Action.Allow,
                rule.getNumber());
    }

    public NetworkACLTO(final long id, final String vlanTag, final String protocol, final Integer portStart, final Integer portEnd, final boolean revoked, final boolean
            alreadyAdded, final List<String> cidrList,
                        final Integer icmpType, final Integer icmpCode, final TrafficType trafficType, final boolean allow, final int number) {
        this.vlanTag = vlanTag;
        this.protocol = protocol;

        if (portStart != null) {
            final List<Integer> range = new ArrayList<>();
            range.add(portStart);
            if (portEnd != null) {
                range.add(portEnd);
            }

            portRange = new int[range.size()];
            int i = 0;
            for (final Integer port : range) {
                portRange[i] = port.intValue();
                i++;
            }
        }

        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.cidrList = cidrList;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.trafficType = trafficType;

        if (!allow) {
            this.action = "DROP";
        } else {
            this.action = "ACCEPT";
        }

        this.number = number;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getSrcVlanTag() {
        return vlanTag;
    }

    public String getProtocol() {
        return protocol;
    }

    public int[] getSrcPortRange() {
        return portRange;
    }

    public Integer getIcmpType() {
        return icmpType;
    }

    public Integer getIcmpCode() {
        return icmpCode;
    }

    public String getStringPortRange() {
        if (portRange == null || portRange.length < 2) {
            return "0:0";
        } else {
            return NetUtils.portRangeToString(portRange);
        }
    }

    public boolean revoked() {
        return revoked;
    }

    public List<String> getSourceCidrList() {
        return cidrList;
    }

    public boolean isAlreadyAdded() {
        return alreadyAdded;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public String getAction() {
        return action;
    }

    public int getNumber() {
        return number;
    }
}
