package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.user.Account;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Account.class)
public class AccountResponse extends BaseResponse implements ResourceLimitAndCountResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the account")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the account")
    private String name;

    @SerializedName(ApiConstants.ACCOUNT_TYPE)
    @Param(description = "account type (admin, domain-admin, user)")
    private Short accountType;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "id of the Domain the account belongs too")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "name of the Domain the account belongs too")
    private String domainName;

    @SerializedName(ApiConstants.DEFAULT_ZONE_ID)
    @Param(description = "the default zone of the account")
    private String defaultZoneId;

    @SerializedName(ApiConstants.RECEIVED_BYTES)
    @Param(description = "the total number of network traffic bytes received")
    private Long bytesReceived;

    @SerializedName(ApiConstants.SENT_BYTES)
    @Param(description = "the total number of network traffic bytes sent")
    private Long bytesSent;

    @SerializedName(ApiConstants.VM_LIMIT)
    @Param(description = "the total number of virtual machines that can be deployed by this account")
    private String vmLimit;

    @SerializedName(ApiConstants.VM_TOTAL)
    @Param(description = "the total number of virtual machines deployed by this account")
    private Long vmTotal;

    @SerializedName(ApiConstants.VM_AVAILABLE)
    @Param(description = "the total number of virtual machines available for this account to acquire")
    private String vmAvailable;

    @SerializedName(ApiConstants.IP_LIMIT)
    @Param(description = "the total number of public ip addresses this account can acquire")
    private String ipLimit;

    @SerializedName(ApiConstants.IP_TOTAL)
    @Param(description = "the total number of public ip addresses allocated for this account")
    private Long ipTotal;

    @SerializedName(ApiConstants.IP_AVAILABLE)
    @Param(description = "the total number of public ip addresses available for this account to acquire")
    private String ipAvailable;

    @SerializedName("volumelimit")
    @Param(description = "the total volume which can be used by this account")
    private String volumeLimit;

    @SerializedName("volumetotal")
    @Param(description = "the total volume being used by this account")
    private Long volumeTotal;

    @SerializedName("volumeavailable")
    @Param(description = "the total volume available for this account")
    private String volumeAvailable;

    @SerializedName("snapshotlimit")
    @Param(description = "the total number of snapshots which can be stored by this account")
    private String snapshotLimit;

    @SerializedName("snapshottotal")
    @Param(description = "the total number of snapshots stored by this account")
    private Long snapshotTotal;

    @SerializedName("snapshotavailable")
    @Param(description = "the total number of snapshots available for this account")
    private String snapshotAvailable;

    @SerializedName("templatelimit")
    @Param(description = "the total number of templates which can be created by this account")
    private String templateLimit;

    @SerializedName("templatetotal")
    @Param(description = "the total number of templates which have been created by this account")
    private Long templateTotal;

    @SerializedName("templateavailable")
    @Param(description = "the total number of templates available to be created by this account")
    private String templateAvailable;

    @SerializedName("vmstopped")
    @Param(description = "the total number of virtual machines stopped for this account")
    private Integer vmStopped;

    @SerializedName("vmrunning")
    @Param(description = "the total number of virtual machines running for this account")
    private Integer vmRunning;

    @SerializedName("projectlimit")
    @Param(description = "the total number of projects the account can own", since = "3.0.1")
    private String projectLimit;

    @SerializedName("projecttotal")
    @Param(description = "the total number of projects being administrated by this account", since = "3.0.1")
    private Long projectTotal;

    @SerializedName("projectavailable")
    @Param(description = "the total number of projects available for administration by this account", since = "3.0.1")
    private String projectAvailable;

    @SerializedName("networklimit")
    @Param(description = "the total number of networks the account can own", since = "3.0.1")
    private String networkLimit;

    @SerializedName("networktotal")
    @Param(description = "the total number of networks owned by account", since = "3.0.1")
    private Long networkTotal;

    @SerializedName("networkavailable")
    @Param(description = "the total number of networks available to be created for this account", since = "3.0.1")
    private String networkAvailable;

    @SerializedName("vpclimit")
    @Param(description = "the total number of vpcs the account can own", since = "4.0.0")
    private String vpcLimit;

    @SerializedName("vpctotal")
    @Param(description = "the total number of vpcs owned by account", since = "4.0.0")
    private Long vpcTotal;

    @SerializedName("vpcavailable")
    @Param(description = "the total number of vpcs available to be created for this account", since = "4.0.0")
    private String vpcAvailable;

    @SerializedName("cpulimit")
    @Param(description = "the total number of cpu cores the account can own", since = "4.2.0")
    private String cpuLimit;

    @SerializedName("cputotal")
    @Param(description = "the total number of cpu cores owned by account", since = "4.2.0")
    private Long cpuTotal;

    @SerializedName("cpuavailable")
    @Param(description = "the total number of cpu cores available to be created for this account", since = "4.2.0")
    private String cpuAvailable;

    @SerializedName("memorylimit")
    @Param(description = "the total memory (in MB) the account can own", since = "4.2.0")
    private String memoryLimit;

    @SerializedName("memorytotal")
    @Param(description = "the total memory (in MB) owned by account", since = "4.2.0")
    private Long memoryTotal;

    @SerializedName("memoryavailable")
    @Param(description = "the total memory (in MB) available to be created for this account", since = "4.2.0")
    private String memoryAvailable;

    @SerializedName("primarystoragelimit")
    @Param(description = "the total primary storage space (in GiB) the account can own", since = "4.2.0")
    private String primaryStorageLimit;

    @SerializedName("primarystoragetotal")
    @Param(description = "the total primary storage space (in GiB) owned by account", since = "4.2.0")
    private Long primaryStorageTotal;

    @SerializedName("primarystorageavailable")
    @Param(description = "the total primary storage space (in GiB) available to be used for this account", since = "4.2.0")
    private String primaryStorageAvailable;

    @SerializedName("secondarystoragelimit")
    @Param(description = "the total secondary storage space (in GiB) the account can own", since = "4.2.0")
    private String secondaryStorageLimit;

    @SerializedName("secondarystoragetotal")
    @Param(description = "the total secondary storage space (in GiB) owned by account", since = "4.2.0")
    private Long secondaryStorageTotal;

    @SerializedName("secondarystorageavailable")
    @Param(description = "the total secondary storage space (in GiB) available to be used for this account", since = "4.2.0")
    private String secondaryStorageAvailable;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the account")
    private String state;

    @SerializedName(ApiConstants.IS_CLEANUP_REQUIRED)
    @Param(description = "true if the account requires cleanup")
    private Boolean cleanupRequired;

    @SerializedName("user")
    @Param(description = "the list of users associated with account", responseObject = UserResponse.class)
    private List<UserResponse> users;

    @SerializedName(ApiConstants.NETWORK_DOMAIN)
    @Param(description = "the network domain")
    private String networkDomain;

    @SerializedName(ApiConstants.ACCOUNT_DETAILS)
    @Param(description = "details for the account")
    private Map<String, String> details;

    @SerializedName(ApiConstants.IS_DEFAULT)
    @Param(description = "true if account is default, false otherwise", since = "4.2.0")
    private Boolean isDefault;

    @Override
    public String getObjectId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAccountType(final Short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setBytesReceived(final Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public void setBytesSent(final Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setCleanupRequired(final Boolean cleanupRequired) {
        this.cleanupRequired = cleanupRequired;
    }

    public void setUsers(final List<UserResponse> users) {
        this.users = users;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public void setProjectLimit(final String projectLimit) {
        this.projectLimit = projectLimit;
    }

    public void setProjectTotal(final Long projectTotal) {
        this.projectTotal = projectTotal;
    }

    public void setProjectAvailable(final String projectAvailable) {
        this.projectAvailable = projectAvailable;
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

    public void setDefaultZone(final String defaultZoneId) {
        this.defaultZoneId = defaultZoneId;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
