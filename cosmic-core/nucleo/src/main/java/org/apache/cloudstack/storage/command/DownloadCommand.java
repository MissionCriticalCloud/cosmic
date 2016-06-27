//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.storage.AbstractDownloadCommand;
import com.cloud.agent.api.storage.PasswordAuth;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.utils.net.Proxy;
import org.apache.cloudstack.api.InternalIdentity;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

public class DownloadCommand extends AbstractDownloadCommand implements InternalIdentity {

    private boolean hvm;
    private String description;
    private String checksum;
    private PasswordAuth auth;
    private Proxy _proxy;
    private Long maxDownloadSizeInBytes = null;
    private long id;
    private ResourceType resourceType = ResourceType.TEMPLATE;
    private String installPath;
    private DataStoreTO _store;
    private DataStoreTO cacheStore;

    protected DownloadCommand() {
    }

    public DownloadCommand(final DownloadCommand that) {
        super(that);
        hvm = that.hvm;
        checksum = that.checksum;
        id = that.id;
        description = that.description;
        auth = that.getAuth();
        setSecUrl(that.getSecUrl());
        maxDownloadSizeInBytes = that.getMaxDownloadSizeInBytes();
        resourceType = that.resourceType;
        installPath = that.installPath;
        _store = that._store;
        _proxy = that._proxy;
    }

    public PasswordAuth getAuth() {
        return auth;
    }

    public Long getMaxDownloadSizeInBytes() {
        return maxDownloadSizeInBytes;
    }

    public DownloadCommand(final TemplateObjectTO template, final String user, final String passwd, final Long maxDownloadSizeInBytes) {
        this(template, maxDownloadSizeInBytes);
        auth = new PasswordAuth(user, passwd);
    }

    public DownloadCommand(final TemplateObjectTO template, final Long maxDownloadSizeInBytes) {

        super(template.getName(), template.getOrigUrl(), template.getFormat(), template.getAccountId());
        _store = template.getDataStore();
        installPath = template.getPath();
        hvm = template.isRequiresHvm();
        checksum = template.getChecksum();
        id = template.getId();
        description = template.getDescription();
        if (_store instanceof NfsTO) {
            setSecUrl(((NfsTO) _store).getUrl());
        }
        this.maxDownloadSizeInBytes = maxDownloadSizeInBytes;
    }

    public DownloadCommand(final VolumeObjectTO volume, final Long maxDownloadSizeInBytes, final String checkSum, final String url, final ImageFormat format) {
        super(volume.getName(), url, format, volume.getAccountId());
        checksum = checkSum;
        id = volume.getVolumeId();
        installPath = volume.getPath();
        _store = volume.getDataStore();
        this.maxDownloadSizeInBytes = maxDownloadSizeInBytes;
        resourceType = ResourceType.VOLUME;
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean isHvm() {
        return hvm;
    }

    public void setHvm(final boolean hvm) {
        this.hvm = hvm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public void setCreds(final String userName, final String passwd) {
        auth = new PasswordAuth(userName, passwd);
    }

    public Proxy getProxy() {
        return _proxy;
    }

    public void setProxy(final Proxy proxy) {
        _proxy = proxy;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public DataStoreTO getDataStore() {
        return _store;
    }

    public void setDataStore(final DataStoreTO store) {
        this._store = store;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public DataStoreTO getCacheStore() {
        return cacheStore;
    }

    public void setCacheStore(final DataStoreTO cacheStore) {
        this.cacheStore = cacheStore;
    }

    public static enum ResourceType {
        VOLUME, TEMPLATE
    }
}
