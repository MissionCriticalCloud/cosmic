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
@Table(name = "counter")
public class CounterVO implements Counter, Identity, InternalIdentity {

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "source")
    @Enumerated(value = EnumType.STRING)
    private Source source;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;
    @Column(name = "uuid")
    private String uuid;

    public CounterVO() {
    }

    public CounterVO(final Source source, final String name, final String value) {
        this.source = source;
        this.name = name;
        this.value = value;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return new StringBuilder("Counter[").append("id-").append(id).append("]").toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public Date getRemoved() {
        return removed;
    }

    public Date getCreated() {
        return created;
    }
}
