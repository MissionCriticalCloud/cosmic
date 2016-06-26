package org.apache.cloudstack.api.response;

import com.cloud.dc.DedicatedResources;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = DedicatedResources.class)
public class DedicatePodResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the dedicated resource")
    private String id;

    @SerializedName("podid")
    @Param(description = "the ID of the Pod")
    private String podId;

    @SerializedName("podname")
    @Param(description = "the Name of the Pod")
    private String podName;

    @SerializedName("domainid")
    @Param(description = "the domain ID to which the Pod is dedicated")
    private String domainId;

    @SerializedName("accountid")
    @Param(description = "the Account Id to which the Pod is dedicated")
    private String accountId;

    @SerializedName("affinitygroupid")
    @Param(description = "the Dedication Affinity Group ID of the pod")
    private String affinityGroupId;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(final String podId) {
        this.podId = podId;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(final String podName) {
        this.podName = podName;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String getAffinityGroupId() {
        return affinityGroupId;
    }

    public void setAffinityGroupId(final String affinityGroupId) {
        this.affinityGroupId = affinityGroupId;
    }
}
