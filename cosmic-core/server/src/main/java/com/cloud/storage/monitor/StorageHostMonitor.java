package com.cloud.storage.monitor;

import com.cloud.host.Host;
import com.cloud.utils.component.Manager;

public interface StorageHostMonitor extends Manager {
    void failoverVolumes(Host fromHost);
}
