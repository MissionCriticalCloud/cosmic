package com.cloud.network.vpc;

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
@Table(name = "vpc_offerings")
public class VpcOfferingVO implements VpcOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "name")
    String name;
    @Column(name = "unique_name")
    String uniqueName;
    @Column(name = "display_text")
    String displayText;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state = State.Disabled;
    @Column(name = "default")
    boolean isDefault = false;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "service_offering_id")
    Long serviceOfferingId;
    @Column(name = "secondary_service_offering_id")
    Long secondaryServiceOfferingId;
    @Column(name = "supports_distributed_router")
    boolean redundantRouter = false;
    @Column(name = "uuid")
    private String uuid;

    public VpcOfferingVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public VpcOfferingVO(final String name, final String displayText, final boolean isDefault, final Long serviceOfferingId, final Long secondaryServiceOfferingId,
                         final boolean redundantRouter) {
        this(name, displayText, serviceOfferingId, secondaryServiceOfferingId);
        this.isDefault = isDefault;
        this.redundantRouter = redundantRouter;
    }

    public VpcOfferingVO(final String name, final String displayText, final Long serviceOfferingId, final Long secondaryServiceOfferingId) {
        this.name = name;
        this.displayText = displayText;
        this.uniqueName = name;
        this.serviceOfferingId = serviceOfferingId;
        this.secondaryServiceOfferingId = secondaryServiceOfferingId;
        this.uuid = UUID.randomUUID().toString();
        this.state = State.Disabled;
    }

    public VpcOfferingVO(final String name, final String displayText, final boolean isDefault, final Long serviceOfferingId, final Long secondaryServiceOfferingId) {
        this(name, displayText, serviceOfferingId, secondaryServiceOfferingId);
        this.isDefault = isDefault;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setServiceOfferingId(final Long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public boolean getRedundantRouter() {
        return this.redundantRouter;
    }

    public Long getSecondaryServiceOfferingId() {
        if (secondaryServiceOfferingId != null) {
            return secondaryServiceOfferingId;
        } else {
            return getServiceOfferingId();
        }
    }

    public void setSecondaryServiceOfferingId(final Long secondaryServiceOfferingId) {
        this.secondaryServiceOfferingId = secondaryServiceOfferingId;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(final String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("[VPC Offering [");
        return buf.append(id).append("-").append(name).append("]").toString();
    }
}
