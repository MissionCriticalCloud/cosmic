package com.cloud.legacymodel.to;

import com.cloud.model.enumeration.DataStoreRole;

public interface DataStoreTO {
    DataStoreRole getRole();

    String getUuid();

    String getUrl();

    String getPathSeparator();
}
