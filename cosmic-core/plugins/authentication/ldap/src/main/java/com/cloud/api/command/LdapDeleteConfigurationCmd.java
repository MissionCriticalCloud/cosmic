package com.cloud.api.command;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.LdapConfigurationResponse;
import com.cloud.ldap.LdapManager;
import com.cloud.legacymodel.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteLdapConfiguration", group = APICommandGroup.AuthenticationService, description = "Remove an Ldap Configuration", responseObject = LdapConfigurationResponse.class, since =
        "4.2.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class LdapDeleteConfigurationCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(LdapDeleteConfigurationCmd.class.getName());
    private static final String s_name = "ldapconfigurationresponse";

    @Inject
    private LdapManager _ldapManager;

    @Parameter(name = "hostname", type = CommandType.STRING, required = true, description = "Hostname")
    private String hostname;

    public LdapDeleteConfigurationCmd() {
        super();
    }

    public LdapDeleteConfigurationCmd(final LdapManager ldapManager) {
        super();
        _ldapManager = ldapManager;
    }

    @Override
    public void execute() throws ServerApiException {
        try {
            final LdapConfigurationResponse response = _ldapManager.deleteConfiguration(hostname);
            response.setObjectName("LdapDeleteConfiguration");
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.toString());
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
