package org.apache.cloudstack.ldap;

public class NoSuchLdapUserException extends Exception {
    private static final long serialVersionUID = 6782938919658010900L;
    private final String username;

    public NoSuchLdapUserException(final String username) {
        super("No such user: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
