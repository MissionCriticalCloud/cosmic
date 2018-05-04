package com.cloud.network.router;

import com.cloud.framework.config.ConfigKey;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VirtualNetworkApplianceService;
import com.cloud.utils.component.Manager;
import com.cloud.vm.DomainRouterVO;

import java.util.List;

/**
 * NetworkManager manages the network for the different end users.
 */
public interface VirtualNetworkApplianceManager extends Manager, VirtualNetworkApplianceService {

    String RouterTemplateXenCK = "router.template.xenserver";
    String RouterTemplateKvmCK = "router.template.kvm";

    ConfigKey<String> RouterTemplateXen = new ConfigKey<>(String.class, RouterTemplateXenCK, "Advanced", "SystemVM Template (XenServer)",
            "Name of the default router template on Xenserver.", true, ConfigKey.Scope.Zone, null);
    ConfigKey<String> RouterTemplateKvm = new ConfigKey<>(String.class, RouterTemplateKvmCK, "Advanced", "SystemVM Template (KVM)",
            "Name of the default router template on KVM.", true, ConfigKey.Scope.Zone, null);

    int DEFAULT_ROUTER_VM_RAMSIZE = 256;            // 256M
    int DEFAULT_ROUTER_CPU_MHZ = 500;               // 500 MHz

    boolean startRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers) throws ResourceUnavailableException;

    boolean deleteRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers) throws ResourceUnavailableException;

    List<VirtualRouter> getRoutersForNetwork(long networkId);

    VirtualRouter stop(VirtualRouter router, boolean forced, User callingUser, Account callingAccount) throws ConcurrentOperationException, ResourceUnavailableException;

    String getDnsBasicZoneUpdate();

    boolean prepareAggregatedExecution(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException;

    boolean completeAggregatedExecution(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException;

    boolean updateVR(final Vpc vpc, final DomainRouterVO router);
}
