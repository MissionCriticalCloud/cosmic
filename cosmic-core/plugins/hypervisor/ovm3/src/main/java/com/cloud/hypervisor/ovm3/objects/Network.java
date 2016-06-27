package com.cloud.hypervisor.ovm3.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Network extends OvmObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    private static final String START = "start";
    private static final String BRIDGE = "Bridge";
    private static final String ADDRESS = "Address";
    private static final String PHYSICAL = "Physical";
    private Map<String, Interface> interfaceList = null;
    private Object postDiscovery = null;
    private List<String> netInterfaces = new ArrayList<>();

    public Network(final Connection connection) {
        setClient(connection);
    }

    public Network.Interface getInterfaceByIp(final String ip)
            throws Ovm3ResourceException {
        return getNetIface(ADDRESS, ip);
    }

    private Network.Interface getNetIface(final String key, final String val)
            throws Ovm3ResourceException {
        final Map<String, Network.Interface> ifaces = getInterfaceList();
        for (final Entry<String, Interface> iface : ifaces.entrySet()) {
            String match = "default";
            if (ADDRESS.equals(key)) {
                match = iface.getValue().getAddress();
            }
            if ("Name".equals(key)) {
                match = iface.getKey();
            }
            if (match != null && match.equals(val)) {
                return iface.getValue();
            }
        }
        LOGGER.debug("Unable to find " + key + " Interface by value: " + val);
        setSuccess(false);
        return null;
    }

    public Map<String, Interface> getInterfaceList()
            throws Ovm3ResourceException {
        discoverNetwork();
        return interfaceList;
    }

    public Boolean discoverNetwork() throws Ovm3ResourceException {
        postDiscovery = callWrapper("discover_network");
        if (postDiscovery == null) {
            return false;
        }
        interfaceList = new HashMap<>();
        final Document xmlDocument = prepParse((String) postDiscovery);
        final String path = "//Discover_Network_Result/Network/Active";
        String bpath = path + "/Bridges/Device";

        netInterfaces = new ArrayList<>();
        netInterfaces.addAll(xmlToList(bpath + "/@Name", xmlDocument));
        for (final String b : netInterfaces) {
            final Map<String, String> br = xmlToMap(bpath + "[@Name='" + b
                    + "']/Family", xmlDocument);
      /* vifs are here too */
            final String phyInt = (String) this.xmlToMap(
                    bpath + "[@Name='" + b + "']/Interfaces", xmlDocument).get(
                    "PhyInterface");
            final Interface iface = new Interface();
            iface.setInterface(br);
            iface.setName(b);
            iface.setIfType(BRIDGE);
            if (phyInt == null) {
                iface.setIfType("Local");
            }
            iface.setPhysical(phyInt);
            interfaceList.put(b, iface);
        }
    /* add "physical" interfaces */
        bpath = path + "/Network/Device";
        netInterfaces = new ArrayList<>();
        netInterfaces.addAll(xmlToList(bpath + "/@Name", xmlDocument));
        for (final String p : netInterfaces) {
            final Map<String, String> nf = xmlToMap("//Device[@Name='" + p + "']",
                    xmlDocument);
            final Interface iface = new Interface();
            iface.setPhysical(nf.get("Basename"));
            iface.setName(p);
            iface.setMac(nf.get("MAC"));
            iface.setIfType(PHYSICAL);
            interfaceList.put(p, iface);
        }
    /* add virtual interfaces ? */
        return true;
    }

    /* check if it is a BRIDGE */
    public String getPhysicalByBridgeName(final String name)
            throws Ovm3ResourceException {
        return getInterfaceByName(name).getPhysical();
    }

    public Network.Interface getInterfaceByName(final String name)
            throws Ovm3ResourceException {
        return getNetIface("Name", name);
    }

    public Network.Interface getBridgeByName(final String name)
            throws Ovm3ResourceException {
        if (getNetIface("Name", name) != null
                && getNetIface("Name", name).getIfType().contentEquals(BRIDGE)) {
            return getNetIface("Name", name);
        }
        LOGGER.debug("Unable to find bridge by name: " + name);
        setSuccess(false);
        return null;
    }

    public Network.Interface getBridgeByIp(final String ip)
            throws Ovm3ResourceException {
        if (getNetIface(ADDRESS, ip) != null
                && getNetIface(ADDRESS, ip).getIfType().contentEquals(BRIDGE)) {
            return getNetIface(ADDRESS, ip);
        }
        LOGGER.debug("Unable to find bridge by ip: " + ip);
        setSuccess(false);
        return null;
    }

    /*
     * configure_virtual_ip, <class 'agent.api.network.linux_network.LinuxNetwork'> argument: self - default: None
     * argument: virtual_ip - default: None argument: base_ip - default: None
     */
    public Boolean configureVip(final String vip, final String baseip)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("configure_virtual_ip", vip, baseip);
    }

    public Boolean ovsIpConfig(final String net, final String optype, final String ip,
                               final String netmask) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("ovs_ip_config", net, optype, ip, netmask);
    }

  /*
   * Restriction: - data string that starts with leading spaces will be rejected ovs_if_meta('bond0',
   * 'ethernet:c0a80100{192.168.1.0}:MANAGEMENT,CLUSTER_HEARTBEAT,LIVE_MIGRATE,VIRTUAL_MACHINE,STORAGE')
   */

    public Boolean startOvsLocalConfig(final String br) throws Ovm3ResourceException {
        final String s = (String) ovsLocalConfig(START, br);
        if (s.startsWith(START)) {
            return true;
        }
        return false;
    }

    private Object ovsLocalConfig(final String action, final String br)
            throws Ovm3ResourceException {
        return callWrapper("ovs_local_config", action, br);
    }

    public Boolean stopOvsLocalConfig(final String br) throws Ovm3ResourceException {
        final String s = (String) ovsLocalConfig("stop", br);
        if (s.startsWith("stop")) {
            return true;
        }
        return false;
    }

    public Boolean startOvsVlanConfig(final String dev, final int vlan)
            throws Ovm3ResourceException {
        return ovsVlanConfig("add", dev, vlan);
    }

    private Boolean ovsVlanConfig(final String action, final String net, final int vlan)
            throws Ovm3ResourceException {
        final Object x = callWrapper("ovs_vlan_config", action, net, vlan);
        if (x == null) {
            return true;
        }
        return false;
    }

    public Boolean stopOvsVlanConfig(final String dev, final int vlan)
            throws Ovm3ResourceException {
        return ovsVlanConfig("remove", dev, vlan);
    }

    public Boolean startOvsBrConfig(final String br, final String dev)
            throws Ovm3ResourceException {
        final String s = (String) ovsBrConfig(START, br, dev);
        if (s.startsWith(START)) {
            return true;
        }
        return false;
    }

    public Object ovsBrConfig(final String action, final String br, final String net)
            throws Ovm3ResourceException {
        return callWrapper("ovs_br_config", action, br, net);
    }

    public Boolean stopOvsBrConfig(final String br, final String dev)
            throws Ovm3ResourceException {
        final String s = (String) ovsBrConfig("stop", br, dev);
        if (s.startsWith("stop")) {
            return true;
        }
        return false;
    }

    /* 1 is untagged, goes till 4095 */
    public Boolean stopOvsVlanBridge(final String br, final String net, final int vlan)
            throws Ovm3ResourceException {
        final String s = (String) ovsVlanBridge("stop", br, net, vlan);
        if (s.startsWith("stop")) {
            return true;
        }
        return false;
    }

    private Object ovsVlanBridge(final String action, final String br, final String net, final int vlan)
            throws Ovm3ResourceException {
        return callWrapper("ovs_vlan_bridge", action, br, net, vlan);
    }

    public Boolean startOvsVlanBridge(final String br, final String net, final int vlan)
            throws Ovm3ResourceException {
        final String s = (String) ovsVlanBridge(START, br, net, vlan);
    /* 3.2.1 uses start, 3.3.1 and up uses added... */
        if (s.startsWith(START) || s.startsWith("Added")) {
            return true;
        }
        return false;
    }

    public static class Interface {
        private final Map<String, String> deviceInterface = new HashMap<String, String>() {
            private static final long serialVersionUID = 6L;

            {
                put("Type", null);
                put(PHYSICAL, null);
                put("Name", null);
                put(ADDRESS, null);
                put("Broadcast", null);
                put("MAC", null);
                put("Vlan", null);
            }
        };

        public Interface() {
        }

        public String getIfType() {
            return deviceInterface.get("Type");
        }

        public void setIfType(final String type) {
            deviceInterface.put("Type", type);
        }

        public void setInterface(final Map<String, String> itf) {
            deviceInterface.putAll(itf);
        }

        public String getName() {
            return deviceInterface.get("Name");
        }

        public String getPhysical() {
            return deviceInterface.get(PHYSICAL);
        }

        public String getAddress() {
            return deviceInterface.get(ADDRESS);
        }

        public String getBroadcast() {
            return deviceInterface.get("Broadcast");
        }

        public String getMac() {
            return deviceInterface.get("MAC");
        }

        public String setName(final String name) {
            return deviceInterface.put("Name", name);
        }

        public String setPhysical(final String ph) {
            return deviceInterface.put(PHYSICAL, ph);
        }

        public String setMac(final String mac) {
            return deviceInterface.put("MAC", mac);
        }
    }

  /*
   * deconfigure_virtual_ip, <class 'agent.api.network.linux_network.LinuxNetwork'> argument: self - default: None
   * argument: virtual_ip - default: None
   */
}
