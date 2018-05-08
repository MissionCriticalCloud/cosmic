package com.cloud.api.response;

import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.legacymodel.dc.DedicatedResources;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = DedicatedResources.class)
public class DedicateZoneResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the dedicated resource")
    private String id;

    @SerializedName("zoneid")
    @Param(description = "the ID of the Zone")
    private String zoneId;

    @SerializedName("zonename")
    @Param(description = "the Name of the Zone")
    private String zoneName;

    @SerializedName("domainid")
    @Param(description = "the domain ID to which the Zone is dedicated")
    private String domainId;

    @SerializedName("domainname")
    @Param(description = "the domain name of the host")
    private String domainName;

    @SerializedName("accountid")
    @Param(description = "the Account Id to which the Zone is dedicated")
    private String accountId;

    @SerializedName("accountname")
    @Param(description = "the Account name of the host")
    private String accountName;

    @SerializedName("affinitygroupid")
    @Param(description = "the Dedication Affinity Group ID of the zone")
    private String affinityGroupId;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
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

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }
}
