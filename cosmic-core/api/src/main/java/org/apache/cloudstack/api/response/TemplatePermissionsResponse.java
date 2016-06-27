package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VirtualMachineTemplate.class)
public class TemplatePermissionsResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the template ID")
    private String id;

    @SerializedName(ApiConstants.IS_PUBLIC)
    @Param(description = "true if this template is a public template, false otherwise")
    private Boolean publicTemplate;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the ID of the domain to which the template belongs")
    private String domainId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the list of accounts the template is available for")
    private List<String> accountNames;

    @SerializedName(ApiConstants.PROJECT_IDS)
    @Param(description = "the list of projects the template is available for")
    private List<String> projectIds;

    public void setId(final String id) {
        this.id = id;
    }

    public void setPublicTemplate(final Boolean publicTemplate) {
        this.publicTemplate = publicTemplate;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setAccountNames(final List<String> accountNames) {
        this.accountNames = accountNames;
    }

    public void setProjectIds(final List<String> projectIds) {
        this.projectIds = projectIds;
    }
}
