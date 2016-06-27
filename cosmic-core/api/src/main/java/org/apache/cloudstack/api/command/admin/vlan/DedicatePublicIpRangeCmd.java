package org.apache.cloudstack.api.command.admin.vlan;

import com.cloud.dc.Vlan;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.VlanIpRangeResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "dedicatePublicIpRange", description = "Dedicates a Public IP range to an account", responseObject = VlanIpRangeResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DedicatePublicIpRangeCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DedicatePublicIpRangeCmd.class.getName());

    private static final String s_name = "dedicatepubliciprangeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VlanIpRangeResponse.class, required = true, description = "the id of the VLAN IP range")
    private Long id;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "account who will own the VLAN")
    private String accountName;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "project who will own the VLAN")
    private Long projectId;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            required = true,
            description = "domain ID of the account owning a VLAN")
    private Long domainId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getProjectId() {
        return projectId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, ResourceAllocationException {
        final Vlan result = _configService.dedicatePublicIpRange(this);
        if (result != null) {
            final VlanIpRangeResponse response = _responseGenerator.createVlanIpRangeResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to dedicate vlan ip range");
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
