package com.cloud.legacymodel.to;

import com.cloud.legacymodel.storage.PrimaryDataStoreInfo;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.model.enumeration.StoragePoolType;

import java.util.Map;

public class PrimaryDataStoreTO implements DataStoreTO {
    public static final String MANAGED = PrimaryDataStoreInfo.MANAGED;
    public static final String STORAGE_HOST = PrimaryDataStoreInfo.STORAGE_HOST;
    public static final String STORAGE_PORT = PrimaryDataStoreInfo.STORAGE_PORT;
    public static final String MANAGED_STORE_TARGET = PrimaryDataStoreInfo.MANAGED_STORE_TARGET;
    public static final String MANAGED_STORE_TARGET_ROOT_VOLUME = PrimaryDataStoreInfo.MANAGED_STORE_TARGET_ROOT_VOLUME;
    public static final String CHAP_INITIATOR_USERNAME = PrimaryDataStoreInfo.CHAP_INITIATOR_USERNAME;
    public static final String CHAP_INITIATOR_SECRET = PrimaryDataStoreInfo.CHAP_INITIATOR_SECRET;
    public static final String CHAP_TARGET_USERNAME = PrimaryDataStoreInfo.CHAP_TARGET_USERNAME;
    public static final String CHAP_TARGET_SECRET = PrimaryDataStoreInfo.CHAP_TARGET_SECRET;
    public static final String VOLUME_SIZE = PrimaryDataStoreInfo.VOLUME_SIZE;
    private static final String pathSeparator = "/";
    private final String uuid;
    private final String name;
    private final long id;
    private final String url;
    private StoragePoolType poolType;
    private String host;
    private String path;
    private int port;
    private final Map<String, String> details;

    public PrimaryDataStoreTO(final String uuid, final String name, final long id, final String url, final StoragePoolType poolType, final String host, final String path, final int port, final
    Map<String, String> details) {
        this.uuid = uuid;
        this.name = name;
        this.id = id;
        this.url = url;
        this.poolType = poolType;
        this.host = host;
        this.path = path;
        this.port = port;
        this.details = details;
    }

    public long getId() {
        return this.id;
    }

    public Map<String, String> getDetails() {
        return this.details;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public DataStoreRole getRole() {
        return DataStoreRole.Primary;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public String getPathSeparator() {
        return pathSeparator;
    }

    public StoragePoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(final StoragePoolType poolType) {
        this.poolType = poolType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return new StringBuilder("PrimaryDataStoreTO[uuid=").append(uuid)
                                                            .append("|name=")
                                                            .append(name)
                                                            .append("|id=")
                                                            .append(id)
                                                            .append("|pooltype=")
                                                            .append(poolType)
                                                            .append("]")
                                                            .toString();
    }
}
