package com.cloud.network.router;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.VpcVirtualNetworkApplianceService;
import com.cloud.network.vpc.PrivateGateway;
import com.cloud.vm.DomainRouterVO;

import java.util.List;

public interface VpcVirtualNetworkApplianceManager extends VirtualNetworkApplianceManager, VpcVirtualNetworkApplianceService {

    /**
     * @param gateway
     * @param router
     * @return
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    boolean destroyPrivateGateway(PrivateGateway gateway, VirtualRouter router) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * @param conn
     * @param routers
     * @return
     * @throws ResourceUnavailableException
     */
    boolean startSite2SiteVpn(Site2SiteVpnConnection conn, VirtualRouter router) throws ResourceUnavailableException;

    /**
     * @param conn
     * @param routers
     * @return
     * @throws ResourceUnavailableException
     */
    boolean stopSite2SiteVpn(Site2SiteVpnConnection conn, VirtualRouter router) throws ResourceUnavailableException;

    /**
     * @param vpcId
     * @return
     */
    List<DomainRouterVO> getVpcRouters(long vpcId);

    /**
     * @param vpn
     * @param router
     * @return
     * @throws ResourceUnavailableException
     */
    boolean startRemoteAccessVpn(RemoteAccessVpn vpn, VirtualRouter router) throws ResourceUnavailableException;

    /**
     * @param vpn
     * @param router
     * @return
     * @throws ResourceUnavailableException
     */
    boolean stopRemoteAccessVpn(RemoteAccessVpn vpn, VirtualRouter router) throws ResourceUnavailableException;
}
