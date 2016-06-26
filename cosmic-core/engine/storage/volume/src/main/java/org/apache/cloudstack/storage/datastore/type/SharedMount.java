package org.apache.cloudstack.storage.datastore.type;

import org.apache.cloudstack.storage.BaseType;

public class SharedMount extends BaseType implements DataStoreType {
    private final String type = "SharedMountPoint";

    @Override
    public String toString() {
        return type;
    }
}
