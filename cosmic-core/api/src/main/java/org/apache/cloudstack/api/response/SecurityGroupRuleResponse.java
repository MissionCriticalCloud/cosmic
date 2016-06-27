package org.apache.cloudstack.api.response;

import com.cloud.network.security.SecurityRule;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = SecurityRule.class)
public class SecurityGroupRuleResponse extends BaseResponse {
    @SerializedName("ruleid")
    @Param(description = "the id of the security group rule")
    private String ruleId;

    @SerializedName("protocol")
    @Param(description = "the protocol of the security group rule")
    private String protocol;

    @SerializedName(ApiConstants.ICMP_TYPE)
    @Param(description = "the type of the ICMP message response")
    private Integer icmpType;

    @SerializedName(ApiConstants.ICMP_CODE)
    @Param(description = "the code for the ICMP message response")
    private Integer icmpCode;

    @SerializedName(ApiConstants.START_PORT)
    @Param(description = "the starting IP of the security group rule")
    private Integer startPort;

    @SerializedName(ApiConstants.END_PORT)
    @Param(description = "the ending IP of the security group rule ")
    private Integer endPort;

    @SerializedName(ApiConstants.SECURITY_GROUP_NAME)
    @Param(description = "security group name")
    private String securityGroupName;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "account owning the security group rule")
    private String accountName;

    @SerializedName(ApiConstants.CIDR)
    @Param(description = "the CIDR notation for the base IP address of the security group rule")
    private String cidr;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with the rule", responseObject = ResourceTagResponse.class)
    private java.util.Set<ResourceTagResponse> tags;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(final Integer icmpType) {
        this.icmpType = icmpType;
    }

    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(final Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public void setStartPort(final Integer startPort) {
        this.startPort = startPort;
    }

    public Integer getEndPort() {
        return endPort;
    }

    public void setEndPort(final Integer endPort) {
        this.endPort = endPort;
    }

    public String getSecurityGroupName() {
        return securityGroupName;
    }

    public void setSecurityGroupName(final String securityGroupName) {
        this.securityGroupName = securityGroupName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String oid = this.getRuleId();
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        return result;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(final String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SecurityGroupRuleResponse other = (SecurityGroupRuleResponse) obj;
        final String oid = this.getRuleId();
        if (oid == null) {
            if (other.getRuleId() != null) {
                return false;
            }
        } else if (!oid.equals(other.getRuleId())) {
            return false;
        }
        return true;
    }

    public void setTags(final Set<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void addTag(final ResourceTagResponse tag) {
        this.tags.add(tag);
    }
}
