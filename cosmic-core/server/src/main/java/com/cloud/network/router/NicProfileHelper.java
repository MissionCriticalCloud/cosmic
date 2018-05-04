package com.cloud.network.router;

import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.network.vpc.VpcGateway;
import com.cloud.network.router.deployment.RouterDeploymentDefinition;
import com.cloud.vm.NicProfile;

public interface NicProfileHelper {

    public abstract NicProfile createPrivateNicProfileForGateway(final VpcGateway privateGateway, final VirtualRouter router);

    public abstract NicProfile createGuestNicProfileForVpcRouter(final RouterDeploymentDefinition vpcRouterDeploymentDefinition,
                                                                 Network guestNetwork);
}
