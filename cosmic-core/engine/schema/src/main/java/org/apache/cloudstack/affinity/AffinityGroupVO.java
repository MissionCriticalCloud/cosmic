package org.apache.cloudstack.affinity;

import org.apache.cloudstack.acl.ControlledEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ("affinity_group"))
public class AffinityGroupVO implements AffinityGroup {
    @Column(name = "acl_type")
    @Enumerated(value = EnumType.STRING)
    ControlledEntity.ACLType aclType;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "type")
    private String type;
    @Column(name = "description")
    private String description;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "uuid")
    private String uuid;

    public AffinityGroupVO() {
        uuid = UUID.randomUUID().toString();
    }

    public AffinityGroupVO(final String name, final String type, final String description, final long domainId, final long accountId, final ACLType aclType) {
        this.name = name;
        this.description = description;
        this.domainId = domainId;
        this.accountId = accountId;
        uuid = UUID.randomUUID().toString();
        this.type = type;
        this.aclType = aclType;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ControlledEntity.ACLType getAclType() {
        return aclType;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("AffinityGroup[");
        buf.append(uuid).append("]");
        return buf.toString();
    }

    @Override
    public Class<?> getEntityType() {
        return AffinityGroup.class;
    }
}
