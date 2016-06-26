package org.apache.cloudstack.acl;

// Enum for default roles in CloudStack
public enum RoleType {
    Admin(1), ResourceAdmin(2), DomainAdmin(4), User(8), Unknown(0);

    private final int mask;

    private RoleType(final int mask) {
        this.mask = mask;
    }

    public int getValue() {
        return mask;
    }
}

