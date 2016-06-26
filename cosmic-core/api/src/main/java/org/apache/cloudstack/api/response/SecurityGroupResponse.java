package org.apache.cloudstack.api.response;

import com.cloud.network.security.SecurityGroup;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = SecurityGroup.class)
public class SecurityGroupResponse extends BaseResponse implements ControlledViewEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the security group")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the security group")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the security group")
    private String description;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account owning the security group")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the group")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the group")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the security group")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the security group")
    private String domainName;

    @SerializedName("ingressrule")
    @Param(description = "the list of ingress rules associated with the security group", responseObject = SecurityGroupRuleResponse.class)
    private Set<SecurityGroupRuleResponse> ingressRules;

    @SerializedName("egressrule")
    @Param(description = "the list of egress rules associated with the security group", responseObject = SecurityGroupRuleResponse.class)
    private Set<SecurityGroupRuleResponse> egressRules;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with the rule", responseObject = ResourceTagResponse.class)
    private Set<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_COUNT)
    @Param(description = "the number of virtualmachines associated with this securitygroup", since = "4.6.0")
    private Integer virtualMachineCount;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_IDS)
    @Param(description = "the list of virtualmachine ids associated with this securitygroup", since = "4.6.0")
    private Set<String> virtualMachineIds;

    public SecurityGroupResponse() {
        this.virtualMachineIds = new LinkedHashSet<>();
        this.ingressRules = new LinkedHashSet<>();
        this.egressRules = new LinkedHashSet<>();
        this.tags = new LinkedHashSet<>();
    }

    @Override
    public String getObjectId() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setSecurityGroupIngressRules(final Set<SecurityGroupRuleResponse> securityGroupRules) {
        this.ingressRules = securityGroupRules;
    }

    public void addSecurityGroupIngressRule(final SecurityGroupRuleResponse rule) {
        this.ingressRules.add(rule);
    }

    public void setSecurityGroupEgressRules(final Set<SecurityGroupRuleResponse> securityGroupRules) {
        this.egressRules = securityGroupRules;
    }

    public void addSecurityGroupEgressRule(final SecurityGroupRuleResponse rule) {
        this.egressRules.add(rule);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
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
        final SecurityGroupResponse other = (SecurityGroupResponse) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
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

    public void setVirtualMachineCount(final Integer virtualMachineCount) {
        this.virtualMachineCount = virtualMachineCount;
    }

    public void setVirtualMachineIds(final Set<String> virtualMachineIds) {
        this.virtualMachineIds = virtualMachineIds;
    }

    public void addVirtualMachineId(final String virtualMachineId) {
        this.virtualMachineIds.add(virtualMachineId);
    }
}
