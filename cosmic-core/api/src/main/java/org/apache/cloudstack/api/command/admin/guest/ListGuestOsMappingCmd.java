package org.apache.cloudstack.api.command.admin.guest;

import com.cloud.storage.GuestOSHypervisor;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.GuestOSResponse;
import org.apache.cloudstack.api.response.GuestOsMappingResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listGuestOsMapping", description = "Lists all available OS mappings for given hypervisor", responseObject = GuestOsMappingResponse.class,
        since = "4.4.0", requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListGuestOsMappingCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListGuestOsMappingCmd.class.getName());

    private static final String s_name = "listguestosmappingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GuestOsMappingResponse.class, required = false, description = "list mapping by its UUID")
    private Long id;

    @Parameter(name = ApiConstants.OS_TYPE_ID, type = CommandType.UUID, entityType = GuestOSResponse.class, required = false, description = "list mapping by Guest OS Type UUID")
    private Long osTypeId;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, required = false, description = "list Guest OS mapping by hypervisor")
    private String hypervisor;

    @Parameter(name = ApiConstants.HYPERVISOR_VERSION, type = CommandType.STRING, required = false, description = "list Guest OS mapping by hypervisor version. Must be used with" +
            " hypervisor parameter")
    private String hypervisorVersion;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getOsTypeId() {
        return osTypeId;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends GuestOSHypervisor>, Integer> result = _mgr.listGuestOSMappingByCriteria(this);
        final ListResponse<GuestOsMappingResponse> response = new ListResponse<>();
        final List<GuestOsMappingResponse> osMappingResponses = new ArrayList<>();
        for (final GuestOSHypervisor guestOSHypervisor : result.first()) {
            final GuestOsMappingResponse guestOsMappingResponse = _responseGenerator.createGuestOSMappingResponse(guestOSHypervisor);
            osMappingResponses.add(guestOsMappingResponse);
        }

        response.setResponses(osMappingResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
