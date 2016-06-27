package org.apache.cloudstack.usage;

import java.util.Date;

public interface Usage {

    public long getId();

    public Long getZoneId();

    public Long getAccountId();

    public Long getDomainId();

    public String getDescription();

    public String getUsageDisplay();

    public int getUsageType();

    public Double getRawUsage();

    public Long getVmInstanceId();

    public String getVmName();

    public Long getCpuCores();

    public Long getCpuSpeed();

    public Long getMemory();

    public Long getOfferingId();

    public Long getTemplateId();

    public Long getUsageId();

    public String getType();

    public Long getNetworkId();

    public Long getSize();

    public Date getStartDate();

    public Date getEndDate();

    public Long getVirtualSize();
}
