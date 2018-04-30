package com.cloud.offering;

import com.cloud.model.enumeration.DiskControllerType;

public class DiskOfferingInfo {
    private DiskOffering _diskOffering;
    private Long _size;
    private Long _minIops;
    private Long _maxIops;
    private DiskControllerType diskControllerType;

    public DiskOfferingInfo() {
    }

    public DiskOfferingInfo(final DiskOffering diskOffering) {
        _diskOffering = diskOffering;
    }

    public DiskOffering getDiskOffering() {
        return _diskOffering;
    }

    public void setDiskOffering(final DiskOffering diskOffering) {
        _diskOffering = diskOffering;
    }

    public Long getSize() {
        return _size;
    }

    public void setSize(final Long size) {
        _size = size;
    }

    public Long getMinIops() {
        return _minIops;
    }

    public void setMinIops(final Long minIops) {
        _minIops = minIops;
    }

    public Long getMaxIops() {
        return _maxIops;
    }

    public void setMaxIops(final Long maxIops) {
        _maxIops = maxIops;
    }

    public DiskControllerType getDiskControllerType() {
        return diskControllerType;
    }

    public void setDiskControllerType(final DiskControllerType diskControllerType) {
        this.diskControllerType = diskControllerType;
    }
}
