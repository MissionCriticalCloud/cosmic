package com.cloud.vm;

public class VmWorkAddVmToNetwork extends VmWork {
    private static final long serialVersionUID = 8861516006586736813L;

    Long networkId;
    NicProfile requstedNicProfile;

    public VmWorkAddVmToNetwork(final long userId, final long accountId, final long vmId, final String handlerName,
                                final Long networkId, final NicProfile requested) {
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
