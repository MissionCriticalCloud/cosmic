package com.cloud.vm;

import com.cloud.deploy.DeployDestination;

public class VmWorkMigrateForScale extends VmWorkMigrate {
    private static final long serialVersionUID = 6854870395568389613L;

    Long newSvcOfferingId;

    public VmWorkMigrateForScale(final long userId, final long accountId, final long vmId, final String handlerName, final long srcHostId,
                                 final DeployDestination dest, final Long newSvcOfferingId) {

        super(userId, accountId, vmId, handlerName, srcHostId, dest);
        this.newSvcOfferingId = newSvcOfferingId;
    }

    public Long getNewServiceOfferringId() {
        return newSvcOfferingId;
    }
}
