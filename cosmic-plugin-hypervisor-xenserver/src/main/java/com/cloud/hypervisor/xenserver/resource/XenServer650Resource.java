package com.cloud.hypervisor.xenserver.resource;

import com.cloud.resource.ServerResource;

import javax.ejb.Local;

@Local(value = ServerResource.class)
public class XenServer650Resource extends Xenserver625Resource {

    @Override
    protected String getPatchFilePath() {
        return "scripts/vm/hypervisor/xenserver/xenserver65/patch";
    }
}
