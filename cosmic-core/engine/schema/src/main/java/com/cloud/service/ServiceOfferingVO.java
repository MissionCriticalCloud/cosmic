package com.cloud.service;

import com.cloud.offering.ServiceOffering;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.vm.VirtualMachine;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Map;

@Entity
@Table(name = "service_offering")
@DiscriminatorValue(value = "Service")
@PrimaryKeyJoinColumn(name = "id")
public class ServiceOfferingVO extends DiskOfferingVO implements ServiceOffering {
    @Column(name = "sort_key")
    int sortKey;
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call service offering dao to load it.
    @Transient
    Map<String, String> details;
    // This flag is required to tell if the offering is dynamic once the cpu, memory and speed are set.
    // In some cases cpu, memory and speed are set to non-null values even if the offering is dynamic.
    @Transient
    boolean isDynamic;
    @Column(name = "cpu")
    private Integer cpu;
    @Column(name = "speed")
    private Integer speed;
    @Column(name = "ram_size")
    private Integer ramSize;
    @Column(name = "nw_rate")
    private Integer rateMbps;
    @Column(name = "mc_rate")
    private Integer multicastRateMbps;
    @Column(name = "ha_enabled")
    private boolean offerHA;
    @Column(name = "limit_cpu_use")
    private boolean limitCpuUse;
    @Column(name = "is_volatile")
    private boolean volatileVm;
    @Column(name = "host_tag")
    private String hostTag;
    @Column(name = "default_use")
    private boolean defaultUse;
    @Column(name = "vm_type")
    private String vmType;
    @Column(name = "deployment_planner")
    private String deploymentPlanner = null;

    protected ServiceOfferingVO() {
        super();
    }

    public ServiceOfferingVO(final String name, final Integer cpu, final Integer ramSize, final Integer speed, final Integer rateMbps, final Integer multicastRateMbps, final
    boolean offerHA, final String displayText,
                             final ProvisioningType provisioningType, final boolean useLocalStorage, final boolean recreatable, final String tags, final boolean systemUse, final
                             VirtualMachine.Type vmType, final boolean
                                     defaultUse) {
        super(name, displayText, provisioningType, false, tags, recreatable, useLocalStorage, systemUse, true);
        this.cpu = cpu;
        this.ramSize = ramSize;
        this.speed = speed;
        this.rateMbps = rateMbps;
        this.multicastRateMbps = multicastRateMbps;
        this.offerHA = offerHA;
        limitCpuUse = false;
        volatileVm = false;
        this.defaultUse = defaultUse;
        this.vmType = vmType == null ? null : vmType.toString().toLowerCase();
    }

    public ServiceOfferingVO(final String name, final Integer cpu, final Integer ramSize, final Integer speed, final Integer rateMbps, final Integer multicastRateMbps, final
    boolean offerHA,
                             final boolean limitResourceUse, final boolean volatileVm, final String displayText, final ProvisioningType provisioningType, final boolean
                                     useLocalStorage, final boolean recreatable,
                             final String tags, final boolean systemUse,
                             final VirtualMachine.Type vmType, final Long domainId, final String hostTag, final String deploymentPlanner) {
        this(name,
                cpu,
                ramSize,
                speed,
                rateMbps,
                multicastRateMbps,
                offerHA,
                limitResourceUse,
                volatileVm,
                displayText,
                provisioningType,
                useLocalStorage,
                recreatable,
                tags,
                systemUse,
                vmType,
                domainId,
                hostTag);
        this.deploymentPlanner = deploymentPlanner;
    }

    public ServiceOfferingVO(final String name, final Integer cpu, final Integer ramSize, final Integer speed, final Integer rateMbps, final Integer multicastRateMbps, final
    boolean offerHA,
                             final boolean limitResourceUse, final boolean volatileVm, final String displayText, final ProvisioningType provisioningType, final boolean
                                     useLocalStorage, final boolean recreatable,
                             final String tags, final boolean systemUse,
                             final VirtualMachine.Type vmType, final Long domainId, final String hostTag) {
        this(name,
                cpu,
                ramSize,
                speed,
                rateMbps,
                multicastRateMbps,
                offerHA,
                limitResourceUse,
                volatileVm,
                displayText,
                provisioningType,
                useLocalStorage,
                recreatable,
                tags,
                systemUse,
                vmType,
                domainId);
        this.hostTag = hostTag;
    }

    public ServiceOfferingVO(final String name, final Integer cpu, final Integer ramSize, final Integer speed, final Integer rateMbps, final Integer multicastRateMbps, final
    boolean offerHA, final boolean limitCpuUse,
                             final boolean volatileVm, final String displayText, final ProvisioningType provisioningType, final boolean useLocalStorage, final boolean
                                     recreatable, final String tags, final boolean
                                     systemUse, final VirtualMachine.Type vmType, final Long domainId) {
        super(name, displayText, provisioningType, false, tags, recreatable, useLocalStorage, systemUse, true, domainId);
        this.cpu = cpu;
        this.ramSize = ramSize;
        this.speed = speed;
        this.rateMbps = rateMbps;
        this.multicastRateMbps = multicastRateMbps;
        this.offerHA = offerHA;
        this.limitCpuUse = limitCpuUse;
        this.volatileVm = volatileVm;
        this.vmType = vmType == null ? null : vmType.toString().toLowerCase();
    }

    public ServiceOfferingVO(final ServiceOfferingVO offering) {
        super(offering.getId(),
                offering.getName(),
                offering.getDisplayText(),
                offering.getProvisioningType(),
                false,
                offering.getTags(),
                offering.isRecreatable(),
                offering.getUseLocalStorage(),
                offering.getSystemUse(),
                true,
                offering.isCustomizedIops() == null ? false : offering.isCustomizedIops(),
                offering.getDomainId(),
                offering.getMinIops(),
                offering.getMaxIops());
        cpu = offering.getCpu();
        ramSize = offering.getRamSize();
        speed = offering.getSpeed();
        rateMbps = offering.getRateMbps();
        multicastRateMbps = offering.getMulticastRateMbps();
        offerHA = offering.getOfferHA();
        limitCpuUse = offering.getLimitCpuUse();
        volatileVm = offering.getVolatileVm();
        hostTag = offering.getHostTag();
        vmType = offering.getSystemVmType();
    }

    @Override
    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(final int cpu) {
        this.cpu = cpu;
    }

    @Override
    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(final int speed) {
        this.speed = speed;
    }

    @Override
    public Integer getRamSize() {
        return ramSize;
    }

    @Override
    public boolean getOfferHA() {
        return offerHA;
    }

    public void setOfferHA(final boolean offerHA) {
        this.offerHA = offerHA;
    }

    @Override
    public boolean getLimitCpuUse() {
        return limitCpuUse;
    }

    @Override
    public boolean getVolatileVm() {
        return volatileVm;
    }

    @Override
    public Integer getRateMbps() {
        return rateMbps;
    }

    public void setRateMbps(final Integer rateMbps) {
        this.rateMbps = rateMbps;
    }

    @Override
    public Integer getMulticastRateMbps() {
        return multicastRateMbps;
    }

    public void setMulticastRateMbps(final Integer multicastRateMbps) {
        this.multicastRateMbps = multicastRateMbps;
    }

    @Override
    public String getHostTag() {
        return hostTag;
    }

    @Override
    public boolean getDefaultUse() {
        return defaultUse;
    }

    @Override
    public String getSystemVmType() {
        return vmType;
    }

    @Override
    public String getDeploymentPlanner() {
        return deploymentPlanner;
    }

    @Override
    public boolean isDynamic() {
        return cpu == null || speed == null || ramSize == null || isDynamic;
    }

    public void setHostTag(final String hostTag) {
        this.hostTag = hostTag;
    }

    public void setRamSize(final int ramSize) {
        this.ramSize = ramSize;
    }

    public void setLimitResourceUse(final boolean limitCpuUse) {
        this.limitCpuUse = limitCpuUse;
    }

    @Override
    @Transient
    public String[] getTagsArray() {
        final String tags = getTags();
        if (tags == null || tags.length() == 0) {
            return new String[0];
        }

        return tags.split(",");
    }

    @Override
    public int getSortKey() {
        return sortKey;
    }

    @Override
    public void setSortKey(final int key) {
        sortKey = key;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    public String getDetail(final String name) {
        return details != null ? details.get(name) : null;
    }

    public void setDetail(final String name, final String value) {
        assert (details != null) : "Did you forget to load the details?";

        details.put(name, value);
    }

    public void setDynamicFlag(final boolean isdynamic) {
        isDynamic = isdynamic;
    }
}
