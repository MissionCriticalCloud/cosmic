package org.apache.cloudstack.ldap;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.util.List;

public interface LdapUserManager {

    public LdapUser getUser(final String username, final LdapContext context) throws NamingException, IOException;

    public LdapUser getUser(final String username, final String type, final String name, final LdapContext context) throws NamingException, IOException;

    public List<LdapUser> getUsers(final LdapContext context) throws NamingException, IOException;

    public List<LdapUser> getUsers(final String username, final LdapContext context) throws NamingException, IOException;

    public List<LdapUser> getUsersInGroup(String groupName, LdapContext context) throws NamingException;

    public List<LdapUser> searchUsers(final LdapContext context) throws NamingException, IOException;

    public List<LdapUser> searchUsers(final String username, final LdapContext context) throws NamingException, IOException;

    public enum Provider {
        MICROSOFTAD, OPENLDAP
    }
}
