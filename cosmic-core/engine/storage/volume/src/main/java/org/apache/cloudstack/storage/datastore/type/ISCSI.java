package org.apache.cloudstack.storage.datastore.type;

import org.apache.cloudstack.storage.BaseType;

import org.springframework.stereotype.Component;

@Component
public class ISCSI extends BaseType implements DataStoreType {
    private final String type = "iscsi";

    @Override
    public String toString() {
        return type;
    }
}
