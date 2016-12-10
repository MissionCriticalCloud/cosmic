package com.cloud.api.command.user.volume;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.storage.Volume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVolumes", description = "Lists all volumes.", responseObject = VolumeResponse.class, responseView = ResponseView.Restricted, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVolumesCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVolumesCmd.class.getName());

    private static final String s_name = "listvolumesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class, description = "list volumes on specified host")
    private Long hostId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class, description = "the ID of the disk volume")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the disk volume")
    private String volumeName;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the pod id the disk volume belongs to")
    private Long podId;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "the type of disk volume")
    private String type;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class, description = "the ID of the virtual machine")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the ID of the availability zone")
    private Long zoneId;

    @Parameter(name = ApiConstants.STORAGE_ID,
            type = CommandType.UUID,
            entityType = StoragePoolResponse.class,
            description = "the ID of the storage pool, available to ROOT admin only",
            since = "4.3",
            authorized = {RoleType.Admin})
    private Long storageId;

    @Parameter(name = ApiConstants.DISK_OFFERING_ID,
            type = CommandType.UUID,
            entityType = DiskOfferingResponse.class,
            description = "list volumes by disk offering",
            since = "4.4")
    private Long diskOfferingId;

    @Parameter(name = ApiConstants.DISPLAY_VOLUME, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this " +
            "parameter", since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getHostId() {
        return hostId;
    }

    public Long getId() {
        return id;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    public String getType() {
        return type;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getStorageId() {
        return storageId;
    }

    @Override
    public Boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return super.getDisplay();
    }
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    @Override
    public void execute() {
        final ListResponse<VolumeResponse> response = _queryService.searchForVolumes(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
