package com.cloud.upgrade.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "version")
public class VersionVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "version")
    String version;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated")
    Date updated;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "step")
    Step step;

    public VersionVO(final String version) {
        this.version = version;
        this.updated = new Date();
        this.step = Step.Upgrade;
    }

    protected VersionVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(final Step step) {
        this.step = step;
    }

    public enum Step {
        Upgrade, Complete
    }
}
