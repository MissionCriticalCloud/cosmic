package com.cloud.vpc;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.network.dao.Site2SiteVpnConnectionVO;
import com.cloud.network.vpn.Site2SiteVpnManager;
import com.cloud.network.vpn.Site2SiteVpnService;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import com.cloud.vm.DomainRouterVO;
import org.apache.cloudstack.api.command.user.vpn.CreateVpnConnectionCmd;
import org.apache.cloudstack.api.command.user.vpn.CreateVpnCustomerGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.CreateVpnGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.DeleteVpnConnectionCmd;
import org.apache.cloudstack.api.command.user.vpn.DeleteVpnCustomerGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.DeleteVpnGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.ListVpnConnectionsCmd;
import org.apache.cloudstack.api.command.user.vpn.ListVpnCustomerGatewaysCmd;
import org.apache.cloudstack.api.command.user.vpn.ListVpnGatewaysCmd;
import org.apache.cloudstack.api.command.user.vpn.ResetVpnConnectionCmd;
import org.apache.cloudstack.api.command.user.vpn.UpdateVpnCustomerGatewayCmd;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MockSite2SiteVpnManagerImpl extends ManagerBase implements Site2SiteVpnManager, Site2SiteVpnService {

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#createVpnGateway(org.apache.cloudstack.api.commands.CreateVpnGatewayCmd)
     */
    @Override
    public Site2SiteVpnGateway createVpnGateway(final CreateVpnGatewayCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#createCustomerGateway(org.apache.cloudstack.api.commands.CreateVpnCustomerGatewayCmd)
     */
    @Override
    public Site2SiteCustomerGateway createCustomerGateway(final CreateVpnCustomerGatewayCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#startVpnConnection(long)
     */
    @Override
    public Site2SiteVpnConnection startVpnConnection(final long id) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#getVpnGateway(java.lang.Long)
     */
    @Override
    public Site2SiteVpnGateway getVpnGateway(final Long vpnGatewayId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#createVpnConnection(org.apache.cloudstack.api.commands.CreateVpnConnectionCmd)
     */
    @Override
    public Site2SiteVpnConnection createVpnConnection(final CreateVpnConnectionCmd cmd) throws NetworkRuleConflictException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#deleteCustomerGateway(org.apache.cloudstack.api.commands.DeleteVpnCustomerGatewayCmd)
     */
    @Override
    public boolean deleteCustomerGateway(final DeleteVpnCustomerGatewayCmd deleteVpnCustomerGatewayCmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#deleteVpnGateway(org.apache.cloudstack.api.commands.DeleteVpnGatewayCmd)
     */
    @Override
    public boolean deleteVpnGateway(final DeleteVpnGatewayCmd deleteVpnGatewayCmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#deleteVpnConnection(org.apache.cloudstack.api.commands.DeleteVpnConnectionCmd)
     */
    @Override
    public boolean deleteVpnConnection(final DeleteVpnConnectionCmd deleteVpnConnectionCmd) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#resetVpnConnection(org.apache.cloudstack.api.commands.ResetVpnConnectionCmd)
     */
    @Override
    public Site2SiteVpnConnection resetVpnConnection(final ResetVpnConnectionCmd resetVpnConnectionCmd) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#searchForCustomerGateways(org.apache.cloudstack.api.commands.ListVpnCustomerGatewaysCmd)
     */
    @Override
    public Pair<List<? extends Site2SiteCustomerGateway>, Integer> searchForCustomerGateways(final ListVpnCustomerGatewaysCmd listVpnCustomerGatewaysCmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#searchForVpnGateways(org.apache.cloudstack.api.commands.ListVpnGatewaysCmd)
     */
    @Override
    public Pair<List<? extends Site2SiteVpnGateway>, Integer> searchForVpnGateways(final ListVpnGatewaysCmd listVpnGatewaysCmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#searchForVpnConnections(org.apache.cloudstack.api.commands.ListVpnConnectionsCmd)
     */
    @Override
    public Pair<List<? extends Site2SiteVpnConnection>, Integer> searchForVpnConnections(final ListVpnConnectionsCmd listVpnConnectionsCmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnService#updateCustomerGateway(org.apache.cloudstack.api.commands.UpdateVpnCustomerGatewayCmd)
     */
    @Override
    public Site2SiteCustomerGateway updateCustomerGateway(final UpdateVpnCustomerGatewayCmd updateVpnCustomerGatewayCmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Site2SiteVpnConnection updateVpnConnection(final long id, final String customId, final Boolean forDisplay) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Site2SiteVpnGateway updateVpnGateway(final Long id, final String customId, final Boolean forDisplay) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnManager#cleanupVpnConnectionByVpc(long)
     */
    @Override
    public boolean cleanupVpnConnectionByVpc(final long vpcId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnManager#cleanupVpnGatewayByVpc(long)
     */
    @Override
    public boolean cleanupVpnGatewayByVpc(final long vpcId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnManager#markDisconnectVpnConnByVpc(long)
     */
    @Override
    public void markDisconnectVpnConnByVpc(final long vpcId) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnManager#getConnectionsForRouter(com.cloud.vm.DomainRouterVO)
     */
    @Override
    public List<Site2SiteVpnConnectionVO> getConnectionsForRouter(final DomainRouterVO router) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnManager#deleteCustomerGatewayByAccount(long)
     */
    @Override
    public boolean deleteCustomerGatewayByAccount(final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpn.Site2SiteVpnManager#reconnectDisconnectedVpnByVpc(java.lang.Long)
     */
    @Override
    public void reconnectDisconnectedVpnByVpc(final Long vpcId) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }
}
