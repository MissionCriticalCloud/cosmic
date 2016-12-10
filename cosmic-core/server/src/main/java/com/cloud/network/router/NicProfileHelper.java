package com.cloud.network.router;

import com.cloud.network.Network;
import com.cloud.network.router.deployment.RouterDeploymentDefinition;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.vm.NicProfile;

public interface NicProfileHelper {

    public abstract NicProfile createPrivateNicProfileForGateway(final VpcGateway privateGateway, final VirtualRouter router);

    public abstract NicProfile createGuestNicProfileForVpcRouter(final RouterDeploymentDefinition vpcRouterDeploymentDefinition,
                                                                 Network guestNetwork);
}
