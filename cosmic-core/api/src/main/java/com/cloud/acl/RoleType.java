package com.cloud.acl;

public enum RoleType {
    Admin(1), ResourceAdmin(2), DomainAdmin(4), User(8), ReadOnly(16), Unknown(0);

    private final int mask;

    RoleType(final int mask) {
        this.mask = mask;
    }

    public int getValue() {
        return mask;
    }
}

