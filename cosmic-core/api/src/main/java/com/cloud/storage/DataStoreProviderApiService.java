package com.cloud.storage;

import com.cloud.api.response.StorageProviderResponse;

import java.util.List;

public interface DataStoreProviderApiService {
    public List<StorageProviderResponse> getDataStoreProviders(String type);
}
