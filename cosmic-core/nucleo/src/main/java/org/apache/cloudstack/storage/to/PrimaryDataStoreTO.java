//

//

package org.apache.cloudstack.storage.to;

import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage.StoragePoolType;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStore;

import java.util.Map;

public class PrimaryDataStoreTO implements DataStoreTO {
    public static final String MANAGED = PrimaryDataStore.MANAGED;
    public static final String STORAGE_HOST = PrimaryDataStore.STORAGE_HOST;
    public static final String STORAGE_PORT = PrimaryDataStore.STORAGE_PORT;
    public static final String MANAGED_STORE_TARGET = PrimaryDataStore.MANAGED_STORE_TARGET;
    public static final String MANAGED_STORE_TARGET_ROOT_VOLUME = PrimaryDataStore.MANAGED_STORE_TARGET_ROOT_VOLUME;
    public static final String CHAP_INITIATOR_USERNAME = PrimaryDataStore.CHAP_INITIATOR_USERNAME;
    public static final String CHAP_INITIATOR_SECRET = PrimaryDataStore.CHAP_INITIATOR_SECRET;
    public static final String CHAP_TARGET_USERNAME = PrimaryDataStore.CHAP_TARGET_USERNAME;
    public static final String CHAP_TARGET_SECRET = PrimaryDataStore.CHAP_TARGET_SECRET;
    public static final String VOLUME_SIZE = PrimaryDataStore.VOLUME_SIZE;
    private static final String pathSeparator = "/";
    private final String uuid;
    private final String name;
    private final long id;
    private final String url;
    private String type;
    private StoragePoolType poolType;
    private String host;
    private String path;
    private int port;
    private final Map<String, String> details;

    public PrimaryDataStoreTO(final PrimaryDataStore dataStore) {
        this.uuid = dataStore.getUuid();
        this.name = dataStore.getName();
        this.id = dataStore.getId();
        this.setPoolType(dataStore.getPoolType());
        this.setHost(dataStore.getHostAddress());
        this.setPath(dataStore.getPath());
        this.setPort(dataStore.getPort());
        this.url = dataStore.getUri();
        this.details = dataStore.getDetails();
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

    public String getType() {
        return this.type;
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
