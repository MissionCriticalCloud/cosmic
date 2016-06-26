package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class LdapConfigurationResponse extends BaseResponse {
    @SerializedName("hostname")
    @Param(description = "hostname")
    private String hostname;

    @SerializedName("port")
    @Param(description = "port")
    private int port;

    public LdapConfigurationResponse() {
        super();
    }

    public LdapConfigurationResponse(final String hostname) {
        super();
        this.hostname = hostname;
    }

    public LdapConfigurationResponse(final String hostname, final int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
