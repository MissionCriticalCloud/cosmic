//

//

package com.cloud.network.nicira;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class AccessRule implements Serializable {

    public static final String ETHERTYPE_IPV4 = "IPv4";
    public static final String ETHERTYPE_IPV6 = "IPv6";

    protected String ethertype = ETHERTYPE_IPV4;

    protected int protocol;

    public String getEthertype() {
        return ethertype;
    }

    public void setEthertype(final String ethertype) {
        this.ethertype = ethertype;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(final int protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE, false);
    }
}
