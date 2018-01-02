package com.cloud.offering;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface ServiceOffering extends DiskOffering, InfrastructureEntity, InternalIdentity, Identity {
    String consoleProxyDefaultOffUniqueName = "Cloud.com-ConsoleProxy";
    String ssvmDefaultOffUniqueName = "Cloud.com-SecondaryStorage";
    String routerDefaultOffUniqueName = "Cloud.Com-SoftwareRouter";
    String routerDefaultSecondaryOffUniqueName = "Cloud.Com-SoftwareRouter2";

    Integer getCpu();

    Integer getSpeed();

    Integer getRamSize();

    boolean getOfferHA();

    boolean getLimitCpuUse();

    boolean getVolatileVm();

    Integer getRateMbps();

    Integer getMulticastRateMbps();

    @Override
    boolean getUseLocalStorage();

    @Override
    Long getDomainId();

    @Override
    String getName();

    @Override
    boolean getSystemUse();

    @Override
    String getDisplayText();

    @Override
    String getTags();

    @Override
    Date getCreated();

    String getHostTag();

    boolean getDefaultUse();

    String getSystemVmType();

    String getDeploymentPlanner();

    enum StorageType {
        local, shared
    }
}
