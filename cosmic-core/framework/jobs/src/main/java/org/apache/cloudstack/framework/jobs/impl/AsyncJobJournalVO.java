package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.jobs.AsyncJob;

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
@Table(name = "async_job_journal")
public class AsyncJobJournalVO {
    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;
    @Column(name = "job_id")
    private long jobId;
    @Column(name = "journal_type", updatable = false, nullable = false, length = 32)
    @Enumerated(value = EnumType.STRING)
    private AsyncJob.JournalType journalType;
    @Column(name = "journal_text", length = 1024)
    private String journalText;
    @Column(name = "journal_obj", length = 1024)
    private String journalObjJsonString;

    public AsyncJobJournalVO() {
        created = DateUtil.currentGMTTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(final long jobId) {
        this.jobId = jobId;
    }

    public AsyncJob.JournalType getJournalType() {
        return journalType;
    }

    public void setJournalType(final AsyncJob.JournalType journalType) {
        this.journalType = journalType;
    }

    public String getJournalText() {
        return journalText;
    }

    public void setJournalText(final String journalText) {
        this.journalText = journalText;
    }

    public String getJournalObjJsonString() {
        return journalObjJsonString;
    }

    public void setJournalObjJsonString(final String journalObjJsonString) {
        this.journalObjJsonString = journalObjJsonString;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }
}
