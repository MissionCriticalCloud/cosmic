package org.apache.cloudstack.ldap;

public class NoLdapUserMatchingQueryException extends Exception {
    private static final long serialVersionUID = 7124360347208388174L;

    private final String query;

    public NoLdapUserMatchingQueryException(final String query) {
        super("No users matching: " + query);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
