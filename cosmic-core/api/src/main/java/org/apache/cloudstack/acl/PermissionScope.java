package org.apache.cloudstack.acl;

public enum PermissionScope {
    RESOURCE(0),
    ACCOUNT(1),
    DOMAIN(2),
    REGION(3), ALL(4);

    private final int _scale;

    private PermissionScope(final int scale) {
        _scale = scale;
    }

    public boolean greaterThan(final PermissionScope s) {
        if (_scale > s.getScale()) {
            return true;
        } else {
            return false;
        }
    }

    public int getScale() {
        return _scale;
    }
}
