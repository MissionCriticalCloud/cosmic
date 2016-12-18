package com.cloud.network.router;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.framework.config.ConfigKey;
import com.cloud.network.Network;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VirtualNetworkApplianceService;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.component.Manager;
import com.cloud.vm.DomainRouterVO;

import java.util.List;

/**
 * NetworkManager manages the network for the different end users.
 */
public interface VirtualNetworkApplianceManager extends Manager, VirtualNetworkApplianceService {

    String RouterTemplateXenCK = "router.template.xenserver";
    String RouterTemplateKvmCK = "router.template.kvm";
    String SetServiceMonitorCK = "network.router.EnableServiceMonitoring";
    String RouterAlertsCheckIntervalCK = "router.alerts.check.interval";

    ConfigKey<String> RouterTemplateXen = new ConfigKey<>(String.class, RouterTemplateXenCK, "Advanced", "SystemVM Template (XenServer)",
            "Name of the default router template on Xenserver.", true, ConfigKey.Scope.Zone, null);
    ConfigKey<String> RouterTemplateKvm = new ConfigKey<>(String.class, RouterTemplateKvmCK, "Advanced", "SystemVM Template (KVM)",
            "Name of the default router template on KVM.", true, ConfigKey.Scope.Zone, null);

    ConfigKey<String> SetServiceMonitor = new ConfigKey<>(String.class, SetServiceMonitorCK, "Advanced", "true",
            "service monitoring in router enable/disable option, default true", true, ConfigKey.Scope.Zone, null);

    ConfigKey<Integer> RouterAlertsCheckInterval = new ConfigKey<>(Integer.class, RouterAlertsCheckIntervalCK, "Advanced", "1800",
            "Interval (in seconds) to check for alerts in Virtual Router.", false, ConfigKey.Scope.Global, null);
    ConfigKey<Boolean> routerVersionCheckEnabled = new ConfigKey<>("Advanced", Boolean.class, "router.version.check", "true",
            "If true, router minimum required version is checked before sending command", false);
    ConfigKey<Boolean> UseExternalDnsServers = new ConfigKey<>(Boolean.class, "use.external.dns", "Advanced", "false",
            "Bypass internal dns, use external dns1 and dns2", true, ConfigKey.Scope.Zone, null);

    int DEFAULT_ROUTER_VM_RAMSIZE = 256;            // 256M
    int DEFAULT_ROUTER_CPU_MHZ = 500;                // 500 MHz
    boolean USE_POD_VLAN = false;
    int DEFAULT_PRIORITY = 100;
    int DEFAULT_DELTA = 2;

    boolean startRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers) throws ResourceUnavailableException;

    boolean deleteRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers) throws ResourceUnavailableException;

    List<VirtualRouter> getRoutersForNetwork(long networkId);

    VirtualRouter stop(VirtualRouter router, boolean forced, User callingUser, Account callingAccount) throws ConcurrentOperationException, ResourceUnavailableException;

    String getDnsBasicZoneUpdate();

    boolean removeDhcpSupportForSubnet(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException;

    boolean prepareAggregatedExecution(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException;

    boolean completeAggregatedExecution(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException;
}
