package org.apache.cloudstack.ldap;

import com.cloud.server.auth.UserAuthenticator;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import com.cloud.user.dao.UserAccountDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapAuthenticator extends AdapterBase implements UserAuthenticator {
    private static final Logger s_logger = LoggerFactory.getLogger(LdapAuthenticator.class.getName());

    @Inject
    private LdapManager _ldapManager;
    @Inject
    private UserAccountDao _userAccountDao;
    @Inject
    private AccountManager _accountManager;

    private String ldapGroupName;

    public LdapAuthenticator() {
        super();
    }

    public LdapAuthenticator(final LdapManager ldapManager, final UserAccountDao userAccountDao) {
        super();
        _ldapManager = ldapManager;
        _userAccountDao = userAccountDao;
    }

    @Override
    public Pair<Boolean, ActionOnFailedAuthentication> authenticate(final String username, final String password, final Long domainId,
                                                                    final Map<String, Object[]> requestParameters) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            s_logger.debug("Username or Password cannot be empty");
            return new Pair<>(false, null);
        }

        boolean result = false;
        ActionOnFailedAuthentication action = null;

        if (_ldapManager.isLdapEnabled()) {
            final UserAccount user = _userAccountDao.getUserAccount(username, domainId);
            final LdapTrustMapVO ldapTrustMapVO = _ldapManager.getDomainLinkedToLdap(domainId);
            if (ldapTrustMapVO != null) {
                ldapGroupName = DistinguishedNameParser.parseLeafName(ldapTrustMapVO.getName());
                try {
                    final LdapUser ldapUser = _ldapManager.getUser(username, ldapTrustMapVO.getType().toString(), ldapTrustMapVO.getName());
                    if (!ldapUser.isDisabled()) {
                        result = _ldapManager.canAuthenticate(ldapUser.getPrincipal(), password);
                        if (result) {
                            if (user == null) {
                                // import user to cloudstack
                                createCloudStackUserAccount(ldapUser, domainId, ldapTrustMapVO.getAccountType());
                            } else {
                                enableUserInCloudStack(user);
                            }
                        }
                    } else {
                        // disable user in cloudstack
                        disableUserInCloudStack(user);
                    }
                } catch (final NoLdapUserMatchingQueryException e) {
                    s_logger.debug(e.getMessage());
                }
            } else {
                // domain is not linked to ldap follow normal authentication
                if (user != null) {
                    try {
                        final LdapUser ldapUser = _ldapManager.getUser(username);
                        if (!ldapUser.isDisabled()) {
                            result = _ldapManager.canAuthenticate(ldapUser.getPrincipal(), password);
                        } else {
                            s_logger.debug("user with principal " + ldapUser.getPrincipal() + " is disabled in ldap");
                        }
                    } catch (final NoLdapUserMatchingQueryException e) {
                        s_logger.debug(e.getMessage());
                    }
                }
            }
            if (!result && user != null) {
                action = ActionOnFailedAuthentication.INCREMENT_INCORRECT_LOGIN_ATTEMPT_COUNT;
            }
        }

        return new Pair<>(result, action);
    }

    private void createCloudStackUserAccount(final LdapUser user, final long domainId, final short accountType) {
        final String username = user.getUsername();
        final Account account = _accountManager.getActiveAccountByName(ldapGroupName, domainId);
        if (account == null) {
            s_logger.info("Account (" + ldapGroupName + ") for LDAP group does not exist. Creating account and user (" + username + ").");
            _accountManager.createUserAccount(username, "", user.getFirstname(), user.getLastname(), user.getEmail(), null, ldapGroupName, accountType, domainId,
                    null, null, UUID.randomUUID().toString(), UUID.randomUUID().toString(), User.Source.LDAP);
        } else {
            s_logger.debug("Account (" + ldapGroupName + ") for LDAP group already exists. Creating user (" + username + ").");
            _accountManager.createUser(username, "", user.getFirstname(), user.getLastname(), user.getEmail(), null, ldapGroupName, domainId,
                    UUID.randomUUID().toString(), User.Source.LDAP);
        }
    }

    private void enableUserInCloudStack(final UserAccount user) {
        if (user != null && user.getState().equalsIgnoreCase(Account.State.disabled.toString())) {
            _accountManager.enableUser(user.getId());
        }
    }

    private void disableUserInCloudStack(final UserAccount user) {
        if (user != null) {
            _accountManager.disableUser(user.getId());
        }
    }

    @Override
    public String encode(final String password) {
        return password;
    }
}
