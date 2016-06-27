package org.apache.cloudstack.api.response;

import com.cloud.projects.Project;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Project.class)
public class ProjectResponse extends BaseResponse implements ResourceLimitAndCountResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the project")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the project")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "the displaytext of the project")
    private String displaytext;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id the project belongs to")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name where the project belongs to")
    private String domain;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account name of the project's owner")
    private String ownerName;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the project")
    private String state;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with vm", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags = new ArrayList<>();

    @SerializedName("networklimit")
    @Param(description = "the total number of networks the project can own", since = "4.2.0")
    private String networkLimit;

    @SerializedName("networktotal")
    @Param(description = "the total number of networks owned by project", since = "4.2.0")
    private Long networkTotal;

    @SerializedName("networkavailable")
    @Param(description = "the total number of networks available to be created for this project", since = "4.2.0")
    private String networkAvailable;

    @SerializedName("vpclimit")
    @Param(description = "the total number of vpcs the project can own", since = "4.2.0")
    private String vpcLimit;

    @SerializedName("vpctotal")
    @Param(description = "the total number of vpcs owned by project", since = "4.2.0")
    private Long vpcTotal;

    @SerializedName("vpcavailable")
    @Param(description = "the total number of vpcs available to be created for this project", since = "4.2.0")
    private String vpcAvailable;

    @SerializedName("cpulimit")
    @Param(description = "the total number of cpu cores the project can own", since = "4.2.0")
    private String cpuLimit;

    @SerializedName("cputotal")
    @Param(description = "the total number of cpu cores owned by project", since = "4.2.0")
    private Long cpuTotal;

    @SerializedName("cpuavailable")
    @Param(description = "the total number of cpu cores available to be created for this project", since = "4.2.0")
    private String cpuAvailable;

    @SerializedName("memorylimit")
    @Param(description = "the total memory (in MB) the project can own", since = "4.2.0")
    private String memoryLimit;

    @SerializedName("memorytotal")
    @Param(description = "the total memory (in MB) owned by project", since = "4.2.0")
    private Long memoryTotal;

    @SerializedName("memoryavailable")
    @Param(description = "the total memory (in MB) available to be created for this project", since = "4.2.0")
    private String memoryAvailable;

    @SerializedName("primarystoragelimit")
    @Param(description = "the total primary storage space (in GiB) the project can own", since = "4.2.0")
    private String primaryStorageLimit;

    @SerializedName("primarystoragetotal")
    @Param(description = "the total primary storage space (in GiB) owned by project", since = "4.2.0")
    private Long primaryStorageTotal;

    @SerializedName("primarystorageavailable")
    @Param(description = "the total primary storage space (in GiB) available to be used for this project", since = "4.2.0")
    private String primaryStorageAvailable;

    @SerializedName("secondarystoragelimit")
    @Param(description = "the total secondary storage space (in GiB) the project can own", since = "4.2.0")
    private String secondaryStorageLimit;

    @SerializedName("secondarystoragetotal")
    @Param(description = "the total secondary storage space (in GiB) owned by project", since = "4.2.0")
    private Long secondaryStorageTotal;

    @SerializedName("secondarystorageavailable")
    @Param(description = "the total secondary storage space (in GiB) available to be used for this project", since = "4.2.0")
    private String secondaryStorageAvailable;

    @SerializedName(ApiConstants.VM_LIMIT)
    @Param(description = "the total number of virtual machines that can be deployed by this project", since = "4.2.0")
    private String vmLimit;

    @SerializedName(ApiConstants.VM_TOTAL)
    @Param(description = "the total number of virtual machines deployed by this project", since = "4.2.0")
    private Long vmTotal;

    @SerializedName(ApiConstants.VM_AVAILABLE)
    @Param(description = "the total number of virtual machines available for this project to acquire", since = "4.2.0")
    private String vmAvailable;

    @SerializedName(ApiConstants.IP_LIMIT)
    @Param(description = "the total number of public ip addresses this project can acquire", since = "4.2.0")
    private String ipLimit;

    @SerializedName(ApiConstants.IP_TOTAL)
    @Param(description = "the total number of public ip addresses allocated for this project", since = "4.2.0")
    private Long ipTotal;

    @SerializedName(ApiConstants.IP_AVAILABLE)
    @Param(description = "the total number of public ip addresses available for this project to acquire", since = "4.2.0")
    private String ipAvailable;

    @SerializedName("volumelimit")
    @Param(description = "the total volume which can be used by this project", since = "4.2.0")
    private String volumeLimit;

    @SerializedName("volumetotal")
    @Param(description = "the total volume being used by this project", since = "4.2.0")
    private Long volumeTotal;

    @SerializedName("volumeavailable")
    @Param(description = "the total volume available for this project", since = "4.2.0")
    private String volumeAvailable;

    @SerializedName("snapshotlimit")
    @Param(description = "the total number of snapshots which can be stored by this project", since = "4.2.0")
    private String snapshotLimit;

    @SerializedName("snapshottotal")
    @Param(description = "the total number of snapshots stored by this project", since = "4.2.0")
    private Long snapshotTotal;

    @SerializedName("snapshotavailable")
    @Param(description = "the total number of snapshots available for this project", since = "4.2.0")
    private String snapshotAvailable;

    @SerializedName("templatelimit")
    @Param(description = "the total number of templates which can be created by this project", since = "4.2.0")
    private String templateLimit;

    @SerializedName("templatetotal")
    @Param(description = "the total number of templates which have been created by this project", since = "4.2.0")
    private Long templateTotal;

    @SerializedName("templateavailable")
    @Param(description = "the total number of templates available to be created by this project", since = "4.2.0")
    private String templateAvailable;

    @SerializedName("vmstopped")
    @Param(description = "the total number of virtual machines stopped for this project", since = "4.2.0")
    private Integer vmStopped;

    @SerializedName("vmrunning")
    @Param(description = "the total number of virtual machines running for this project", since = "4.2.0")
    private Integer vmRunning;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplaytext(final String displaytext) {
        this.displaytext = displaytext;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public void setOwner(final String owner) {
        ownerName = owner;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void addTag(final ResourceTagResponse tag) {
        tags.add(tag);
    }

    @Override
    public void setNetworkLimit(final String networkLimit) {
        this.networkLimit = networkLimit;
    }

    @Override
    public void setNetworkTotal(final Long networkTotal) {
        this.networkTotal = networkTotal;
    }

    @Override
    public void setNetworkAvailable(final String networkAvailable) {
        this.networkAvailable = networkAvailable;
    }

    @Override
    public void setVpcLimit(final String vpcLimit) {
        this.vpcLimit = networkLimit;
    }

    @Override
    public void setVpcTotal(final Long vpcTotal) {
        this.vpcTotal = vpcTotal;
    }

    @Override
    public void setVpcAvailable(final String vpcAvailable) {
        this.vpcAvailable = vpcAvailable;
    }

    @Override
    public void setCpuLimit(final String cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    @Override
    public void setCpuTotal(final Long cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    @Override
    public void setCpuAvailable(final String cpuAvailable) {
        this.cpuAvailable = cpuAvailable;
    }

    @Override
    public void setMemoryLimit(final String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    @Override
    public void setMemoryTotal(final Long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    @Override
    public void setMemoryAvailable(final String memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }

    @Override
    public void setPrimaryStorageLimit(final String primaryStorageLimit) {
        this.primaryStorageLimit = primaryStorageLimit;
    }

    @Override
    public void setPrimaryStorageTotal(final Long primaryStorageTotal) {
        this.primaryStorageTotal = primaryStorageTotal;
    }

    @Override
    public void setPrimaryStorageAvailable(final String primaryStorageAvailable) {
        this.primaryStorageAvailable = primaryStorageAvailable;
    }

    @Override
    public void setSecondaryStorageLimit(final String secondaryStorageLimit) {
        this.secondaryStorageLimit = secondaryStorageLimit;
    }

    @Override
    public void setSecondaryStorageTotal(final Long secondaryStorageTotal) {
        this.secondaryStorageTotal = secondaryStorageTotal;
    }

    @Override
    public void setSecondaryStorageAvailable(final String secondaryStorageAvailable) {
        this.secondaryStorageAvailable = secondaryStorageAvailable;
    }

    @Override
    public void setVmLimit(final String vmLimit) {
        this.vmLimit = vmLimit;
    }

    @Override
    public void setVmTotal(final Long vmTotal) {
        this.vmTotal = vmTotal;
    }

    @Override
    public void setVmAvailable(final String vmAvailable) {
        this.vmAvailable = vmAvailable;
    }

    @Override
    public void setIpLimit(final String ipLimit) {
        this.ipLimit = ipLimit;
    }

    @Override
    public void setIpTotal(final Long ipTotal) {
        this.ipTotal = ipTotal;
    }

    @Override
    public void setIpAvailable(final String ipAvailable) {
        this.ipAvailable = ipAvailable;
    }

    @Override
    public void setVolumeLimit(final String volumeLimit) {
        this.volumeLimit = volumeLimit;
    }

    @Override
    public void setVolumeTotal(final Long volumeTotal) {
        this.volumeTotal = volumeTotal;
    }

    @Override
    public void setVolumeAvailable(final String volumeAvailable) {
        this.volumeAvailable = volumeAvailable;
    }

    @Override
    public void setSnapshotLimit(final String snapshotLimit) {
        this.snapshotLimit = snapshotLimit;
    }

    @Override
    public void setSnapshotTotal(final Long snapshotTotal) {
        this.snapshotTotal = snapshotTotal;
    }

    @Override
    public void setSnapshotAvailable(final String snapshotAvailable) {
        this.snapshotAvailable = snapshotAvailable;
    }

    @Override
    public void setTemplateLimit(final String templateLimit) {
        this.templateLimit = templateLimit;
    }

    @Override
    public void setTemplateTotal(final Long templateTotal) {
        this.templateTotal = templateTotal;
    }

    @Override
    public void setTemplateAvailable(final String templateAvailable) {
        this.templateAvailable = templateAvailable;
    }

    @Override
    public void setVmStopped(final Integer vmStopped) {
        this.vmStopped = vmStopped;
    }

    @Override
    public void setVmRunning(final Integer vmRunning) {
        this.vmRunning = vmRunning;
    }
}
