package com.cloud.api.command.user.offering;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListDomainResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.UserVmResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listServiceOfferings", description = "Lists all available service offerings.", responseObject = ServiceOfferingResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListServiceOfferingsCmd extends BaseListDomainResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListServiceOfferingsCmd.class.getName());

    private static final String s_name = "listserviceofferingsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ServiceOfferingResponse.class, description = "ID of the service offering")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "name of the service offering")
    private String serviceOfferingName;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            description = "the ID of the virtual machine. Pass this in if you want to see the available service offering that a virtual machine can be changed to.")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.IS_SYSTEM_OFFERING, type = CommandType.BOOLEAN, description = "is this a system vm offering")
    private Boolean isSystem;

    @Parameter(name = ApiConstants.SYSTEM_VM_TYPE,
            type = CommandType.STRING,
            description = "the system VM type. Possible types are \"consoleproxy\", \"secondarystoragevm\" or \"domainrouter\".")
    private String systemVmType;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    public Boolean getIsSystem() {
        return isSystem == null ? false : isSystem;
    }

    public String getSystemVmType() {
        return systemVmType;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final ListResponse<ServiceOfferingResponse> response = _queryService.searchForServiceOfferings(this);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
