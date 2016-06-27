//

//

package org.apache.cloudstack.storage.to;

import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.storage.DataStoreRole;
import org.apache.cloudstack.storage.image.datastore.ImageStoreInfo;

public class ImageStoreTO implements DataStoreTO {
    private static final String pathSeparator = "/";
    private String type;
    private String uri;
    private String providerName;
    private DataStoreRole role;
    private String uuid;

    public ImageStoreTO() {

    }

    public ImageStoreTO(final ImageStoreInfo dataStore) {
        this.type = dataStore.getType();
        this.uri = dataStore.getUri();
        this.providerName = null;
        this.role = dataStore.getRole();
    }

    public String getProtocol() {
        return this.type;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public DataStoreRole getRole() {
        return this.role;
    }

    public void setRole(final DataStoreRole role) {
        this.role = role;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getUrl() {
        return getUri();
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    @Override
    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return new StringBuilder("ImageStoreTO[type=").append(type)
                                                      .append("|provider=")
                                                      .append(providerName)
                                                      .append("|role=")
                                                      .append(role)
                                                      .append("|uri=")
                                                      .append(uri)
                                                      .append("]")
                                                      .toString();
    }
}
