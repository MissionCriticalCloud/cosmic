package com.cloud.hypervisor.xenserver.resource;

import com.cloud.common.resource.ServerResource;

import javax.ejb.Local;

@Local(value = ServerResource.class)
public class XenServer56SP2Resource extends XenServer56FP1Resource {

    public XenServer56SP2Resource() {
        super();
        this._xsMemoryUsed = 128 * 1024 * 1024L;
        this._xsVirtualizationFactor = 62.0 / 64.0;
    }
}
