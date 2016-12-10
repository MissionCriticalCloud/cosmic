package org.apache.cloudstack.ldap;

public class LdapUser implements Comparable<LdapUser> {
    private final String email;
    private final String principal;
    private final String firstname;
    private final String lastname;
    private final String username;
    private final String domain;
    private final boolean disabled;

    public LdapUser(final String username, final String email, final String firstname, final String lastname, final String principal, final String domain, final boolean disabled) {
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.principal = principal;
        this.domain = domain;
        this.disabled = disabled;
    }

    @Override
    public int compareTo(final LdapUser other) {
        return getUsername().compareTo(other.getUsername());
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public int hashCode() {
        return getUsername().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof LdapUser) {
            final LdapUser otherLdapUser = (LdapUser) other;
            return getUsername().equals(otherLdapUser.getUsername());
        }
        return false;
    }
}
