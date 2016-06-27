package com.cloud.network.vpn;

import com.cloud.network.dao.Site2SiteVpnConnectionVO;
import com.cloud.vm.DomainRouterVO;

import java.util.List;

public interface Site2SiteVpnManager extends Site2SiteVpnService {
    boolean cleanupVpnConnectionByVpc(long vpcId);

    boolean cleanupVpnGatewayByVpc(long vpcId);

    void markDisconnectVpnConnByVpc(long vpcId);

    List<Site2SiteVpnConnectionVO> getConnectionsForRouter(DomainRouterVO router);

    boolean deleteCustomerGatewayByAccount(long accountId);

    void reconnectDisconnectedVpnByVpc(Long vpcId);
}
