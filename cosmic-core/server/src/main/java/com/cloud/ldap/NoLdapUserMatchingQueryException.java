package com.cloud.ldap;

public class NoLdapUserMatchingQueryException extends Exception {
    private final String query;

    public NoLdapUserMatchingQueryException(final String query) {
        super("No users matching: " + query);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
