package org.apache.cloudstack.storage.datastore.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ImageStore;
import com.cloud.storage.ScopeType;
import com.cloud.utils.UriUtils;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.util.Date;

@Entity
@Table(name = "image_store")
public class ImageStoreVO implements ImageStore {
    @Id
    @TableGenerator(name = "image_store_sq", table = "sequence", pkColumnName = "name", valueColumnName = "value", pkColumnValue = "image_store_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "protocol", nullable = false)
    private String protocol;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "image_provider_name", nullable = false)
    private String providerName;

    @Column(name = "data_center_id")
    private Long dcId;

    @Column(name = "scope")
    @Enumerated(value = EnumType.STRING)
    private ScopeType scope;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private DataStoreRole role;

    @Column(name = "parent")
    private String parent;

    @Column(name = "total_size")
    private Long totalSize;

    @Column(name = "used_bytes")
    private Long usedBytes;

    public DataStoreRole getRole() {
        return role;
    }

    public void setRole(final DataStoreRole role) {
        this.role = role;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Long getDataCenterId() {
        return this.dcId;
    }

    @Override
    public String getProviderName() {
        return this.providerName;
    }

    public void setProviderName(final String provider) {
        this.providerName = provider;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setDataCenterId(final Long dcId) {
        this.dcId = dcId;
    }

    public ScopeType getScope() {
        return this.scope;
    }

    public void setScope(final ScopeType scope) {
        this.scope = scope;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getUrl() {
        String updatedUrl = url;
        if ("cifs".equalsIgnoreCase(this.protocol)) {
            updatedUrl = UriUtils.getUpdateUri(updatedUrl, false);
        }
        return updatedUrl;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(final Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(final Long usedBytes) {
        this.usedBytes = usedBytes;
    }
}
