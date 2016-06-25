//

//

package com.cloud.network.nicira;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AclRule extends AccessRule {

    public static final String ETHERTYPE_ARP = "ARP";

    /**
     * @TODO Convert this String into Enum and check the JSON communication still works
     */
    protected String action;

    protected String sourceIpPrefix;

    protected String destinationIpPrefix;

    protected String sourceMacAddress;

    protected String destinationMacAddress;

    protected Integer sourcePortRangeMin;

    protected Integer destinationPortRangeMin;

    protected Integer sourcePortRangeMax;

    protected Integer destinationPortRangeMax;

    protected Integer icmpProtocolCode;

    protected Integer icmpProtocolType;

    protected int order;

    /**
     * Default constructor
     */
    public AclRule() {
    }

    /**
     * Fully parameterized constructor
     */
    public AclRule(final String ethertype, final int protocol, final String action, final String sourceMacAddress,
                   final String destinationMacAddress, final String sourceIpPrefix, final String destinationIpPrefix,
                   final Integer sourcePortRangeMin, final Integer sourcePortRangeMax,
                   final Integer destinationPortRangeMin, final Integer destinationPortRangeMax,
                   final int order, final Integer icmpProtocolCode, final Integer icmpProtocolType) {
        this.ethertype = ethertype;
        this.protocol = protocol;
        this.action = action;
        this.sourceMacAddress = sourceMacAddress;
        this.destinationMacAddress = destinationMacAddress;
        this.sourceIpPrefix = sourceIpPrefix;
        this.destinationIpPrefix = destinationIpPrefix;
        this.sourcePortRangeMin = sourcePortRangeMin;
        this.sourcePortRangeMax = sourcePortRangeMax;
        this.destinationPortRangeMin = destinationPortRangeMin;
        this.destinationPortRangeMax = destinationPortRangeMax;
        this.order = order;
        this.icmpProtocolCode = icmpProtocolCode;
        this.icmpProtocolType = icmpProtocolType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getSourceIpPrefix() {
        return sourceIpPrefix;
    }

    public void setSourceIpPrefix(final String sourceIpPrefix) {
        this.sourceIpPrefix = sourceIpPrefix;
    }

    public String getDestinationIpPrefix() {
        return destinationIpPrefix;
    }

    public void setDestinationIpPrefix(final String destinationIpPrefix) {
        this.destinationIpPrefix = destinationIpPrefix;
    }

    public String getSourceMacAddress() {
        return sourceMacAddress;
    }

    public void setSourceMacAddress(final String sourceMacAddress) {
        this.sourceMacAddress = sourceMacAddress;
    }

    public String getDestinationMacAddress() {
        return destinationMacAddress;
    }

    public void setDestinationMacAddress(final String destinationMacAddress) {
        this.destinationMacAddress = destinationMacAddress;
    }

    public Integer getSourcePortRangeMin() {
        return sourcePortRangeMin;
    }

    public void setSourcePortRangeMin(final Integer sourcePortRangeMin) {
        this.sourcePortRangeMin = sourcePortRangeMin;
    }

    public Integer getDestinationPortRangeMin() {
        return destinationPortRangeMin;
    }

    public void setDestinationPortRangeMin(final Integer destinationPortRangeMin) {
        this.destinationPortRangeMin = destinationPortRangeMin;
    }

    public Integer getSourcePortRangeMax() {
        return sourcePortRangeMax;
    }

    public void setSourcePortRangeMax(final Integer sourcePortRangeMax) {
        this.sourcePortRangeMax = sourcePortRangeMax;
    }

    public Integer getDestinationPortRangeMax() {
        return destinationPortRangeMax;
    }

    public void setDestinationPortRangeMax(final Integer destinationPortRangeMax) {
        this.destinationPortRangeMax = destinationPortRangeMax;
    }

    public Integer getIcmpProtocolCode() {
        return icmpProtocolCode;
    }

    public void setIcmpProtocolCode(final Integer icmpProtocolCode) {
        this.icmpProtocolCode = icmpProtocolCode;
    }

    public Integer getIcmpProtocolType() {
        return icmpProtocolType;
    }

    public void setIcmpProtocolType(final Integer icmpProtocolType) {
        this.icmpProtocolType = icmpProtocolType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(ethertype).append(protocol)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AclRule)) {
            return false;
        }
        final AclRule another = (AclRule) obj;
        return new EqualsBuilder()
                .append(ethertype, another.ethertype)
                .append(protocol, another.protocol)
                .isEquals();
    }
}
