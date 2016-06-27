package org.apache.cloudstack.api.command.user.affinitygroup;

import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.UserVmResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listAffinityGroups", description = "Lists affinity groups", responseObject = AffinityGroupResponse.class, entityType = {AffinityGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListAffinityGroupsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListAffinityGroupsCmd.class.getName());

    private static final String s_name = "listaffinitygroupsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "lists affinity groups by name")
    private String affinityGroupName;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            description = "lists affinity groups by virtual machine ID",
            entityType = UserVmResponse.class)
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, description = "list the affinity group by the ID provided", entityType = AffinityGroupResponse.class)
    private Long id;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "lists affinity groups by type")
    private String affinityGroupType;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public String getAffinityGroupName() {
        return affinityGroupName;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    public Long getId() {
        return id;
    }

    public String getAffinityGroupType() {
        return affinityGroupType;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final ListResponse<AffinityGroupResponse> response = _queryService.searchForAffinityGroups(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AffinityGroup;
    }
}
