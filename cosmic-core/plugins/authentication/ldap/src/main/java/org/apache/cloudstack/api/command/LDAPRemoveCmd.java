package org.apache.cloudstack.api.command;

import com.cloud.user.Account;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.response.LDAPConfigResponse;
import org.apache.cloudstack.api.response.LDAPRemoveResponse;
import org.apache.cloudstack.ldap.LdapConfigurationVO;
import org.apache.cloudstack.ldap.LdapManager;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated as of 4.3 use the new api {@link LdapDeleteConfigurationCmd}
 */
@Deprecated
@APICommand(name = "ldapRemove", description = "Remove the LDAP context for this site.", responseObject = LDAPConfigResponse.class, since = "3.0.1",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class LDAPRemoveCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(LDAPRemoveCmd.class.getName());
    private static final String s_name = "ldapremoveresponse";
    @Inject
    private LdapManager _ldapManager;

    @Override
    public void execute() {
        final boolean result = this.removeLDAP();
        if (result) {
            final LDAPRemoveResponse lr = new LDAPRemoveResponse();
            lr.setObjectName("ldapremove");
            lr.setResponseName(getCommandName());
            this.setResponseObject(lr);
        }
    }

    private boolean removeLDAP() {
        final LdapListConfigurationCmd listConfigurationCmd = new LdapListConfigurationCmd(_ldapManager);
        final Pair<List<? extends LdapConfigurationVO>, Integer> result = _ldapManager.listConfigurations(listConfigurationCmd);
        for (final LdapConfigurationVO config : result.first()) {
            _ldapManager.deleteConfiguration(config.getHostname());
        }
        return true;
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
