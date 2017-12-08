package com.cloud.storage;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "guest_os")
public class GuestOSVO implements GuestOS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "name")
    String name;
    @Column(name = "display_name")
    String displayName;
    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();
    @Column(name = "category_id")
    private long categoryId;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "is_user_defined")
    private boolean isUserDefined;

    @Column(name = "manufacturer_string")
    String manufacturer;

    @Column(name = "cpuflags")
    String cpuflags;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    @Override
    public boolean getIsUserDefined() {
        return isUserDefined;
    }

    public void setIsUserDefined(final boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(final String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCpuflags() {
        return cpuflags;
    }

    public void setCpuflags(final String cpuflags) {
        this.cpuflags = cpuflags;
    }
}
