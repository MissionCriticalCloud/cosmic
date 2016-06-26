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
@Table(name = "guest_os_hypervisor")
public class GuestOSHypervisorVO implements GuestOSHypervisor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "guest_os_id")
    long guestOsId;

    @Column(name = "guest_os_name")
    String guestOsName;

    @Column(name = "hypervisor_type")
    String hypervisorType;

    @Column(name = "hypervisor_version")
    String hypervisorVersion;

    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    @Column(name = "is_user_defined")
    private boolean isUserDefined;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getHypervisorType() {
        return hypervisorType;
    }

    @Override
    public String getGuestOsName() {
        return guestOsName;
    }

    @Override
    public long getGuestOsId() {
        return guestOsId;
    }

    @Override
    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public boolean getIsUserDefined() {
        return isUserDefined;
    }

    public void setIsUserDefined(final boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setHypervisorVersion(final String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    public void setGuestOsId(final long guestOsId) {
        this.guestOsId = guestOsId;
    }

    public void setGuestOsName(final String guestOsName) {
        this.guestOsName = guestOsName;
    }

    public void setHypervisorType(final String hypervisorType) {
        this.hypervisorType = hypervisorType;
    }
}
