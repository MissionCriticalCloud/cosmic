package com.cloud.projects;

import com.cloud.utils.db.GenericDao;
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

@Entity
@Table(name = "project_account")
public class ProjectAccountVO implements ProjectAccount, InternalIdentity {
    @Column(name = "project_account_id")
    long projectAccountId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "account_role")
    @Enumerated(value = EnumType.STRING)
    private Role accountRole = Role.Regular;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    protected ProjectAccountVO() {
    }

    public ProjectAccountVO(final Project project, final long accountId, final Role accountRole) {
        this.accountId = accountId;
        this.accountRole = accountRole;
        this.projectId = project.getId();
        this.projectAccountId = project.getProjectAccountId();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getProjectId() {
        return projectId;
    }

    @Override
    public Role getAccountRole() {
        return accountRole;
    }

    @Override
    public long getProjectAccountId() {
        return projectAccountId;
    }

    public void setAccountRole(final Role accountRole) {
        this.accountRole = accountRole;
    }
}
