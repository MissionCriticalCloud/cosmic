package com.cloud.api.query.vo;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

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
