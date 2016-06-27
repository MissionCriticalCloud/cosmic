package com.cloud.hypervisor.xenserver.resource;

import com.cloud.resource.ServerResource;

import javax.ejb.Local;

@Local(value = ServerResource.class)
public class XenServer600Resource extends XenServer56SP2Resource {

    @Override
    protected String getPatchFilePath() {
        return "scripts/vm/hypervisor/xenserver/xenserver60/patch";
    }
}
