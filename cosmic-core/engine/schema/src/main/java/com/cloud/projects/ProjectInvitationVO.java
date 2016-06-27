package com.cloud.projects;

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
@Table(name = "project_invitations")
public class ProjectInvitationVO implements ProjectInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "project_id")
    private long projectId;

    @Column(name = "account_id")
    private Long forAccountId;

    @Column(name = "domain_id")
    private Long inDomainId;

    @Column(name = "token")
    private String token;

    @Column(name = "email")
    private String email;

    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state = State.Pending;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "uuid")
    private String uuid;

    protected ProjectInvitationVO() {
        uuid = UUID.randomUUID().toString();
    }

    public ProjectInvitationVO(final long projectId, final Long accountId, final Long domainId, final String email, final String token) {
        forAccountId = accountId;
        inDomainId = domainId;
        this.projectId = projectId;
        this.email = email;
        this.token = token;
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getProjectId() {
        return projectId;
    }

    @Override
    public Long getForAccountId() {
        return forAccountId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public Long getInDomainId() {
        return inDomainId;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("ProjectInvitation[");
        buf.append(id).append("|projectId=").append(projectId).append("|accountId=").append(forAccountId).append("]");
        return buf.toString();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getDomainId() {
        return inDomainId == null ? -1 : inDomainId;
    }

    @Override
    public long getAccountId() {
        return forAccountId == null ? -1 : forAccountId;
    }

    @Override
    public Class<?> getEntityType() {
        return ProjectInvitation.class;
    }
}
