package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.domain.Domain;
import com.cloud.event.EventTypes;
import com.cloud.network.VpnUser;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.VpnUsersResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addVpnUser", description = "Adds vpn users", responseObject = VpnUsersResponse.class, entityType = {VpnUser.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddVpnUserCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddVpnUserCmd.class.getName());

    private static final String s_name = "addvpnuserresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, required = true, description = "username for the vpn user")
    private String userName;

    @Parameter(name = ApiConstants.PASSWORD, type = CommandType.STRING, required = true, description = "password for the username")
    private String password;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "an optional account for the vpn user. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "add vpn user to the specific project")
    private Long projectId;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "an optional domainId for the vpn user. If the account parameter is used, domainId must also be used.")
    private Long domainId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getPassword() {
        return password;
    }

    public Long getProjectId() {
        return projectId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VPN_USER_ADD;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Add Remote Access VPN user for account " + getEntityOwnerId() + " username= " + getUserName();
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void execute() {
        final VpnUser vpnUser = _entityMgr.findById(VpnUser.class, getEntityId());
        final Account account = _entityMgr.findById(Account.class, vpnUser.getAccountId());
        if (!_ravService.applyVpnUsers(vpnUser.getAccountId(), userName)) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add vpn user");
        }

        final VpnUsersResponse vpnResponse = new VpnUsersResponse();
        vpnResponse.setId(vpnUser.getUuid());
        vpnResponse.setUserName(vpnUser.getUsername());
        vpnResponse.setAccountName(account.getAccountName());

        final Domain domain = _entityMgr.findById(Domain.class, account.getDomainId());
        if (domain != null) {
            vpnResponse.setDomainId(domain.getUuid());
            vpnResponse.setDomainName(domain.getName());
        }

        vpnResponse.setResponseName(getCommandName());
        vpnResponse.setObjectName("vpnuser");
        setResponseObject(vpnResponse);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Long accountId = _accountService.finalyzeAccountId(accountName, domainId, projectId, true);
        if (accountId == null) {
            return CallContext.current().getCallingAccount().getId();
        }

        return accountId;
    }

    @Override
    public void create() {
        final Account owner = _accountService.getAccount(getEntityOwnerId());

        final VpnUser vpnUser = _ravService.addVpnUser(owner.getId(), userName, password);
        if (vpnUser == null) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add vpn user");
        }
        setEntityId(vpnUser.getId());
        setEntityUuid(vpnUser.getUuid());
    }
}
