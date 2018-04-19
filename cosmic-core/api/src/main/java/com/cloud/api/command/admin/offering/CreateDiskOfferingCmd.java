package com.cloud.api.command.admin.offering;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createDiskOffering", group = APICommandGroup.DiskOfferingService, description = "Creates a disk offering.", responseObject = DiskOfferingResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateDiskOfferingCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateDiskOfferingCmd.class.getName());

    private static final String s_name = "creatediskofferingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.STORAGE_TYPE, type = CommandType.STRING, description = "the storage type of the disk offering. Values are local and shared.")
    private final String storageType = ServiceOffering.StorageType.shared.toString();
    @Parameter(name = ApiConstants.PROVISIONINGTYPE,
            type = CommandType.STRING,
            description = "provisioning type used to create volumes. Valid values are thin, sparse, fat.")
    private final String provisioningType = ProvisioningType.THIN.toString();
    @Parameter(name = ApiConstants.DISK_SIZE, type = CommandType.LONG, required = false, description = "size of the disk offering in GB (1GB = 1,073,741,824 bytes)")
    private Long diskSize;
    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, required = true, description = "alternate display text of the disk offering", length = 4096)
    private String displayText;
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "name of the disk offering")
    private String offeringName;
    @Parameter(name = ApiConstants.TAGS, type = CommandType.STRING, description = "tags for the disk offering", length = 4096)
    private String tags;
    @Parameter(name = ApiConstants.CUSTOMIZED, type = CommandType.BOOLEAN, description = "whether disk offering size is custom or not")
    private Boolean customized;
    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "the ID of the containing domain, null for public offerings")
    private Long domainId;
    @Parameter(name = ApiConstants.DISPLAY_OFFERING,
            type = CommandType.BOOLEAN,
            description = "an optional field, whether to display the offering to the end user or not.")
    private Boolean displayOffering;

    @Parameter(name = ApiConstants.BYTES_READ_RATE, type = CommandType.LONG, required = false, description = "bytes read rate of the disk offering")
    private Long bytesReadRate;

    @Parameter(name = ApiConstants.BYTES_WRITE_RATE, type = CommandType.LONG, required = false, description = "bytes write rate of the disk offering")
    private Long bytesWriteRate;

    @Parameter(name = ApiConstants.IOPS_READ_RATE, type = CommandType.LONG, required = false, description = "io requests read rate per GB of the disk offering")
    private Long iopsReadRate;

    @Parameter(name = ApiConstants.IOPS_WRITE_RATE, type = CommandType.LONG, required = false, description = "io requests write rate per GB of the disk offering")
    private Long iopsWriteRate;

    @Parameter(name = ApiConstants.IOPS_TOTAL_RATE, type = CommandType.LONG, required = false, description = "io requests generic total rate per GB of the disk offering")
    private Long iopsTotalRate;

    @Parameter(name = ApiConstants.HYPERVISOR_SNAPSHOT_RESERVE,
            type = CommandType.INTEGER,
            required = false,
            description = "Hypervisor snapshot reserve space as a percent of a volume (for managed storage using Xen)")
    private Integer hypervisorSnapshotReserve;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getDiskSize() {
        return diskSize;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getOfferingName() {
        return offeringName;
    }

    public String getTags() {
        return tags;
    }

    public Boolean isCustomized() {
        return customized;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getBytesReadRate() {
        return bytesReadRate;
    }

    public Long getBytesWriteRate() {
        return bytesWriteRate;
    }

    public Long getIopsReadRate() {
        return iopsReadRate;
    }

    public Long getIopsWriteRate() {
        return iopsWriteRate;
    }

    public Long getIopsTotalRate() {
        return iopsTotalRate;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getProvisioningType() {
        return provisioningType;
    }

    public Boolean getDisplayOffering() {
        return displayOffering;
    }

    public Integer getHypervisorSnapshotReserve() {
        return hypervisorSnapshotReserve;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final DiskOffering offering = _configService.createDiskOffering(this);
        if (offering != null) {
            final DiskOfferingResponse response = _responseGenerator.createDiskOfferingResponse(offering);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create disk offering");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
