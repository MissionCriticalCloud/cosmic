package com.cloud.api.query.vo;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

/**
 * This is the interface for all VO classes representing DB views created for previous ControlledEntity.
 */
public interface ControlledViewEntity extends ControlledEntity, InternalIdentity, Identity {

    public String getDomainPath();

    public short getAccountType();

    public String getAccountUuid();

    public String getAccountName();

    public String getDomainUuid();

    public String getDomainName();

    public String getProjectUuid();

    public String getProjectName();
}
