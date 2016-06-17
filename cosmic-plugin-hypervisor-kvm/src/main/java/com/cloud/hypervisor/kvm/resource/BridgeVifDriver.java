package com.cloud.hypervisor.kvm.resource;

import com.cloud.agent.api.to.NicTO;
import com.cloud.exception.InternalErrorException;
import com.cloud.network.Networks;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;
import com.google.common.base.Strings;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BridgeVifDriver extends VifDriverBase {

    private final Logger logger = LoggerFactory.getLogger(BridgeVifDriver.class);

    private int timeout;

    private final Object vnetBridgeMonitor = new Object();
    private String modifyVlanPath;
    private String modifyVxlanPath;
    private String bridgeNameSchema;
    private Long libvirtVersion;

    @Override
    public void configure(final Map<String, Object> params) throws ConfigurationException {

        super.configure(params);

        // Set the domr scripts directory
        params.put("domr.scripts.dir", "scripts/network/domr/kvm");

        String networkScriptsDir = (String) params.get("network.scripts.dir");
        if (networkScriptsDir == null) {
            networkScriptsDir = "scripts/vm/network/vnet";
        }

        bridgeNameSchema = (String) params.get("network.bridge.name.schema");

        final String value = (String) params.get("scripts.timeout");
        timeout = NumbersUtil.parseInt(value, 30 * 60) * 1000;

        modifyVlanPath = Script.findScript(networkScriptsDir, "modifyvlan.sh");
        if (modifyVlanPath == null) {
            throw new ConfigurationException("Unable to find modifyvlan.sh");
        }
        modifyVxlanPath = Script.findScript(networkScriptsDir, "modifyvxlan.sh");
        if (modifyVxlanPath == null) {
            throw new ConfigurationException("Unable to find modifyvxlan.sh");
        }

        libvirtVersion = (Long) params.get("libvirtVersion");
        if (libvirtVersion == null) {
            libvirtVersion = 0L;
        }

        try {
            createControlNetwork();
        } catch (final LibvirtException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    @Override
    public LibvirtVmDef.InterfaceDef plug(final NicTO nic, final String guestOsType, final String nicAdapter)
            throws InternalErrorException, LibvirtException {

        if (logger.isDebugEnabled()) {
            logger.debug("nic=" + nic);
            if (nicAdapter != null && !nicAdapter.isEmpty()) {
                logger.debug("custom nic adapter=" + nicAdapter);
            }
        }

        final LibvirtVmDef.InterfaceDef intf = new LibvirtVmDef.InterfaceDef();

        String netId = null;
        String protocol = null;
        if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vlan
                || nic.getBroadcastType() == Networks.BroadcastDomainType.Vxlan) {
            netId = Networks.BroadcastDomainType.getValue(nic.getBroadcastUri());
            protocol = Networks.BroadcastDomainType.getSchemeValue(nic.getBroadcastUri()).scheme();
        } else if (nic.getBroadcastType() == Networks.BroadcastDomainType.Lswitch) {
            throw new InternalErrorException("Nicira NVP Logicalswitches are not supported by the BridgeVifDriver");
        }
        final String trafficLabel = nic.getName();
        Integer networkRateKBps = 0;
        if (libvirtVersion > 10 * 1000 + 10) {
            networkRateKBps = nic.getNetworkRateMbps() != null && nic.getNetworkRateMbps().intValue() != -1
                    ? nic.getNetworkRateMbps().intValue() * 128 : 0;
        }

        if (nic.getType() == Networks.TrafficType.Guest) {
            if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vlan && netId != null && protocol != null
                    && !netId.equalsIgnoreCase("untagged")
                    || nic.getBroadcastType() == Networks.BroadcastDomainType.Vxlan) {
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    logger.debug("creating a vNet dev and bridge for guest traffic per traffic label " + trafficLabel);
                    final String brName = createVnetBr(netId, trafficLabel, protocol);
                    intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
                } else {
                    final String brName = createVnetBr(netId, "private", protocol);
                    intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
                }
            } else {
                String brname = "";
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    brname = trafficLabel;
                } else {
                    brname = bridges.get("guest");
                }
                intf.defBridgeNet(brname, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
            }
        } else if (nic.getType() == Networks.TrafficType.Control) {
      /* Make sure the network is still there */
            createControlNetwork();
            intf.defBridgeNet(bridges.get("linklocal"), null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter));
        } else if (nic.getType() == Networks.TrafficType.Public) {
            if (nic.getBroadcastType() == Networks.BroadcastDomainType.Vlan && netId != null && protocol != null
                    && !netId.equalsIgnoreCase("untagged")
                    || nic.getBroadcastType() == Networks.BroadcastDomainType.Vxlan) {
                if (trafficLabel != null && !trafficLabel.isEmpty()) {
                    logger.debug("creating a vNet dev and bridge for public traffic per traffic label " + trafficLabel);
                    final String brName = createVnetBr(netId, trafficLabel, protocol);
                    intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
                } else {
                    final String brName = createVnetBr(netId, "public", protocol);
                    intf.defBridgeNet(brName, null, nic.getMac(), getGuestNicModel(guestOsType, nicAdapter), networkRateKBps);
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
    public void unplug(final LibvirtVmDef.InterfaceDef iface) {
        deleteVnetBr(iface.getBrName());
    }

    private String setVnetBrName(final String pifName, final String vnetId) {
        return "br" + pifName + "-" + vnetId;
    }

    private String setVxnetBrName(final String pifName, final String vnetId) {
        return "brvx-" + vnetId;
    }

    private String createVnetBr(final String netId, final String pifKey, final String protocol) throws InternalErrorException {
        String nic = pifs.get(pifKey);
        if (nic == null) {
            // if not found in bridge map, maybe traffic label refers to pif already?
            final File pif = new File("/sys/class/net/" + pifKey);
            if (pif.isDirectory()) {
                nic = pifKey;
            }
        }
        String brName = "";
        if (protocol.equals(Networks.BroadcastDomainType.Vxlan.scheme())) {
            brName = setVxnetBrName(nic, netId);
        } else {
            brName = setVnetBrName(nic, netId);
        }
        createVnet(netId, nic, brName, protocol);
        return brName;
    }

    private void createVnet(final String vnetId, final String pif, final String brName, final String protocol) throws InternalErrorException {
        synchronized (vnetBridgeMonitor) {
            String script = modifyVlanPath;
            if (protocol.equals(Networks.BroadcastDomainType.Vxlan.scheme())) {
                script = modifyVxlanPath;
            }
            final Script command = new Script(script, timeout, logger);
            command.add("-v", vnetId);
            command.add("-p", pif);
            command.add("-b", brName);
            command.add("-o", "add");

            final String result = command.execute();
            if (result != null) {
                throw new InternalErrorException("Failed to create vnet " + vnetId + ": " + result);
            }
        }
    }

    private void deleteVnetBr(final String brName) {
        synchronized (vnetBridgeMonitor) {
            String cmdout = Script.runSimpleBashScript("ls /sys/class/net/" + brName);
            if (cmdout == null) {
                // Bridge does not exist
                return;
            }
            cmdout = Script.runSimpleBashScript("ls /sys/class/net/" + brName + "/brif | tr '\n' ' '");
            if (cmdout != null && cmdout.contains("vnet")) {
                // Active VM remains on that bridge
                return;
            }

            final Pattern oldStyleBrNameRegex = Pattern.compile("^cloudVirBr(\\d+)$");
            final Pattern brNameRegex = Pattern.compile("^br(\\S+)-(\\d+)$");
            final Matcher oldStyleBrNameMatcher = oldStyleBrNameRegex.matcher(brName);
            final Matcher brNameMatcher = brNameRegex.matcher(brName);

            String name = null;
            String netId = null;
            if (oldStyleBrNameMatcher.find()) {
                // Actually modifyvlan.sh doesn't require pif name when deleting its bridge so far.
                name = "undefined";
                netId = oldStyleBrNameMatcher.group(1);
            } else if (brNameMatcher.find()) {
                if (brNameMatcher.group(1) != null || !brNameMatcher.group(1).isEmpty()) {
                    name = brNameMatcher.group(1);
                } else {
                    name = "undefined";
                }
                netId = brNameMatcher.group(2);
            }

            if (netId == null || netId.isEmpty()) {
                logger.debug("unable to get a vNet ID from name " + brName);
                return;
            }

            String scriptPath = null;
            if (cmdout != null && cmdout.contains("vxlan")) {
                scriptPath = modifyVxlanPath;
            } else {
                scriptPath = modifyVlanPath;
            }

            final Script command = new Script(scriptPath, timeout, logger);
            command.add("-o", "delete");
            command.add("-v", netId);
            command.add("-p", name);
            command.add("-b", brName);

            final String result = command.execute();
            if (result != null) {
                logger.debug("Delete bridge " + brName + " failed: " + result);
            }
        }
    }

    private void createControlNetwork() throws LibvirtException {
        createControlNetwork(bridges.get("linklocal"));
    }

    private void createControlNetwork(final String privBrName) {
        deleteExistingLinkLocalRouteTable(privBrName);
        if (!isBridgeExists(privBrName)) {
            Script.runSimpleBashScript("brctl addbr " + privBrName + "; ip link set " + privBrName
                    + " up; ip address add 169.254.0.1/16 dev " + privBrName, timeout);
        }

    }

    private void deleteExistingLinkLocalRouteTable(final String linkLocalBr) {
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
                if (tokens != null && tokens.length < 2) {
                    continue;
                }
                final String device = tokens[2];
                if (!Strings.isNullOrEmpty(device) && !device.equalsIgnoreCase(linkLocalBr)) {
                    Script.runSimpleBashScript("ip route del " + NetUtils.getLinkLocalCIDR() + " dev " + tokens[2]);
                } else {
                    foundLinkLocalBr = true;
                }
            }
        }
        if (!foundLinkLocalBr) {
            Script.runSimpleBashScript("ip address add 169.254.0.1/16 dev " + linkLocalBr + ";" + "ip route add "
                    + NetUtils.getLinkLocalCIDR() + " dev " + linkLocalBr + " src "
                    + NetUtils.getLinkLocalGateway());
        }
    }

    private boolean isBridgeExists(final String bridgeName) {
        final File f = new File("/sys/devices/virtual/net/" + bridgeName);
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
