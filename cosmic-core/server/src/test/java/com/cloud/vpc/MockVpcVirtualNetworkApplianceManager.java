package com.cloud.vpc;

import com.cloud.api.command.admin.router.UpgradeRouterCmd;
import com.cloud.api.command.admin.router.UpgradeRouterTemplateCmd;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.network.vpc.PrivateGateway;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.VpcVirtualNetworkApplianceService;
import com.cloud.network.router.VpcVirtualNetworkApplianceManager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.VirtualMachineProfile;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MockVpcVirtualNetworkApplianceManager extends ManagerBase implements VpcVirtualNetworkApplianceManager, VpcVirtualNetworkApplianceService {

    /* (non-Javadoc)
     * @see com.cloud.network.router.VirtualNetworkApplianceManager#startRemoteAccessVpn(com.cloud.legacymodel.network.Network, com.cloud.network.RemoteAccessVpn, java.util.List)
     */
    @Override
    public boolean startRemoteAccessVpn(final Network network, final RemoteAccessVpn vpn, final List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VirtualNetworkApplianceManager#deleteRemoteAccessVpn(com.cloud.legacymodel.network.Network, com.cloud.network.RemoteAccessVpn, java.util.List)
     */
    @Override
    public boolean deleteRemoteAccessVpn(final Network network, final RemoteAccessVpn vpn, final List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VirtualNetworkApplianceManager#getRoutersForNetwork(long)
     */
    @Override
    public List<VirtualRouter> getRoutersForNetwork(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VirtualNetworkApplianceManager#stop(com.cloud.legacymodel.network.VirtualRouter, boolean, com.cloud.legacymodel.user.User, com.cloud.legacymodel.user.Account)
     */
    @Override
    public VirtualRouter stop(final VirtualRouter router, final boolean forced, final User callingUser, final Account callingAccount) throws ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VirtualNetworkApplianceManager#getDnsBasicZoneUpdate()
     */
    @Override
    public String getDnsBasicZoneUpdate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean prepareAggregatedExecution(final Network network, final List<DomainRouterVO> routers) throws AgentUnavailableException {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean completeAggregatedExecution(final Network network, final List<DomainRouterVO> routers) throws AgentUnavailableException {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VirtualNetworkApplianceService#startRouter(long, boolean)
     */
    @Override
    public VirtualRouter startRouter(final long routerId, final boolean reprogramNetwork) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VirtualNetworkApplianceService#rebootRouter(long, boolean)
     */
    @Override
    public VirtualRouter rebootRouter(final long routerId, final boolean reprogramNetwork) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VirtualNetworkApplianceService#upgradeRouter(com.cloud.api.commands.UpgradeRouterCmd)
     */
    @Override
    public VirtualRouter upgradeRouter(final UpgradeRouterCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VirtualNetworkApplianceService#stopRouter(long, boolean)
     */
    @Override
    public VirtualRouter stopRouter(final long routerId, final boolean forced) throws ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VirtualNetworkApplianceService#startRouter(long)
     */
    @Override
    public VirtualRouter startRouter(final long id) throws ResourceUnavailableException, InsufficientCapacityException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VirtualNetworkApplianceService#destroyRouter(long, com.cloud.legacymodel.user.Account, java.lang.Long)
     */
    @Override
    public VirtualRouter destroyRouter(final long routerId, final Account caller, final Long callerUserId) throws ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VirtualRouter findRouter(final long routerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> upgradeRouterTemplate(final UpgradeRouterTemplateCmd cmd) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VpcVirtualNetworkApplianceService#addVpcRouterToGuestNetwork(com.cloud.legacymodel.network.VirtualRouter, com.cloud.legacymodel.network.Network, boolean)
     */
    @Override
    public boolean addVpcRouterToGuestNetwork(final VirtualRouter router, final Network network, final Map<VirtualMachineProfile.Param, Object> params)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.VpcVirtualNetworkApplianceService#removeVpcRouterFromGuestNetwork(com.cloud.legacymodel.network.VirtualRouter, com.cloud.legacymodel.network.Network, boolean)
     */
    @Override
    public boolean removeVpcRouterFromGuestNetwork(final VirtualRouter router, final Network network) throws ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VpcVirtualNetworkApplianceManager#destroyPrivateGateway(com.cloud.legacymodel.network.vpc.PrivateGateway, com.cloud.legacymodel.network.VirtualRouter)
     */
    @Override
    public boolean destroyPrivateGateway(final PrivateGateway gateway, final VirtualRouter router) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VpcVirtualNetworkApplianceManager#startSite2SiteVpn(com.cloud.network.Site2SiteVpnConnection, com.cloud.legacymodel.network.VirtualRouter)
     */
    @Override
    public boolean startSite2SiteVpn(final Site2SiteVpnConnection conn, final VirtualRouter router) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VpcVirtualNetworkApplianceManager#refreshSite2SiteVpn(com.cloud.network.Site2SiteVpnConnection, com.cloud.legacymodel.network.VirtualRouter)
     */
    @Override
    public boolean refreshSite2SiteVpn(final Site2SiteVpnConnection conn, final VirtualRouter router) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.router.VpcVirtualNetworkApplianceManager#stopSite2SiteVpn(com.cloud.network.Site2SiteVpnConnection, com.cloud.legacymodel.network.VirtualRouter)
     */
    @Override
    public boolean stopSite2SiteVpn(final Site2SiteVpnConnection conn, final VirtualRouter router) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<DomainRouterVO> getVpcRouters(final long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean startRemoteAccessVpn(final RemoteAccessVpn vpn, final VirtualRouter router) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopRemoteAccessVpn(final RemoteAccessVpn vpn, final VirtualRouter router) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateVR(final Vpc vpc, final DomainRouterVO router) {
        // TODO Auto-generated method stub
        return false;
    }
}
