package org.apache.cloudstack.api.command.user.vmsnapshot;

import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListTaggedResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VMSnapshotResponse;

import java.util.ArrayList;
import java.util.List;

@APICommand(name = "listVMSnapshot", description = "List virtual machine snapshot by conditions", responseObject = VMSnapshotResponse.class, since = "4.2.0", entityType =
        {VMSnapshot.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVMSnapshotCmd extends BaseListTaggedResourcesCmd {

    private static final String s_name = "listvmsnapshotresponse";

    @Parameter(name = ApiConstants.VM_SNAPSHOT_ID, type = CommandType.UUID, entityType = VMSnapshotResponse.class, description = "The ID of the VM snapshot")
    private Long id;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "state of the virtual machine snapshot")
    private String state;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class, description = "the ID of the vm")
    private Long vmId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "lists snapshot by snapshot name or display name")
    private String vmSnapshotName;

    public String getState() {
        return state;
    }

    public String getVmSnapshotName() {
        return vmSnapshotName;
    }

    public Long getVmId() {
        return vmId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        final List<? extends VMSnapshot> result = _vmSnapshotService.listVMSnapshots(this);
        final ListResponse<VMSnapshotResponse> response = new ListResponse<>();
        final List<VMSnapshotResponse> snapshotResponses = new ArrayList<>();
        for (final VMSnapshot r : result) {
            final VMSnapshotResponse vmSnapshotResponse = _responseGenerator.createVMSnapshotResponse(r);
            vmSnapshotResponse.setObjectName("vmSnapshot");
            snapshotResponses.add(vmSnapshotResponse);
        }
        response.setResponses(snapshotResponses);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
