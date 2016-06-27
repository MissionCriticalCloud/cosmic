package com.cloud.storage;

import org.apache.cloudstack.api.response.StorageProviderResponse;

import java.util.List;

public interface DataStoreProviderApiService {
    public List<StorageProviderResponse> getDataStoreProviders(String type);
}
