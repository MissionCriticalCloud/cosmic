package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.router.VirtualRouter;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.Nic;
import com.cloud.vm.dao.NicDao;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VpcIpAssociationRules extends RuleApplier {

    private static final Logger s_logger = LoggerFactory.getLogger(VpcIpAssociationRules.class);

    private final List<? extends PublicIpAddress> _ipAddresses;

    private Map<String, String> _vlanMacAddress;

    private List<PublicIpAddress> _ipsToSend;

    public VpcIpAssociationRules(final Network network, final List<? extends PublicIpAddress> ipAddresses) {
        super(network);
        _ipAddresses = ipAddresses;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        _vlanMacAddress = new HashMap<>();
        _ipsToSend = new ArrayList<>();

        final NicDao nicDao = visitor.getVirtualNetworkApplianceFactory().getNicDao();
        for (final PublicIpAddress ipAddr : _ipAddresses) {
            final String broadcastURI = BroadcastDomainType.Vlan.toUri(ipAddr.getVlanTag()).toString();
            final Nic nic = nicDao.findByNetworkIdInstanceIdAndBroadcastUri(ipAddr.getNetworkId(), _router.getId(), broadcastURI);

            String macAddress = null;
            if (nic == null) {
                if (ipAddr.getState() != IpAddress.State.Releasing) {
                    throw new CloudRuntimeException("Unable to find the nic in network " + ipAddr.getNetworkId() + "  to apply the ip address " + ipAddr + " for");
                }
                s_logger.debug("Not sending release for ip address " + ipAddr + " as its nic is already gone from VPC router " + _router);
            } else {
                macAddress = nic.getMacAddress();
                _vlanMacAddress.put(BroadcastDomainType.getValue(BroadcastDomainType.fromString(ipAddr.getVlanTag())), macAddress);
                _ipsToSend.add(ipAddr);
            }
        }

        return visitor.visit(this);
    }

    public List<? extends PublicIpAddress> getIpAddresses() {
        return _ipAddresses;
    }

    public Map<String, String> getVlanMacAddress() {
        return _vlanMacAddress;
    }

    public List<PublicIpAddress> getIpsToSend() {
        return _ipsToSend;
    }
}
