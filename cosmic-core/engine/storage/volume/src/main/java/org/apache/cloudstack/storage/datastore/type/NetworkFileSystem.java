package org.apache.cloudstack.storage.datastore.type;

import org.apache.cloudstack.storage.BaseType;

import org.springframework.stereotype.Component;

@Component
public class NetworkFileSystem extends BaseType implements DataStoreType {
    private final String type = "nfs";

    @Override
    public String toString() {
        return type;
    }
}
