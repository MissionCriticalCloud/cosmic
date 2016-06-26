package com.cloud.event;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "event")
public class EventVO implements Event {
    public static final String LEVEL_INFO = "INFO";
    public static final String LEVEL_WARN = "WARN";
    public static final String LEVEL_ERROR = "ERROR";
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id = -1;
    @Column(name = "type")
    private String type;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    private State state = State.Completed;
    @Column(name = "description", length = 1024)
    private String description;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date createDate;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "level")
    private String level = LEVEL_INFO;
    @Column(name = "start_id")
    private long startId;
    @Column(name = "parameters", length = 1024)
    private String parameters;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "archived")
    private boolean archived;
    @Transient
    private int totalSize;

    public EventVO() {
        uuid = UUID.randomUUID().toString();
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setCreateDate(final Date createDate) {
        this.createDate = createDate;
    }

    public void setArchived(final boolean archived) {
        this.archived = archived;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(final int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public String getLevel() {
        return level;
    }

    public void setLevel(final String level) {
        this.level = level;
    }

    @Override
    public long getStartId() {
        return startId;
    }

    public void setStartId(final long startId) {
        this.startId = startId;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    public void setParameters(final String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean getArchived() {
        return archived;
    }

    public void setArchived(final Boolean archived) {
        this.archived = archived;
    }

    public void setCreatedDate(final Date createdDate) {
        createDate = createdDate;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    @Override
    public Class<?> getEntityType() {
        return Event.class;
    }
}
