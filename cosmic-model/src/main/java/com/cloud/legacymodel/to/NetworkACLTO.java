package com.cloud.legacymodel.to;

import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.network.vpc.NetworkACLItem;
import com.cloud.legacymodel.network.vpc.NetworkACLItem.TrafficType;

import java.util.ArrayList;
import java.util.List;

public class NetworkACLTO implements InternalIdentity {
    private long id;
    private String vlanTag;
    private String protocol;
    private int[] portRange;
    private boolean revoked;
    private boolean alreadyAdded;
    private String action;
    private int number;
    private List<String> cidrList;
    private Integer icmpType;
    private Integer icmpCode;
    private TrafficType trafficType;

    protected NetworkACLTO() {
    }

    public NetworkACLTO(final NetworkACLItem rule, final String vlanTag, final TrafficType trafficType) {
        this(
                rule.getId(),
                vlanTag,
                rule.getProtocol(),
                rule.getSourcePortStart(),
                rule.getSourcePortEnd(),
                NetworkACLItem.State.Revoke.equals(rule.getState()),
                NetworkACLItem.State.Active.equals(rule.getState()),
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode(),
                trafficType,
                NetworkACLItem.Action.Allow.equals(rule.getAction()),
                rule.getNumber()
        );
    }

    public NetworkACLTO(final long id, final String vlanTag, final String protocol, final Integer portStart, final Integer portEnd, final boolean revoked,
                        final boolean alreadyAdded, final List<String> cidrList, final Integer icmpType, final Integer icmpCode, final TrafficType trafficType,
                        final boolean allow, final int number) {
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
                portRange[i] = port;
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
            return Integer.toString(portRange[0]) + ":" + Integer.toString(portRange[1]);
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
