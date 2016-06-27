package com.cloud.network.element;

import com.cloud.network.Network;

public interface IpDeployingRequester {
    /**
     * Would return the IpDeployer can deploy IP for this element
     *
     * @param network
     * @return IpDeployer object, or null if there is no deployer for this element
     */
    IpDeployer getIpDeployer(Network network);
}
