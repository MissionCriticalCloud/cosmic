package com.cloud.vm;

public class VmWorkAddVmToNetwork extends VmWork {
    Long networkId;
    NicProfile requstedNicProfile;

    public VmWorkAddVmToNetwork(final long userId, final long accountId, final long vmId, final String handlerName, final Long networkId, final NicProfile requested) {
        super(userId, accountId, vmId, handlerName);

        this.networkId = networkId;
        requstedNicProfile = requested;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public NicProfile getRequestedNicProfile() {
        return requstedNicProfile;
    }
}
