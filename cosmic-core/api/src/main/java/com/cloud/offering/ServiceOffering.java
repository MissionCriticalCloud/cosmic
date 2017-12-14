package com.cloud.offering;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.Date;

/**
 * offered.
 */
public interface ServiceOffering extends DiskOffering, InfrastructureEntity, InternalIdentity, Identity {
    public static final String consoleProxyDefaultOffUniqueName = "Cloud.com-ConsoleProxy";
    public static final String ssvmDefaultOffUniqueName = "Cloud.com-SecondaryStorage";
    public static final String routerDefaultOffUniqueName = "Cloud.Com-SoftwareRouter";
    public static final String routerDefaultSecondaryOffUniqueName = "Cloud.Com-SoftwareRouter2";

    /**
     * @return # of cpu.
     */
    Integer getCpu();

    /**
     * @return speed in mhz
     */
    Integer getSpeed();

    /**
     * @return ram size in megabytes
     */
    Integer getRamSize();

    /**
     * @return Does this service plan offer HA?
     */
    boolean getOfferHA();

    /**
     * @return Does this service plan offer VM to use CPU resources beyond the service offering limits?
     */
    boolean getLimitCpuUse();

    /**
     * @return Does this service plan support Volatile VM that is, discard VM's root disk and create a new one on reboot?
     */
    boolean getVolatileVm();

    /**
     * @return the rate in megabits per sec to which a VM's network interface is throttled to
     */
    Integer getRateMbps();

    /**
     * @return the rate megabits per sec to which a VM's multicast&broadcast traffic is throttled to
     */
    Integer getMulticastRateMbps();

    /**
     * @return whether or not the service offering requires local storage
     */
    @Override
    boolean getUseLocalStorage();

    @Override
    Long getDomainId();

    /**
     * @return user readable description
     */
    @Override
    String getName();

    /**
     * @return is this a system service offering
     */
    @Override
    boolean getSystemUse();

    @Override
    String getDisplayText();

    @Override
    String getTags();

    @Override
    Date getCreated();

    /**
     * @return tag that should be present on the host needed, optional parameter
     */
    String getHostTag();

    boolean getDefaultUse();

    String getSystemVmType();

    String getDeploymentPlanner();

    boolean isDynamic();

    public enum StorageType {
        local, shared
    }
}
