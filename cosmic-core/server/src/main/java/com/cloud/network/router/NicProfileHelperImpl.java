package com.cloud.network.router;

import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.vpc.PrivateIpAddress;
import com.cloud.network.vpc.PrivateIpVO;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpc.dao.PrivateIpDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.VMInstanceDao;

import javax.ejb.Local;
import javax.inject.Inject;
import java.net.URI;

import org.cloud.network.router.deployment.RouterDeploymentDefinition;

@Local(value = {NicProfileHelper.class})
public class NicProfileHelperImpl implements NicProfileHelper {

    @Inject
    protected NetworkModel _networkModel;
    @Inject
    protected VpcManager _vpcMgr;
    @Inject
    protected NicDao _nicDao;
    @Inject
    protected IpAddressManager _ipAddrMgr;
    @Inject
    private VMInstanceDao _vmDao;
    @Inject
    private PrivateIpDao _privateIpDao;

    @Override
    @DB
    public NicProfile createPrivateNicProfileForGateway(final VpcGateway privateGateway, final VirtualRouter router) {
        final Network privateNetwork = _networkModel.getNetwork(privateGateway.getNetworkId());

        PrivateIpVO ipVO = _privateIpDao.allocateIpAddress(privateNetwork.getDataCenterId(), privateNetwork.getId(), privateGateway.getIp4Address());

        final Long vpcId = privateGateway.getVpcId();
        final Vpc activeVpc = _vpcMgr.getActiveVpc(vpcId);
        if (activeVpc.isRedundant() && ipVO == null) {
            ipVO = _privateIpDao.findByIpAndVpcId(vpcId, privateGateway.getIp4Address());
        }

        Nic privateNic = null;

        if (ipVO != null) {
            privateNic = _nicDao.findByIp4AddressAndNetworkId(ipVO.getIpAddress(), privateNetwork.getId());
        }

        NicProfile privateNicProfile = new NicProfile();

        if (privateNic != null) {
            privateNicProfile =
                    new NicProfile(privateNic, privateNetwork, privateNic.getBroadcastUri(), privateNic.getIsolationUri(), _networkModel.getNetworkRate(
                            privateNetwork.getId(), router.getId()), _networkModel.isSecurityGroupSupportedInNetwork(privateNetwork), _networkModel.getNetworkTag(
                            router.getHypervisorType(), privateNetwork));

            if (router.getIsRedundantRouter()) {
                final String newMacAddress = NetUtils.long2Mac(NetUtils.createSequenceBasedMacAddress(ipVO.getMacAddress()));
                privateNicProfile.setMacAddress(newMacAddress);
            }
        } else {
            final String netmask = NetUtils.getCidrNetmask(privateNetwork.getCidr());
            final PrivateIpAddress ip =
                    new PrivateIpAddress(ipVO, privateNetwork.getBroadcastUri().toString(), privateNetwork.getGateway(), netmask,
                            NetUtils.long2Mac(NetUtils.createSequenceBasedMacAddress(ipVO.getMacAddress())));

            final URI netUri = BroadcastDomainType.fromString(ip.getBroadcastUri());
            privateNicProfile.setIPv4Address(ip.getIpAddress());
            privateNicProfile.setIPv4Gateway(ip.getGateway());
            privateNicProfile.setIPv4Netmask(ip.getNetmask());
            privateNicProfile.setIsolationUri(netUri);
            privateNicProfile.setBroadcastUri(netUri);
            // can we solve this in setBroadcastUri()???
            // or more plugable construct is desirable
            privateNicProfile.setBroadcastType(BroadcastDomainType.getSchemeValue(netUri));
            privateNicProfile.setFormat(AddressFormat.Ip4);
            privateNicProfile.setReservationId(String.valueOf(ip.getBroadcastUri()));
            privateNicProfile.setMacAddress(ip.getMacAddress());
        }

        return privateNicProfile;
    }

    @Override
    public NicProfile createGuestNicProfileForVpcRouter(final RouterDeploymentDefinition vpcRouterDeploymentDefinition, final Network guestNetwork) {
        final NicProfile guestNic = new NicProfile();

        // Redundant VPCs should not acquire the gateway ip because that is the VIP between the two routers to which guest VMs connect
        // VPCs without sourcenat service also should not acquire the gateway ip because it is in use by an external device on the network
        if (vpcRouterDeploymentDefinition.isRedundant() || !vpcRouterDeploymentDefinition.hasSourceNatService()) {
            guestNic.setIPv4Address(_ipAddrMgr.acquireGuestIpAddress(guestNetwork, null));
        } else {
            guestNic.setIPv4Address(guestNetwork.getGateway());
        }

        guestNic.setBroadcastUri(guestNetwork.getBroadcastUri());
        guestNic.setBroadcastType(guestNetwork.getBroadcastDomainType());
        guestNic.setIsolationUri(guestNetwork.getBroadcastUri());
        guestNic.setMode(guestNetwork.getMode());
        final String gatewayCidr = guestNetwork.getCidr();
        guestNic.setIPv4Netmask(NetUtils.getCidrNetmask(gatewayCidr));

        return guestNic;
    }
}
