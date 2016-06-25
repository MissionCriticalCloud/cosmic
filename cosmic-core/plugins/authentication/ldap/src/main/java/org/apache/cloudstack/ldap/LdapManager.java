package org.apache.cloudstack.ldap;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.Pair;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.api.command.LdapListConfigurationCmd;
import org.apache.cloudstack.api.response.LdapConfigurationResponse;
import org.apache.cloudstack.api.response.LdapUserResponse;
import org.apache.cloudstack.api.response.LinkDomainToLdapResponse;

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

    public LdapTrustMapVO getDomainLinkedToLdap(long domainId);

    enum LinkType {GROUP, OU}
}
