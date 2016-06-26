package com.cloud.agent.api.to;

import com.cloud.storage.DataStoreRole;

public class NfsTO implements DataStoreTO {

    private static final String pathSeparator = "/";
    private String _url;
    private DataStoreRole _role;
    private String uuid;

    public NfsTO() {

        super();
    }

    public NfsTO(final String url, final DataStoreRole role) {

        super();

        this._url = url;
        this._role = role;
    }

    @Override
    public DataStoreRole getRole() {
        return _role;
    }

    public void setRole(final DataStoreRole role) {
        this._role = role;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getUrl() {
        return _url;
    }

    public void setUrl(final String url) {
        this._url = url;
    }

    @Override
    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
