package com.cloud.usage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_vm_instance")
public class UsageVMInstanceVO {

    @Column(name = "usage_type")
    private int usageType;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "vm_instance_id")
    private long vmInstanceId;

    @Column(name = "vm_name")
    private String vmName = null;

    @Column(name = "service_offering_id")
    private long serviceOfferingId;

    @Column(name = "cpu_cores")
    private Long cpuCores;

    @Column(name = "memory")
    private Long memory;

    @Column(name = "cpu_speed")
    private Long cpuSpeed;

    @Column(name = "template_id")
    private long templateId;

    @Column(name = "hypervisor_type")
    private String hypervisorType;

    @Column(name = "start_date")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startDate = null;

    @Column(name = "end_date")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endDate = null;

    protected UsageVMInstanceVO() {
    }

    public UsageVMInstanceVO(final int usageType, final long zoneId, final long accountId, final long vmInstanceId, final String vmName, final long serviceOfferingId, final long
            templateId,
                             final String hypervisorType, final Date startDate, final Date endDate) {
        this.usageType = usageType;
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.vmInstanceId = vmInstanceId;
        this.vmName = vmName;
        this.serviceOfferingId = serviceOfferingId;
        this.templateId = templateId;
        this.hypervisorType = hypervisorType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UsageVMInstanceVO(final int usageType, final long zoneId, final long accountId, final long vmInstanceId, final String vmName, final long serviceOfferingId, final long
            templateId,
                             final Long cpuSpeed, final Long cpuCores, final Long memory, final String hypervisorType, final Date startDate, final Date endDate) {
        this.usageType = usageType;
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.vmInstanceId = vmInstanceId;
        this.vmName = vmName;
        this.serviceOfferingId = serviceOfferingId;
        this.templateId = templateId;
        this.cpuSpeed = cpuSpeed;
        this.cpuCores = cpuCores;
        this.memory = memory;
        this.hypervisorType = hypervisorType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getUsageType() {
        return usageType;
    }

    public long getZoneId() {
        return zoneId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getVmInstanceId() {
        return vmInstanceId;
    }

    public String getVmName() {
        return vmName;
    }

    public long getSerivceOfferingId() {
        return serviceOfferingId;
    }

    public long getTemplateId() {
        return templateId;
    }

    public void setServiceOfferingId(final long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public String getHypervisorType() {
        return hypervisorType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(final Long memory) {
        this.memory = memory;
    }

    public Long getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(final Long cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Long getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(final Long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }
}
