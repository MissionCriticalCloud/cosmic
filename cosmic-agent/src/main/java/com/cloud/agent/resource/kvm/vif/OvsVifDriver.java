package com.cloud.agent.resource.kvm.vif;

import com.cloud.agent.resource.kvm.xml.LibvirtVmDef;
import com.cloud.agent.resource.kvm.xml.LibvirtVmDef.InterfaceDef;
import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.to.NicTO;
import com.cloud.model.enumeration.BroadcastDomainType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import javax.naming.ConfigurationException;
import java.util.Map;

import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvsVifDriver extends VifDriverBase {

    private final Logger logger = LoggerFactory.getLogger(OvsVifDriver.class);

    private int timeout;

    @Override
    public void configure(final Map<String, Object> params) throws ConfigurationException {
        super.configure(params);

        String networkScriptsDir = (String) params.get("network.scripts.dir");
        if (networkScriptsDir == null) {
            networkScriptsDir = "scripts/vm/network/vnet";
        }

        this.timeout = ((Integer) params.get("scripts.timeout")) * 1000;

        createControlNetwork(this.bridges.get("linklocal"));
    }

    @Override
    public InterfaceDef plug(final NicTO nic, final String guestOsType, final String nicAdapter)
            throws InternalErrorException, LibvirtException {
        this.logger.debug("plugging nic=" + nic);

        final LibvirtVmDef.InterfaceDef intf = new LibvirtVmDef.InterfaceDef();
        intf.setVirtualPortType("openvswitch");

        String vlanId = null;
        String logicalSwitchUuid = null;
        if (nic.getBroadcastType() == BroadcastDomainType.Vlan) {
            vlanId = BroadcastDomainType.getValue(nic.getBroadcastUri());
        } else if (nic.getBroadcastType() == BroadcastDomainType.Lswitch) {
            logicalSwitchUuid = BroadcastDomainType.getValue(nic.getBroadcastUri());
        } else if (nic.getBroadcastType() == BroadcastDomainType.Pvlan) {
            // TODO consider moving some of this functionality from NetUtils to Networks....
            vlanId = NetUtils.getPrimaryPvlanFromUri(nic.getBroadcastUri());
        }
        final String trafficLabel = nic.getName();
        if (nic.getType() == TrafficType.Guest) {
            final Integer networkRateKBps = nic.getNetworkRateMbps() != null && nic.getNetworkRateMbps().intValue() != -1
                    ? nic.getNetworkRateMbps().intValue() * 128 : 0;
            if ((nic.getBroadcastType() == BroadcastDomainType.Vlan
                    || nic.getBroadcastType() == BroadcastDomainType.Pvlan)
                    && !vlanId.equalsIgnoreCase("untagged")) {
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    this.logger.debug("creating a vlan dev and bridge for guest traffic per traffic label " + trafficLabel);
                    intf.defBridgeNet(this.pifs.get(trafficLabel), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                } else {
                    intf.defBridgeNet(this.pifs.get("private"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                }
            } else if (nic.getBroadcastType() == BroadcastDomainType.Lswitch) {
                this.logger.debug("nic " + nic + " needs to be connected to LogicalSwitch " + logicalSwitchUuid);
                intf.setVirtualPortInterfaceId(nic.getUuid());
                final String brName = trafficLabel != null && !trafficLabel.isEmpty() ? this.pifs.get(trafficLabel)
                        : this.pifs.get("private");
                intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            } else if (nic.getBroadcastType() == BroadcastDomainType.Vswitch) {
                final String vnetId = BroadcastDomainType.getValue(nic.getBroadcastUri());
                final String brName = "OVSTunnel" + vnetId;
                this.logger.debug("nic " + nic + " needs to be connected to LogicalSwitch " + brName);
                intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            } else if (nic.getBroadcastType() == BroadcastDomainType.Vsp) {
                intf.setVirtualPortInterfaceId(nic.getUuid());
                final String brName = trafficLabel != null && !trafficLabel.isEmpty() ? this.pifs.get(trafficLabel)
                        : this.pifs.get("private");
                intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            } else {
                intf.defBridgeNet(this.bridges.get("guest"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                        networkRateKBps);
            }
        } else if (nic.getType() == TrafficType.Control) {
            /* Make sure the network is still there */
            createControlNetwork(this.bridges.get("linklocal"));
            intf.defBridgeNet(this.bridges.get("linklocal"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter));
        } else if (nic.getType() == TrafficType.Public) {
            final Integer networkRateKBps = nic.getNetworkRateMbps() != null && nic.getNetworkRateMbps().intValue() != -1
                    ? nic.getNetworkRateMbps().intValue() * 128 : 0;
            if (nic.getBroadcastType() == BroadcastDomainType.Vlan && !vlanId.equalsIgnoreCase("untagged")) {
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    this.logger.debug("creating a vlan dev and bridge for public traffic per traffic label " + trafficLabel);
                    intf.defBridgeNet(this.pifs.get(trafficLabel), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                } else {
                    intf.defBridgeNet(this.pifs.get("public"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                }
            } else {
                intf.defBridgeNet(this.bridges.get("public"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                        networkRateKBps);
            }
        } else if (nic.getType() == TrafficType.Management) {
            intf.defBridgeNet(this.bridges.get("private"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter));
        } else if (nic.getType() == TrafficType.Storage) {
            final String storageBrName = nic.getName() == null ? this.bridges.get("private") : nic.getName();
            intf.defBridgeNet(storageBrName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter));
        }
        return intf;
    }

    @Override
    public void unplug(final InterfaceDef iface) {
        // Libvirt apparently takes care of this, see BridgeVifDriver unplug
    }

    private void createControlNetwork(final String privBrName) {
        deleteExitingLinkLocalRouteTable(privBrName);
        if (!isBridgeExists(privBrName)) {
            Script.runSimpleBashScript("ovs-vsctl add-br " + privBrName + "; ip link set " + privBrName
                    + " up; ip address add 169.254.0.1/16 dev " + privBrName, this.timeout);
        }
    }

    private void deleteExitingLinkLocalRouteTable(final String linkLocalBr) {
        final Script command = new Script("/bin/bash", this.timeout);
        command.add("-c");
        command.add("ip route | grep " + NetUtils.getLinkLocalCIDR());
        final OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        final String result = command.execute(parser);
        boolean foundLinkLocalBr = false;
        if (result == null && parser.getLines() != null) {
            final String[] lines = parser.getLines().split("\\n");
            for (final String line : lines) {
                final String[] tokens = line.split(" ");
                if (!tokens[2].equalsIgnoreCase(linkLocalBr)) {
                    Script.runSimpleBashScript("ip route del " + NetUtils.getLinkLocalCIDR());
                } else {
                    foundLinkLocalBr = true;
                }
            }
        }
        if (!foundLinkLocalBr) {
            Script.runSimpleBashScript("ip address add 169.254.0.1/16 dev " + linkLocalBr + ";" + "ip route add "
                    + NetUtils.getLinkLocalCIDR() + " dev " + linkLocalBr + " src " + NetUtils.getLinkLocalGateway());
        }
    }

    private boolean isBridgeExists(final String bridgeName) {
        final Script command = new Script("/bin/sh", this.timeout);
        command.add("-c");
        command.add("ovs-vsctl br-exists " + bridgeName);
        final String result = command.execute(null);
        if ("0".equals(result)) {
            return true;
        } else {
            return false;
        }
    }
}
