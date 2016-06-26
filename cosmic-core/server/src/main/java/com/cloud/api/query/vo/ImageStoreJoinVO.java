package com.cloud.api.query.vo;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Image Data Store DB view.
 */
@Entity
@Table(name = "image_store_view")
public class ImageStoreJoinVO extends BaseViewVO implements InternalIdentity, Identity {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "image_provider_name", nullable = false)
    private String providerName;

    @Column(name = "scope")
    @Enumerated(value = EnumType.STRING)
    private ScopeType scope;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private DataStoreRole role;

    @Column(name = "data_center_id")
    private long zoneId;

    @Column(name = "data_center_uuid")
    private String zoneUuid;

    @Column(name = "data_center_name")
    private String zoneName;

    @Column(name = "detail_name")
    private String detailName;

    @Column(name = "detail_value")
    private String detailValue;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getZoneId() {
        return zoneId;
    }

    public String getZoneUuid() {
        return zoneUuid;
    }

    public String getZoneName() {
        return zoneName;
    }

    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getProviderName() {
        return providerName;
    }

    public ScopeType getScope() {
        return scope;
    }

    public String getDetailName() {
        return detailName;
    }

    public String getDetailValue() {
        return detailValue;
    }

    public DataStoreRole getRole() {
        return role;
    }

    public Date getRemoved() {
        return removed;
    }
}
