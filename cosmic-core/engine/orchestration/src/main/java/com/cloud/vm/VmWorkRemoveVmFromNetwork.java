package com.cloud.vm;

import com.cloud.network.Network;

import java.net.URI;

public class VmWorkRemoveVmFromNetwork extends VmWork {
    private static final long serialVersionUID = -5070392905642149925L;

    Network network;
    URI broadcastUri;

    public VmWorkRemoveVmFromNetwork(final long userId, final long accountId, final long vmId, final String handlerName, final Network network, final URI broadcastUri) {
        super(userId, accountId, vmId, handlerName);

        this.network = network;
        this.broadcastUri = broadcastUri;
    }

    public Network getNetwork() {
        return network;
    }

    public URI getBroadcastUri() {
        return broadcastUri;
    }
}
