package com.cloud.ldap;

import com.cloud.api.command.LdapListConfigurationCmd;
import com.cloud.api.response.LdapConfigurationResponse;
import com.cloud.api.response.LdapUserResponse;
import com.cloud.api.response.LinkDomainToLdapResponse;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.utils.component.PluggableService;

import java.util.List;

public interface LdapManager extends PluggableService {

    LdapConfigurationResponse addConfiguration(String hostname, int port) throws InvalidParameterValueException;

    boolean canAuthenticate(String principal, String password);

    LdapConfigurationResponse createLdapConfigurationResponse(LdapConfigurationVO configuration);

    LdapUserResponse createLdapUserResponse(LdapUser user);

    LdapConfigurationResponse deleteConfiguration(String hostname) throws InvalidParameterValueException;

    LdapUser getUser(final String username) throws NoLdapUserMatchingQueryException;

    LdapUser getUser(String username, String type, String name) throws NoLdapUserMatchingQueryException;

    List<LdapUser> getUsers() throws NoLdapUserMatchingQueryException;

    List<LdapUser> getUsersInGroup(String groupName) throws NoLdapUserMatchingQueryException;

    boolean isLdapEnabled();

    Pair<List<? extends LdapConfigurationVO>, Integer> listConfigurations(LdapListConfigurationCmd cmd);

    List<LdapUser> searchUsers(String query) throws NoLdapUserMatchingQueryException;

    LinkDomainToLdapResponse linkDomainToLdap(Long domainId, String type, String name, short accountType);

    LinkDomainToLdapResponse listLinkDomainToLdap(Long domainId);

    public LdapTrustMapVO getDomainLinkedToLdap(long domainId);

    enum LinkType {GROUP, OU}
}
