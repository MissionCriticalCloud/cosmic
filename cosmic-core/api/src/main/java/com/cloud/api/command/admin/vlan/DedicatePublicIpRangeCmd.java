package com.cloud.api.command.admin.vlan;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.VlanIpRangeResponse;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.dc.Vlan;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "dedicatePublicIpRange", group = APICommandGroup.NetworkService, description = "Dedicates a Public IP range to an account", responseObject = VlanIpRangeResponse.class,
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
