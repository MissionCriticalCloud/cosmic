package com.cloud.api.command.admin.volume;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.volume.ListVolumesCmd;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.legacymodel.storage.Volume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVolumes", group = APICommandGroup.VolumeService, description = "Lists all volumes.", responseObject = VolumeResponse.class, responseView = ResponseView.Full, entityType =
        {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVolumesCmdByAdmin extends ListVolumesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVolumesCmdByAdmin.class.getName());

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class,
            description = "the pod id the disk volume belongs to")
    private Long podId;

    @Parameter(name = ApiConstants.STORAGE_ID, type = CommandType.UUID, entityType = StoragePoolResponse.class,
            description = "the ID of the storage pool, available to ROOT admin only", since = "4.3", authorized = {RoleType.Admin})
    private Long storageId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public Long getPodId() {
        return podId;
    }

    @Override
    public Long getStorageId() {
        return storageId;
    }
}
