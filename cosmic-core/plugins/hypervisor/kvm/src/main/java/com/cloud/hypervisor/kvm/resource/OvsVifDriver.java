package com.cloud.hypervisor.kvm.resource;

import com.cloud.agent.api.to.NicTO;
import com.cloud.exception.InternalErrorException;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InterfaceDef;
import com.cloud.network.Networks;
import com.cloud.utils.NumbersUtil;
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

        final String value = (String) params.get("scripts.timeout");
        timeout = NumbersUtil.parseInt(value, 30 * 60) * 1000;

        createControlNetwork(bridges.get("linklocal"));
    }

    @Override
    public InterfaceDef plug(final NicTO nic, final String guestOsType, final String nicAdapter)
            throws InternalErrorException, LibvirtException {
        logger.debug("plugging nic=" + nic);

        final LibvirtVmDef.InterfaceDef intf = new LibvirtVmDef.InterfaceDef();
        intf.setVirtualPortType("openvswitch");

        String vlanId = null;
        String logicalSwitchUuid = null;
        if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vlan) {
            vlanId = Networks.BroadcastDomainType.getValue(nic.getBroadcastUri());
        } else if (nic.getBroadcastType() == Networks.BroadcastDomainType.Lswitch) {
            logicalSwitchUuid = Networks.BroadcastDomainType.getValue(nic.getBroadcastUri());
        } else if (nic.getBroadcastType() == Networks.BroadcastDomainType.Pvlan) {
            // TODO consider moving some of this functionality from NetUtils to Networks....
            vlanId = NetUtils.getPrimaryPvlanFromUri(nic.getBroadcastUri());
        }
        final String trafficLabel = nic.getName();
        if (nic.getType() == Networks.TrafficType.Guest) {
            final Integer networkRateKBps = nic.getNetworkRateMbps() != null && nic.getNetworkRateMbps().intValue() != -1
                    ? nic.getNetworkRateMbps().intValue() * 128 : 0;
            if ((nic.getBroadcastType() == Networks.BroadcastDomainType.Vlan
                    || nic.getBroadcastType() == Networks.BroadcastDomainType.Pvlan)
                    && !vlanId.equalsIgnoreCase("untagged")) {
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    logger.debug("creating a vlan dev and bridge for guest traffic per traffic label " + trafficLabel);
                    intf.defBridgeNet(pifs.get(trafficLabel), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                } else {
                    intf.defBridgeNet(pifs.get("private"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                }
            } else if (nic.getBroadcastType() == Networks.BroadcastDomainType.Lswitch) {
                logger.debug("nic " + nic + " needs to be connected to LogicalSwitch " + logicalSwitchUuid);
                intf.setVirtualPortInterfaceId(nic.getUuid());
                final String brName = trafficLabel != null && !trafficLabel.isEmpty() ? pifs.get(trafficLabel)
                        : pifs.get("private");
                intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            } else if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vswitch) {
                final String vnetId = Networks.BroadcastDomainType.getValue(nic.getBroadcastUri());
                final String brName = "OVSTunnel" + vnetId;
                logger.debug("nic " + nic + " needs to be connected to LogicalSwitch " + brName);
                intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            } else if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vsp) {
                intf.setVirtualPortInterfaceId(nic.getUuid());
                final String brName = trafficLabel != null && !trafficLabel.isEmpty() ? pifs.get(trafficLabel)
                        : pifs.get("private");
                intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            } else {
                intf.defBridgeNet(bridges.get("guest"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                        networkRateKBps);
            }
        } else if (nic.getType() == Networks.TrafficType.Control) {
      /* Make sure the network is still there */
            createControlNetwork(bridges.get("linklocal"));
            intf.defBridgeNet(bridges.get("linklocal"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter));
        } else if (nic.getType() == Networks.TrafficType.Public) {
            final Integer networkRateKBps = nic.getNetworkRateMbps() != null && nic.getNetworkRateMbps().intValue() != -1
                    ? nic.getNetworkRateMbps().intValue() * 128 : 0;
            if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vlan && !vlanId.equalsIgnoreCase("untagged")) {
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    logger.debug("creating a vlan dev and bridge for public traffic per traffic label " + trafficLabel);
                    intf.defBridgeNet(pifs.get(trafficLabel), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                } else {
                    intf.defBridgeNet(pifs.get("public"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                            networkRateKBps);
                    intf.setVlanTag(Integer.parseInt(vlanId));
                }
            } else {
                intf.defBridgeNet(bridges.get("public"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter),
                        networkRateKBps);
            }
        } else if (nic.getType() == Networks.TrafficType.Management) {
            intf.defBridgeNet(bridges.get("private"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter));
        } else if (nic.getType() == Networks.TrafficType.Storage) {
            final String storageBrName = nic.getName() == null ? bridges.get("private") : nic.getName();
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
                    + " up; ip address add 169.254.0.1/16 dev " + privBrName, timeout);
        }
    }

    private void deleteExitingLinkLocalRouteTable(final String linkLocalBr) {
        final Script command = new Script("/bin/bash", timeout);
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
        final Script command = new Script("/bin/sh", timeout);
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
