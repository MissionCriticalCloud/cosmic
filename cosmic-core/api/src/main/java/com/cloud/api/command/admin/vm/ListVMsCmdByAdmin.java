package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.vm.ListVMsCmd;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVirtualMachines", description = "List the virtual machines owned by the account.", responseObject = UserVmResponse.class, responseView = ResponseView
        .Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ListVMsCmdByAdmin extends ListVMsCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVMsCmdByAdmin.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class,
            description = "the host ID")
    private Long hostId;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class,
            description = "the pod ID")
    private Long podId;

    @Parameter(name = ApiConstants.STORAGE_ID, type = CommandType.UUID, entityType = StoragePoolResponse.class,
            description = "the storage ID where vm's volumes belong to")
    private Long storageId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getHostId() {
        return hostId;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getStorageId() {
        return storageId;
    }
}
