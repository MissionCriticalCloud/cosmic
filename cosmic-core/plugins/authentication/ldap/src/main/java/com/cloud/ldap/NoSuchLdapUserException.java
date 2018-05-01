package com.cloud.ldap;

public class NoSuchLdapUserException extends Exception {
    private final String username;

    public NoSuchLdapUserException(final String username) {
        super("No such user: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
