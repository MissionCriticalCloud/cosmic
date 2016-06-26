package org.apache.cloudstack.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADLdapUserManagerImpl extends OpenLdapUserManagerImpl implements LdapUserManager {
    public static final Logger s_logger = LoggerFactory.getLogger(ADLdapUserManagerImpl.class.getName());
    private static final String MICROSOFT_AD_NESTED_MEMBERS_FILTER = "memberOf:1.2.840.113556.1.4.1941:";
    private static final String MICROSOFT_AD_MEMBERS_FILTER = "memberOf";

    @Override
    public List<LdapUser> getUsersInGroup(final String groupName, final LdapContext context) throws NamingException {
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("ldap group name cannot be blank");
        }

        final String basedn = _ldapConfiguration.getBaseDn();
        if (StringUtils.isBlank(basedn)) {
            throw new IllegalArgumentException("ldap basedn is not configured");
        }

        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(_ldapConfiguration.getScope());
        searchControls.setReturningAttributes(_ldapConfiguration.getReturnAttributes());

        final NamingEnumeration<SearchResult> results = context.search(basedn, generateADGroupSearchFilter(groupName), searchControls);
        final List<LdapUser> users = new ArrayList<>();
        while (results.hasMoreElements()) {
            final SearchResult result = results.nextElement();
            users.add(createUser(result));
        }
        return users;
    }

    private String generateADGroupSearchFilter(final String groupName) {
        final StringBuilder userObjectFilter = new StringBuilder();
        userObjectFilter.append("(objectClass=");
        userObjectFilter.append(_ldapConfiguration.getUserObject());
        userObjectFilter.append(")");

        final StringBuilder memberOfFilter = new StringBuilder();
        final String groupCnName = _ldapConfiguration.getCommonNameAttribute() + "=" + groupName + "," + _ldapConfiguration.getBaseDn();
        memberOfFilter.append("(").append(getMemberOfAttribute()).append("=");
        memberOfFilter.append(groupCnName);
        memberOfFilter.append(")");

        final StringBuilder result = new StringBuilder();
        result.append("(&");
        result.append(userObjectFilter);
        result.append(memberOfFilter);
        result.append(")");

        s_logger.debug("group search filter = " + result);
        return result.toString();
    }

    protected boolean isUserDisabled(final SearchResult result) throws NamingException {
        boolean isDisabledUser = false;
        final String userAccountControl = LdapUtils.getAttributeValue(result.getAttributes(), _ldapConfiguration.getUserAccountControlAttribute());
        if (userAccountControl != null) {
            final int control = Integer.parseInt(userAccountControl);
            // second bit represents disabled user flag in AD
            if ((control & 2) > 0) {
                isDisabledUser = true;
            }
        }
        return isDisabledUser;
    }

    protected String getMemberOfAttribute() {
        if (_ldapConfiguration.isNestedGroupsEnabled()) {
            return MICROSOFT_AD_NESTED_MEMBERS_FILTER;
        } else {
            return MICROSOFT_AD_MEMBERS_FILTER;
        }
    }
}
