package com.cloud.api.query.vo;

import com.cloud.event.Event;
import com.cloud.event.Event.State;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "event_view")
public class EventJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Column(name = "display")
    protected boolean display = true;
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "type")
    private String type;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    private State state;
    @Column(name = "description")
    private String description;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date createDate;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "level")
    private String level;
    @Column(name = "start_id")
    private long startId;
    @Column(name = "start_uuid")
    private String startUuid;
    @Column(name = "parameters", length = 1024)
    private String parameters;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "account_uuid")
    private String accountUuid;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "account_type")
    private short accountType;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "project_uuid")
    private String projectUuid;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "archived")
    private boolean archived;

    public EventJoinVO() {
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setCreateDate(final Date createDate) {
        this.createDate = createDate;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setLevel(final String level) {
        this.level = level;
    }

    public void setStartId(final long startId) {
        this.startId = startId;
    }

    public void setStartUuid(final String startUuid) {
        this.startUuid = startUuid;
    }

    public void setParameters(final String parameters) {
        this.parameters = parameters;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public void setProjectUuid(final String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setArchived(final boolean archived) {
        this.archived = archived;
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
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getDomainPath() {
        return domainPath;
    }

    @Override
    public short getAccountType() {
        return accountType;
    }

    @Override
    public String getAccountUuid() {
        return accountUuid;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getDomainUuid() {
        return domainUuid;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getProjectUuid() {
        return projectUuid;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getLevel() {
        return level;
    }

    public long getStartId() {
        return startId;
    }

    public String getStartUuid() {
        return startUuid;
    }

    public String getParameters() {
        return parameters;
    }

    public boolean getArchived() {
        return archived;
    }

    public boolean getDisplay() {
        return display;
    }

    @Override
    public Class<?> getEntityType() {
        return Event.class;
    }
}
