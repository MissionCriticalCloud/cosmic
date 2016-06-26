package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.GuestOSHypervisor;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = GuestOSHypervisor.class)
public class GuestOsMappingResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the Guest OS mapping")
    private String id;

    @SerializedName(ApiConstants.HYPERVISOR)
    @Param(description = "the hypervisor")
    private String hypervisor;

    @SerializedName(ApiConstants.HYPERVISOR_VERSION)
    @Param(description = "version of the hypervisor for mapping")
    private String hypervisorVersion;

    @SerializedName(ApiConstants.OS_TYPE_ID)
    @Param(description = "the ID of the Guest OS type")
    private String osTypeId;

    @SerializedName(ApiConstants.OS_DISPLAY_NAME)
    @Param(description = "standard display name for the Guest OS")
    private String osStdName;

    @SerializedName(ApiConstants.OS_NAME_FOR_HYPERVISOR)
    @Param(description = "hypervisor specific name for the Guest OS")
    private String osNameForHypervisor;

    @SerializedName(ApiConstants.IS_USER_DEFINED)
    @Param(description = "is the mapping user defined")
    private String isUserDefined;

    public String getIsUserDefined() {
        return isUserDefined;
    }

    public void setIsUserDefined(final String isUserDefined) {
        this.isUserDefined = isUserDefined;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    public void setHypervisorVersion(final String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    public String getOsTypeId() {
        return osTypeId;
    }

    public void setOsTypeId(final String osTypeId) {
        this.osTypeId = osTypeId;
    }

    public String getOsStdName() {
        return osStdName;
    }

    public void setOsStdName(final String osStdName) {
        this.osStdName = osStdName;
    }

    public String getOsNameForHypervisor() {
        return osNameForHypervisor;
    }

    public void setOsNameForHypervisor(final String osNameForHypervisor) {
        this.osNameForHypervisor = osNameForHypervisor;
    }
}
