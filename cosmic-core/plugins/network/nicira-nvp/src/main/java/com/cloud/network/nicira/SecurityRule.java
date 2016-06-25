//

//

package com.cloud.network.nicira;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SecurityRule extends AccessRule {

    protected String ipPrefix;

    protected int portRangeMin;

    protected int portRangeMax;

    protected String profileUuid;

    /**
     * Default constructor
     */
    public SecurityRule() {
    }

    /**
     * Fully parameterized constructor
     */
    public SecurityRule(final String ethertype, final String ipPrefix, final String profileUuid,
                        final int portRangeMin, final int portRangeMax, final int protocol) {
        this.ethertype = ethertype;
        this.ipPrefix = ipPrefix;
        this.portRangeMin = portRangeMin;
        this.portRangeMax = portRangeMax;
        this.profileUuid = profileUuid;
        this.protocol = protocol;
    }

    @Override
    public String getEthertype() {
        return ethertype;
    }

    @Override
    public void setEthertype(final String ethertype) {
        this.ethertype = ethertype;
    }

    @Override
    public int getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(final int protocol) {
        this.protocol = protocol;
    }

    public String getIpPrefix() {
        return ipPrefix;
    }

    public void setIpPrefix(final String ipPrefix) {
        this.ipPrefix = ipPrefix;
    }

    public int getPortRangeMin() {
        return portRangeMin;
    }

    public void setPortRangeMin(final int portRangeMin) {
        this.portRangeMin = portRangeMin;
    }

    public int getPortRangeMax() {
        return portRangeMax;
    }

    public void setPortRangeMax(final int portRangeMax) {
        this.portRangeMax = portRangeMax;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(final String profileUuid) {
        this.profileUuid = profileUuid;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(ethertype).append(ipPrefix)
                .append(portRangeMin).append(portRangeMax)
                .append(profileUuid).append(protocol)
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
        if (!(obj instanceof SecurityRule)) {
            return false;
        }
        final SecurityRule another = (SecurityRule) obj;
        return new EqualsBuilder()
                .append(ethertype, another.ethertype)
                .append(ipPrefix, another.ipPrefix)
                .append(portRangeMin, another.portRangeMin)
                .append(portRangeMax, another.portRangeMax)
                .append(profileUuid, another.profileUuid)
                .append(protocol, another.protocol)
                .isEquals();
    }
}
