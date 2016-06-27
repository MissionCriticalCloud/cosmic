package com.cloud.agent.api.to;

import com.cloud.storage.DataStoreRole;
import com.cloud.utils.SwiftUtil;

public class SwiftTO implements DataStoreTO, SwiftUtil.SwiftClientCfg {
    private static final String pathSeparator = "/";
    Long id;
    String url;
    String account;
    String userName;
    String key;

    public SwiftTO() {
    }

    public SwiftTO(final Long id, final String url, final String account, final String userName, final String key) {
        this.id = id;
        this.url = url;
        this.account = account;
        this.userName = userName;
        this.key = key;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getEndPoint() {
        return this.url;
    }

    @Override
    public DataStoreRole getRole() {
        return DataStoreRole.Image;
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getPathSeparator() {
        return pathSeparator;
    }
}
