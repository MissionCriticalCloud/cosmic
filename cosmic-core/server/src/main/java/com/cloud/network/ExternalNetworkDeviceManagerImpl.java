package com.cloud.network;

import com.cloud.agent.AgentManager;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.host.Host;
import com.cloud.host.dao.HostDao;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.ExternalLoadBalancerDeviceDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.InlineLoadBalancerNicMapDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkExternalLoadBalancerDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.command.admin.network.AddNetworkDeviceCmd;
import org.apache.cloudstack.api.command.admin.network.DeleteNetworkDeviceCmd;
import org.apache.cloudstack.api.command.admin.network.ListNetworkDeviceCmd;
import org.apache.cloudstack.api.response.NetworkDeviceResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.network.ExternalNetworkDeviceManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExternalNetworkDeviceManagerImpl extends ManagerBase implements ExternalNetworkDeviceManager {

    private static final Logger s_logger = LoggerFactory.getLogger(ExternalNetworkDeviceManagerImpl.class);
    @Inject
    AgentManager _agentMgr;
    @Inject
    NetworkModel _networkMgr;
    @Inject
    HostDao _hostDao;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    AccountDao _accountDao;
    @Inject
    DomainRouterDao _routerDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    VlanDao _vlanDao;
    @Inject
    UserStatisticsDao _userStatsDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    PortForwardingRulesDao _portForwardingRulesDao;
    @Inject
    LoadBalancerDao _loadBalancerDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    NetworkOfferingDao _networkOfferingDao;
    @Inject
    NicDao _nicDao;
    @Inject
    VpnUserDao _vpnUsersDao;
    @Inject
    InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Inject
    ExternalLoadBalancerDeviceDao _externalLoadBalancerDeviceDao;
    @Inject
    ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Inject
    NetworkExternalLoadBalancerDao _networkExternalLBDao;
    @Inject
    NetworkExternalFirewallDao _networkExternalFirewallDao;
    ScheduledExecutorService _executor;
    int _externalNetworkStatsInterval;

    @Override
    public Host addNetworkDevice(final AddNetworkDeviceCmd cmd) {
        final Map paramList = cmd.getParamList();
        if (paramList == null) {
            throw new CloudRuntimeException("Parameter list is null");
        }

        final Collection paramsCollection = paramList.values();
        final HashMap params = (HashMap) (paramsCollection.toArray())[0];
        return null;
    }

    @Override
    public NetworkDeviceResponse getApiResponse(final Host device) {
        return null;
    }

    @Override
    public List<Host> listNetworkDevice(final ListNetworkDeviceCmd cmd) {
        final Map paramList = cmd.getParamList();
        if (paramList == null) {
            throw new CloudRuntimeException("Parameter list is null");
        }

        final List<Host> res;
        final Collection paramsCollection = paramList.values();
        final HashMap params = (HashMap) (paramsCollection.toArray())[0];
        if (NetworkDevice.ExternalDhcp.getName().equalsIgnoreCase(cmd.getDeviceType())) {
            final Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            final Long podId = Long.parseLong((String) params.get(ApiConstants.POD_ID));
            res = listNetworkDevice(zoneId, null, podId, Host.Type.ExternalDhcp);
        } else if (cmd.getDeviceType() == null) {
            final Long zoneId = Long.parseLong((String) params.get(ApiConstants.ZONE_ID));
            final Long podId = Long.parseLong((String) params.get(ApiConstants.POD_ID));
            final Long physicalNetworkId = (params.get(ApiConstants.PHYSICAL_NETWORK_ID) == null) ? Long.parseLong((String) params.get(ApiConstants.PHYSICAL_NETWORK_ID)) : null;
            final List<Host> deviceAll = new ArrayList<>();
            deviceAll.addAll(listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.ExternalDhcp));
            deviceAll.addAll(listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.ExternalLoadBalancer));
            deviceAll.addAll(listNetworkDevice(zoneId, physicalNetworkId, podId, Host.Type.ExternalFirewall));
            res = deviceAll;
        } else {
            throw new CloudRuntimeException("Unknown network device type:" + cmd.getDeviceType());
        }

        return res;
    }

    private List<Host> listNetworkDevice(final Long zoneId, final Long physicalNetworkId, final Long podId, final Host.Type type) {
        return null;
    }

    @Override
    public boolean deleteNetworkDevice(final DeleteNetworkDeviceCmd cmd) {
        return true;
    }
}
