package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.ScopeType;

public interface ImageStoreProvider extends DataStoreProvider {

    boolean isScopeSupported(ScopeType scope);

    boolean needDownloadSysTemplate();
}
