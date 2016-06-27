//

//

package com.cloud.utils.storage.encoding;

public class DecodedDataStore {
    private final String role;
    private final String uuid;
    private final String providerName;
    private final String scheme;
    private final String url;
    private final String server;
    private final String path;

    public DecodedDataStore(final String role, final String uuid, final String providerName, final String scheme, final String url, final String server, final String path) {
        this.role = role;
        this.uuid = uuid;
        this.providerName = providerName;
        this.scheme = scheme;
        this.url = url;
        this.server = server;
        this.path = path;
    }

    public String getRole() {
        return role;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getScheme() {
        return scheme;
    }

    public String getUrl() {
        return url;
    }

    public String getServer() {
        return server;
    }

    public String getPath() {
        return path;
    }
}
