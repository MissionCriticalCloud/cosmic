//

//

package com.cloud.agent.resource.virtualnetwork.model;

import java.util.List;

public class VmData extends ConfigBase {
    private String vmIpAddress;
    private List<String[]> vmMetadata;

    public VmData() {
        super(ConfigBase.VM_METADATA);
    }

    public VmData(final String vmIpAddress, final List<String[]> vmMetadata) {
        super(ConfigBase.VM_METADATA);
        this.vmIpAddress = vmIpAddress;
        this.vmMetadata = vmMetadata;
    }

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public void setVmIpAddress(final String vmIpAddress) {
        this.vmIpAddress = vmIpAddress;
    }

    public List<String[]> getVmMetadata() {
        return vmMetadata;
    }

    public void setVmMetadata(final List<String[]> vmMetadata) {
        this.vmMetadata = vmMetadata;
    }
}
