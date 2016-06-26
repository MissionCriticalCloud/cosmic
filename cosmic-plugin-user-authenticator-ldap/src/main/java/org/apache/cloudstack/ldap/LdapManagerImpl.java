package org.apache.cloudstack.ldap;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.LdapValidator;
import org.apache.cloudstack.api.command.LDAPConfigCmd;
import org.apache.cloudstack.api.command.LDAPRemoveCmd;
import org.apache.cloudstack.api.command.LdapAddConfigurationCmd;
import org.apache.cloudstack.api.command.LdapCreateAccountCmd;
import org.apache.cloudstack.api.command.LdapDeleteConfigurationCmd;
import org.apache.cloudstack.api.command.LdapImportUsersCmd;
import org.apache.cloudstack.api.command.LdapListConfigurationCmd;
import org.apache.cloudstack.api.command.LdapListUsersCmd;
import org.apache.cloudstack.api.command.LdapUserSearchCmd;
import org.apache.cloudstack.api.command.LinkDomainToLdapCmd;
import org.apache.cloudstack.api.response.LdapConfigurationResponse;
import org.apache.cloudstack.api.response.LdapUserResponse;
import org.apache.cloudstack.api.response.LinkDomainToLdapResponse;
import org.apache.cloudstack.ldap.dao.LdapConfigurationDao;
import org.apache.cloudstack.ldap.dao.LdapTrustMapDao;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Local(value = LdapManager.class)
public class LdapManagerImpl implements LdapManager, LdapValidator {
    private static final Logger s_logger = LoggerFactory.getLogger(LdapManagerImpl.class.getName());
    @Inject
    LdapUserManagerFactory _ldapUserManagerFactory;
    @Inject
    LdapTrustMapDao _ldapTrustMapDao;
    @Inject
    private LdapConfigurationDao _ldapConfigurationDao;
    @Inject
    private LdapContextFactory _ldapContextFactory;
    @Inject
    private LdapConfiguration _ldapConfiguration;

    public LdapManagerImpl() {
        super();
    }

    public LdapManagerImpl(final LdapConfigurationDao ldapConfigurationDao, final LdapContextFactory ldapContextFactory, final LdapUserManagerFactory ldapUserManagerFactory,
                           final LdapConfiguration ldapConfiguration) {
        super();
        _ldapConfigurationDao = ldapConfigurationDao;
        _ldapContextFactory = ldapContextFactory;
        _ldapUserManagerFactory = ldapUserManagerFactory;
        _ldapConfiguration = ldapConfiguration;
    }

    @Override
    public LdapConfigurationResponse addConfiguration(final String hostname, final int port) throws InvalidParameterValueException {
        LdapConfigurationVO configuration = _ldapConfigurationDao.findByHostname(hostname);
        if (configuration == null) {
            LdapContext context = null;
            try {
                final String providerUrl = "ldap://" + hostname + ":" + port;
                context = _ldapContextFactory.createBindContext(providerUrl);
                configuration = new LdapConfigurationVO(hostname, port);
                _ldapConfigurationDao.persist(configuration);
                s_logger.info("Added new ldap server with hostname: " + hostname);
                return new LdapConfigurationResponse(hostname, port);
            } catch (NamingException | IOException e) {
                s_logger.debug("NamingException while doing an LDAP bind", e);
                throw new InvalidParameterValueException("Unable to bind to the given LDAP server");
            } finally {
                closeContext(context);
            }
        } else {
            throw new InvalidParameterValueException("Duplicate configuration");
        }
    }

    @Override
    public boolean canAuthenticate(final String principal, final String password) {
        try {
            final LdapContext context = _ldapContextFactory.createUserContext(principal, password);
            closeContext(context);
            return true;
        } catch (NamingException | IOException e) {
            s_logger.debug("Exception while doing an LDAP bind for user " + " " + principal, e);
            s_logger.info("Failed to authenticate user: " + principal + ". incorrect password.");
            return false;
        }
    }

    @Override
    public LdapConfigurationResponse createLdapConfigurationResponse(final LdapConfigurationVO configuration) {
        final LdapConfigurationResponse response = new LdapConfigurationResponse();
        response.setHostname(configuration.getHostname());
        response.setPort(configuration.getPort());
        return response;
    }

    @Override
    public LdapUserResponse createLdapUserResponse(final LdapUser user) {
        final LdapUserResponse response = new LdapUserResponse();
        response.setUsername(user.getUsername());
        response.setFirstname(user.getFirstname());
        response.setLastname(user.getLastname());
        response.setEmail(user.getEmail());
        response.setPrincipal(user.getPrincipal());
        response.setDomain(user.getDomain());
        return response;
    }

    @Override
    public LdapConfigurationResponse deleteConfiguration(final String hostname) throws InvalidParameterValueException {
        final LdapConfigurationVO configuration = _ldapConfigurationDao.findByHostname(hostname);
        if (configuration == null) {
            throw new InvalidParameterValueException("Cannot find configuration with hostname " + hostname);
        } else {
            _ldapConfigurationDao.remove(configuration.getId());
            s_logger.info("Removed ldap server with hostname: " + hostname);
            return new LdapConfigurationResponse(configuration.getHostname(), configuration.getPort());
        }
    }

    @Override
    public LdapUser getUser(final String username) throws NoLdapUserMatchingQueryException {
        LdapContext context = null;
        try {
            context = _ldapContextFactory.createBindContext();

            final String escapedUsername = LdapUtils.escapeLDAPSearchFilter(username);
            return _ldapUserManagerFactory.getInstance(_ldapConfiguration.getLdapProvider()).getUser(escapedUsername, context);
        } catch (NamingException | IOException e) {
            s_logger.debug("ldap Exception: ", e);
            throw new NoLdapUserMatchingQueryException("No Ldap User found for username: " + username);
        } finally {
            closeContext(context);
        }
    }

    @Override
    public LdapUser getUser(final String username, final String type, final String name) throws NoLdapUserMatchingQueryException {
        LdapContext context = null;
        try {
            context = _ldapContextFactory.createBindContext();
            final String escapedUsername = LdapUtils.escapeLDAPSearchFilter(username);
            return _ldapUserManagerFactory.getInstance(_ldapConfiguration.getLdapProvider()).getUser(escapedUsername, type, name, context);
        } catch (NamingException | IOException e) {
            s_logger.debug("ldap Exception: ", e);
            throw new NoLdapUserMatchingQueryException("No Ldap User found for username: " + username + "name: " + name + "of type: " + type);
        } finally {
            closeContext(context);
        }
    }

    @Override
    public List<LdapUser> getUsers() throws NoLdapUserMatchingQueryException {
        LdapContext context = null;
        try {
            context = _ldapContextFactory.createBindContext();
            return _ldapUserManagerFactory.getInstance(_ldapConfiguration.getLdapProvider()).getUsers(context);
        } catch (NamingException | IOException e) {
            s_logger.debug("ldap Exception: ", e);
            throw new NoLdapUserMatchingQueryException("*");
        } finally {
            closeContext(context);
        }
    }

    @Override
    public List<LdapUser> getUsersInGroup(final String groupName) throws NoLdapUserMatchingQueryException {
        LdapContext context = null;
        try {
            context = _ldapContextFactory.createBindContext();
            return _ldapUserManagerFactory.getInstance(_ldapConfiguration.getLdapProvider()).getUsersInGroup(groupName, context);
        } catch (NamingException | IOException e) {
            s_logger.debug("ldap NamingException: ", e);
            throw new NoLdapUserMatchingQueryException("groupName=" + groupName);
        } finally {
            closeContext(context);
        }
    }

    @Override
    public boolean isLdapEnabled() {
        return listConfigurations(new LdapListConfigurationCmd(this)).second() > 0;
    }

    @Override
    public Pair<List<? extends LdapConfigurationVO>, Integer> listConfigurations(final LdapListConfigurationCmd cmd) {
        final String hostname = cmd.getHostname();
        final int port = cmd.getPort();
        final Pair<List<LdapConfigurationVO>, Integer> result = _ldapConfigurationDao.searchConfigurations(hostname, port);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    public List<LdapUser> searchUsers(final String username) throws NoLdapUserMatchingQueryException {
        LdapContext context = null;
        try {
            context = _ldapContextFactory.createBindContext();
            final String escapedUsername = LdapUtils.escapeLDAPSearchFilter(username);
            return _ldapUserManagerFactory.getInstance(_ldapConfiguration.getLdapProvider()).getUsers("*" + escapedUsername + "*", context);
        } catch (NamingException | IOException e) {
            s_logger.debug("ldap Exception: ", e);
            throw new NoLdapUserMatchingQueryException(username);
        } finally {
            closeContext(context);
        }
    }

    @Override
    public LinkDomainToLdapResponse linkDomainToLdap(final Long domainId, final String type, final String name, final short accountType) {
        Validate.notNull(type, "type cannot be null. It should either be GROUP or OU");
        Validate.notNull(domainId, "domainId cannot be null.");
        Validate.notEmpty(name, "GROUP or OU name cannot be empty");
        //Account type should be 0 or 2. check the constants in com.cloud.user.Account
        Validate.isTrue(accountType == 0 || accountType == 2, "accountype should be either 0(normal user) or 2(domain admin)");
        final LinkType linkType = LdapManager.LinkType.valueOf(type.toUpperCase());
        final LdapTrustMapVO vo = _ldapTrustMapDao.persist(new LdapTrustMapVO(domainId, linkType, name, accountType));
        final LinkDomainToLdapResponse response = new LinkDomainToLdapResponse(vo.getDomainId(), vo.getType().toString(), vo.getName(), vo.getAccountType());
        return response;
    }

    @Override
    public LdapTrustMapVO getDomainLinkedToLdap(final long domainId) {
        return _ldapTrustMapDao.findByDomainId(domainId);
    }

    private void closeContext(final LdapContext context) {
        try {
            if (context != null) {
                context.close();
            }
        } catch (final NamingException e) {
            s_logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public List<Class<?>> getCommands() {
        final List<Class<?>> cmdList = new ArrayList<>();
        cmdList.add(LdapUserSearchCmd.class);
        cmdList.add(LdapListUsersCmd.class);
        cmdList.add(LdapAddConfigurationCmd.class);
        cmdList.add(LdapDeleteConfigurationCmd.class);
        cmdList.add(LdapListConfigurationCmd.class);
        cmdList.add(LdapCreateAccountCmd.class);
        cmdList.add(LdapImportUsersCmd.class);
        cmdList.add(LDAPConfigCmd.class);
        cmdList.add(LDAPRemoveCmd.class);
        cmdList.add(LinkDomainToLdapCmd.class);
        return cmdList;
    }
}
