package com.cloud.network.as;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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
@Table(name = "conditions")
public class ConditionVO implements Condition, Identity, InternalIdentity {

    @Column(name = "domain_id")
    protected long domainId;
    @Column(name = "account_id")
    protected long accountId;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "counter_id")
    private long counterid;
    @Column(name = "threshold")
    private long threshold;
    @Column(name = "relational_operator")
    @Enumerated(value = EnumType.STRING)
    private Operator relationalOperator;
    @Column(name = "uuid")
    private String uuid;

    public ConditionVO() {
    }

    public ConditionVO(final long counterid, final long threshold, final long accountId, final long domainId, final Operator relationalOperator) {
        this.counterid = counterid;
        this.threshold = threshold;
        this.relationalOperator = relationalOperator;
        this.accountId = accountId;
        this.domainId = domainId;
        uuid = UUID.randomUUID().toString();
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder("Condition[").append("id-").append(id).append("]").toString();
    }

    @Override
    public long getCounterid() {
        return counterid;
    }

    @Override
    public long getThreshold() {
        return threshold;
    }

    @Override
    public Operator getRelationalOperator() {
        return relationalOperator;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public Date getRemoved() {
        return removed;
    }

    @Override
    public Class<?> getEntityType() {
        return Condition.class;
    }
}
