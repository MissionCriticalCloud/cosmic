package com.cloud.projects;

import com.cloud.utils.NumbersUtil;
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
@Table(name = "projects")
public class ProjectVO implements Project, Identity, InternalIdentity {
    @Column(name = "display_text")
    String displayText;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "project_account_id")
    long projectAccountId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "uuid")
    private String uuid;

    protected ProjectVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public ProjectVO(final String name, final String displayText, final long domainId, final long projectAccountId) {
        this.name = name;
        this.displayText = displayText;
        this.projectAccountId = projectAccountId;
        this.domainId = domainId;
        this.state = State.Disabled;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getProjectAccountId() {
        return projectAccountId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ProjectVO)) {
            return false;
        }
        final ProjectVO that = (ProjectVO) obj;
        if (this.id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("Project[");
        buf.append(id).append("|name=").append(name).append("|domainid=").append(domainId).append("]");
        return buf.toString();
    }
}
