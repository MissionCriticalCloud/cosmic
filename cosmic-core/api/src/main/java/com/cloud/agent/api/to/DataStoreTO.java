package com.cloud.agent.api.to;

import com.cloud.storage.DataStoreRole;

public interface DataStoreTO {
    DataStoreRole getRole();

    String getUuid();

    String getUrl();

    String getPathSeparator();
}
