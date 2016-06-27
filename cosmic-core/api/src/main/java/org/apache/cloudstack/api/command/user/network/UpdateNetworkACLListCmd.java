package org.apache.cloudstack.api.command.user.network;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.NetworkACL;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.NetworkACLResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateNetworkACLList", description = "Updates network ACL list", responseObject = SuccessResponse.class, since = "4.4",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateNetworkACLListCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateNetworkACLListCmd.class.getName());
    private static final String s_name = "updatenetworkacllistresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = NetworkACLResponse.class, required = true, description = "the ID of the network ACL")
    private Long id;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the list to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ACL_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return ("Updating network ACL list ID=" + id);
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        final NetworkACL acl = _networkACLService.updateNetworkACL(id, this.getCustomId(), getDisplay());
        final NetworkACLResponse aclResponse = _responseGenerator.createNetworkACLResponse(acl);
        setResponseObject(aclResponse);
        aclResponse.setResponseName(getCommandName());
    }

    public Boolean getDisplay() {
        return display;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account caller = CallContext.current().getCallingAccount();
        return caller.getAccountId();
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), NetworkACL.class);
        }
    }
}
