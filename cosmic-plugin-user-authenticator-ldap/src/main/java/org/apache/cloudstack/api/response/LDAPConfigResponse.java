package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/**
 * @deprecated as of 4.3 along with the api {@link org.apache.cloudstack.api.command.LDAPConfigCmd}
 */
@Deprecated
public class LDAPConfigResponse extends BaseResponse {

    @SerializedName(ApiConstants.HOST_NAME)
    @Param(description = "Hostname or ip address of the ldap server eg: my.ldap.com")
    private String hostname;

    @SerializedName(ApiConstants.PORT)
    @Param(description = "Specify the LDAP port if required, default is 389")
    private String port;

    @SerializedName(ApiConstants.USE_SSL)
    @Param(description = "Check Use SSL if the external LDAP server is configured for LDAP over SSL")
    private String useSSL;

    @SerializedName(ApiConstants.SEARCH_BASE)
    @Param(description = "The search base defines the starting point for the search in the directory tree Example:  dc=cloud,dc=com")
    private String searchBase;

    @SerializedName(ApiConstants.QUERY_FILTER)
    @Param(description = "You specify a query filter here, which narrows down the users, who can be part of this domain")
    private String queryFilter;

    @SerializedName(ApiConstants.BIND_DN)
    @Param(description = "Specify the distinguished name of a user with the search permission on the directory")
    private String bindDN;

    @SerializedName(ApiConstants.BIND_PASSWORD)
    @Param(description = "DN password", isSensitive = true)
    private String bindPassword;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(final String useSSL) {
        this.useSSL = useSSL;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(final String queryFilter) {
        this.queryFilter = queryFilter;
    }

    public String getBindDN() {
        return bindDN;
    }

    public void setBindDN(final String bindDN) {
        this.bindDN = bindDN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(final String bindPassword) {
        this.bindPassword = bindPassword;
    }
}
