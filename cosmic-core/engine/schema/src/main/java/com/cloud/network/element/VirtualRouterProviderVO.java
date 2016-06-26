package com.cloud.network.element;

import com.cloud.network.VirtualRouterProvider;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = ("virtual_router_providers"))
public class VirtualRouterProviderVO implements VirtualRouterProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "nsp_id")
    private long nspId;
    @Column(name = "uuid")
    private String uuid;

    public VirtualRouterProviderVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public VirtualRouterProviderVO(final long nspId, final Type type) {
        this.nspId = nspId;
        this.type = type;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public long getNspId() {
        return nspId;
    }

    public void setNspId(final long nspId) {
        this.nspId = nspId;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }
}
