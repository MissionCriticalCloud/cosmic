package com.cloud.storage;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "launch_permission")
public class LaunchPermissionVO implements InternalIdentity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "template_id")
    private long templateId;

    @Column(name = "account_id")
    private long accountId;

    public LaunchPermissionVO() {
    }

    public LaunchPermissionVO(final long templateId, final long accountId) {
        this.templateId = templateId;
        this.accountId = accountId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getTemplateId() {
        return templateId;
    }

    public long getAccountId() {
        return accountId;
    }
}
