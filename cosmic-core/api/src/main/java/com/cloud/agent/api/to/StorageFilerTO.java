package com.cloud.agent.api.to;

import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;

public class StorageFilerTO {
    long id;
    String uuid;
    String host;
    String path;
    String userInfo;
    int port;
    StoragePoolType type;

    public StorageFilerTO(final StoragePool pool) {
        this.id = pool.getId();
        this.host = pool.getHostAddress();
        this.port = pool.getPort();
        this.path = pool.getPath();
        this.type = pool.getPoolType();
        this.uuid = pool.getUuid();
        this.userInfo = pool.getUserInfo();
    }

    protected StorageFilerTO() {
    }

    public long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public int getPort() {
        return port;
    }

    public StoragePoolType getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringBuilder("Pool[").append(id).append("|").append(host).append(":").append(port).append("|").append(path).append("]").toString();
    }
}
