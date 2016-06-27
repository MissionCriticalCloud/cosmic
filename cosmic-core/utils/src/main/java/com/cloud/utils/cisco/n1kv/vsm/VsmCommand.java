//

//

package com.cloud.utils.cisco.n1kv.vsm;

import com.cloud.utils.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class VsmCommand {

    private static final Logger s_logger = LoggerFactory.getLogger(VsmCommand.class);
    private static final String s_namespace = "urn:ietf:params:xml:ns:netconf:base:1.0";
    private static final String s_ciscons = "http://www.cisco.com/nxos:1.0:ppm";
    private static final String s_configuremode = "__XML__MODE__exec_configure";
    private static final String s_portprofmode = "__XML__MODE_port-prof";
    private static final String s_policymapmode = "__XML__MODE_policy-map";
    private static final String s_classtypemode = "__XML__MODE_policy-map_class_type";
    private static final String s_paramvalue = "__XML__PARAM_value";

    public static String getAddPortProfile(final String name, final PortProfileType type, final BindingType binding, final SwitchPortMode mode, final int vlanid, final String
            vdc, final String espName) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(configPortProfileDetails(doc, name, type, binding, mode, vlanid, vdc, espName));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating add port profile message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating add port profile message : " + e.getMessage());
            return null;
        }
    }

    private static Document createDocument(final DOMImplementation dom) {
        final Document doc = dom.createDocument(s_namespace, "nf:rpc", null);
        doc.getDocumentElement().setAttribute("message-id", "101");
        doc.getDocumentElement().setAttributeNS(s_ciscons, "portprofile", "true");
        return doc;
    }

    private static Element configPortProfileDetails(final Document doc, final String name, final PortProfileType type, final BindingType binding, final SwitchPortMode mode,
                                                    final int vlanid, final String vdc,
                                                    final String espName) {

        // In mode, exec_configure.
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Port profile name and type configuration.
        final Element portProfile = doc.createElement("port-profile");
        modeConfigure.appendChild(portProfile);

        // Port profile type.
        final Element portDetails = doc.createElement("name");
        switch (type) {
            case none:
                portProfile.appendChild(portDetails);
                break;
            case ethernet: {
                final Element typetag = doc.createElement("type");
                final Element ethernettype = doc.createElement("ethernet");
                portProfile.appendChild(typetag);
                typetag.appendChild(ethernettype);
                ethernettype.appendChild(portDetails);
            }
            break;
            case vethernet: {
                final Element typetag = doc.createElement("type");
                final Element ethernettype = doc.createElement("vethernet");
                portProfile.appendChild(typetag);
                typetag.appendChild(ethernettype);
                ethernettype.appendChild(portDetails);
            }
            break;
        }

        // Port profile name.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(name);
        portDetails.appendChild(value);

        // element for port prof mode.
        final Element portProf = doc.createElement(s_portprofmode);
        portDetails.appendChild(portProf);

        // Binding type.
        if (binding != BindingType.none) {
            portProf.appendChild(getBindingType(doc, binding));
        }

        if (mode != SwitchPortMode.none) {
            // Switchport mode.
            portProf.appendChild(getSwitchPortMode(doc, mode));
            // Adding vlan details.
            if (vlanid > 0) {
                portProf.appendChild(getAddVlanDetails(doc, mode, Integer.toString(vlanid)));
            }
        }

        // org %vdc%
        // vservice node <Node Name> profile <Edge Security Profile Name in VNMC>
        final Element vdcValue = doc.createElement(s_paramvalue);
        vdcValue.setAttribute("isKey", "true");
        vdcValue.setTextContent(vdc);

        final Element org = doc.createElement("org");
        org.appendChild(doc.createElement("orgname")).appendChild(vdcValue);
        portProf.appendChild(org);

        final String asaNodeName = "ASA_" + vlanid;
        final Element vservice = doc.createElement("vservice");
        vservice.appendChild(doc.createElement("node"))
                .appendChild(doc.createElement(asaNodeName))
                .appendChild(doc.createElement("profile"))
                .appendChild(doc.createElement(espName));
        portProf.appendChild(vservice);

        // no shutdown.
        final Element no = doc.createElement("no");
        final Element shutdown = doc.createElement("shutdown");
        no.appendChild(shutdown);
        portProf.appendChild(no);

        // Enable the port profile.
        final Element state = doc.createElement("state");
        final Element enabled = doc.createElement("enabled");
        state.appendChild(enabled);
        portProf.appendChild(state);

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    private static String serialize(final DOMImplementation domImpl, final Document document) {
        final DOMImplementationLS ls = (DOMImplementationLS) domImpl;
        final LSSerializer lss = ls.createLSSerializer();
        return lss.writeToString(document);
    }

    private static Element getBindingType(final Document doc, final BindingType binding) {
        final Element portBinding = doc.createElement("port-binding");

        // We only have handling for access or trunk mode. Handling for private-vlan
        // host/promiscuous command will have to be added.
        if (binding == BindingType.portbindingstatic) {
            final Element type = doc.createElement("static");
            portBinding.appendChild(type);
        } else if (binding == BindingType.portbindingdynamic) {
            final Element type = doc.createElement("dynamic");
            portBinding.appendChild(type);
        } else if (binding == BindingType.portbindingephermal) {
            final Element type = doc.createElement("ephemeral");
            portBinding.appendChild(type);
        }

        return portBinding;
    }

    private static Element getSwitchPortMode(final Document doc, final SwitchPortMode mode) {
        final Element switchport = doc.createElement("switchport");
        final Element accessmode = doc.createElement("mode");
        switchport.appendChild(accessmode);

        // We only have handling for access or trunk mode. Handling for private-vlan
        // host/promiscuous command will have to be added.
        if (mode == SwitchPortMode.access) {
            final Element access = doc.createElement("access");
            accessmode.appendChild(access);
        } else if (mode == SwitchPortMode.trunk) {
            final Element trunk = doc.createElement("trunk");
            accessmode.appendChild(trunk);
        }

        return switchport;
    }

    private static Element getAddVlanDetails(final Document doc, final SwitchPortMode mode, final String vlanid) {
        final Element switchport = doc.createElement("switchport");

        // Details of the vlanid to add.
        final Element vlancreate = doc.createElement("vlan-id-create-delete");
        final Element value = doc.createElement(s_paramvalue);
        value.setTextContent(vlanid);
        vlancreate.appendChild(value);

        // Handling is there only for 'access' and 'trunk allowed' mode command.
        if (mode == SwitchPortMode.access) {
            final Element access = doc.createElement("access");
            switchport.appendChild(access);

            final Element vlan = doc.createElement("vlan");
            access.appendChild(vlan);

            vlan.appendChild(vlancreate);
        } else if (mode == SwitchPortMode.trunk) {
            final Element trunk = doc.createElement("trunk");
            switchport.appendChild(trunk);

            final Element allowed = doc.createElement("allowed");
            trunk.appendChild(allowed);

            final Element vlan = doc.createElement("vlan");
            allowed.appendChild(vlan);

            final Element add = doc.createElement("add");
            vlan.appendChild(add);

            add.appendChild(vlancreate);
        }

        return switchport;
    }

    private static Element persistConfiguration(final Document doc) {
        final Element copy = doc.createElement("copy");
        final Element running = doc.createElement("running-config");
        final Element startup = doc.createElement("startup-config");
        copy.appendChild(running);
        running.appendChild(startup);
        return copy;
    }

    public static String getAddPortProfile(final String name, final PortProfileType type, final BindingType binding, final SwitchPortMode mode, final int vlanid) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(configPortProfileDetails(doc, name, type, binding, mode, vlanid));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating add port profile message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating add port profile message : " + e.getMessage());
            return null;
        }
    }

    private static Element configPortProfileDetails(final Document doc, final String name, final PortProfileType type, final BindingType binding, final SwitchPortMode mode,
                                                    final int vlanid) {

        // In mode, exec_configure.
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Port profile name and type configuration.
        final Element portProfile = doc.createElement("port-profile");
        modeConfigure.appendChild(portProfile);

        // Port profile type.
        final Element portDetails = doc.createElement("name");
        switch (type) {
            case none:
                portProfile.appendChild(portDetails);
                break;
            case ethernet: {
                final Element typetag = doc.createElement("type");
                final Element ethernettype = doc.createElement("ethernet");
                portProfile.appendChild(typetag);
                typetag.appendChild(ethernettype);
                ethernettype.appendChild(portDetails);
            }
            break;
            case vethernet: {
                final Element typetag = doc.createElement("type");
                final Element ethernettype = doc.createElement("vethernet");
                portProfile.appendChild(typetag);
                typetag.appendChild(ethernettype);
                ethernettype.appendChild(portDetails);
            }
            break;
        }

        // Port profile name.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(name);
        portDetails.appendChild(value);

        // element for port prof mode.
        final Element portProf = doc.createElement(s_portprofmode);
        portDetails.appendChild(portProf);

        // Binding type.
        if (binding != BindingType.none) {
            portProf.appendChild(getBindingType(doc, binding));
        }

        if (mode != SwitchPortMode.none) {
            // Switchport mode.
            portProf.appendChild(getSwitchPortMode(doc, mode));
            // Adding vlan details.
            if (vlanid > 0) {
                portProf.appendChild(getAddVlanDetails(doc, mode, Integer.toString(vlanid)));
            }
        }

        // no shutdown.
        final Element no = doc.createElement("no");
        final Element shutdown = doc.createElement("shutdown");
        no.appendChild(shutdown);
        portProf.appendChild(no);

        // Enable the port profile.
        final Element state = doc.createElement("state");
        final Element enabled = doc.createElement("enabled");
        state.appendChild(enabled);
        portProf.appendChild(state);

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    public static String getUpdatePortProfile(final String name, final SwitchPortMode mode, final List<Pair<VsmCommand.OperationType, String>> params) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to update the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(configPortProfileDetails(doc, name, mode, params));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating update port profile message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating update port profile message : " + e.getMessage());
            return null;
        }
    }

    private static Element configPortProfileDetails(final Document doc, final String name, final SwitchPortMode mode, final List<Pair<VsmCommand.OperationType, String>> params) {

        // In mode, exec_configure.
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Port profile name and type configuration.
        final Element portProfile = doc.createElement("port-profile");
        modeConfigure.appendChild(portProfile);

        // Port profile type.
        final Element portDetails = doc.createElement("name");
        portProfile.appendChild(portDetails);

        // Name of the profile to update.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(name);
        portDetails.appendChild(value);

        // element for port prof mode.
        final Element portProfMode = doc.createElement(s_portprofmode);
        portDetails.appendChild(portProfMode);

        for (final Pair<VsmCommand.OperationType, String> item : params) {
            if (item.first() == OperationType.addvlanid) {
                // Set the access mode configuration or the list
                // of allowed vlans on the trunking interface.
                portProfMode.appendChild(getAddVlanDetails(doc, mode, item.second()));
            } else if (item.first() == OperationType.removevlanid) {
                portProfMode.appendChild(getDeleteVlanDetails(doc, mode, item.second()));
            }
        }

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    private static Node getDeleteVlanDetails(final Document doc, final SwitchPortMode mode, final String vlanid) {
        Node parentNode = null;
        final Element switchport = doc.createElement("switchport");

        // Handling is there only for 'access' and 'trunk allowed' mode command.
        if (mode == SwitchPortMode.access) {
            final Element no = doc.createElement("no");
            no.appendChild(switchport);
            parentNode = no;

            final Element access = doc.createElement("access");
            switchport.appendChild(access);

            final Element vlan = doc.createElement("vlan");
            access.appendChild(vlan);
        } else if (mode == SwitchPortMode.trunk) {
            parentNode = switchport;

            final Element trunk = doc.createElement("trunk");
            switchport.appendChild(trunk);

            final Element allowed = doc.createElement("allowed");
            trunk.appendChild(allowed);

            final Element vlan = doc.createElement("vlan");
            allowed.appendChild(vlan);

            final Element remove = doc.createElement("remove");
            vlan.appendChild(remove);

            // Details of the vlanid to add.
            final Element vlancreate = doc.createElement("vlan-id-create-delete");
            final Element value = doc.createElement(s_paramvalue);
            value.setTextContent(vlanid);
            vlancreate.appendChild(value);

            remove.appendChild(vlancreate);
        }

        return parentNode;
    }

    public static String getDeletePortProfile(final String portName) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(deletePortProfileDetails(doc, portName));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating delete port profile message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating delete port profile message : " + e.getMessage());
            return null;
        }
    }

    private static Element deletePortProfileDetails(final Document doc, final String name) {
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Command and name for the port profile to be deleted.
        final Element deletePortProfile = doc.createElement("no");
        modeConfigure.appendChild(deletePortProfile);

        final Element portProfile = doc.createElement("port-profile");
        deletePortProfile.appendChild(portProfile);

        final Element portDetails = doc.createElement("name");
        portProfile.appendChild(portDetails);

        // Name of the profile to delete.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(name);
        portDetails.appendChild(value);

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    public static String getAddPolicyMap(final String name, final int averageRate, final int maxRate, final int burstRate) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(policyMapDetails(doc, name, averageRate, maxRate, burstRate));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating policy map message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating policy map message : " + e.getMessage());
            return null;
        }
    }

    private static Element policyMapDetails(final Document doc, final String name, final int averageRate, final int maxRate, final int burstRate) {
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Policy map details
        final Element policyMap = doc.createElement("policy-map");
        modeConfigure.appendChild(policyMap);

        final Element policyDetails = doc.createElement("name");
        policyMap.appendChild(policyDetails);

        // Name of the policy to create/update.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(name);
        policyDetails.appendChild(value);

        final Element policyMapMode = doc.createElement(s_policymapmode);
        policyDetails.appendChild(policyMapMode);

        // Create the default class to match all traffic.
        final Element classRoot = doc.createElement("class");
        final Element classDefault = doc.createElement("class-default");
        policyMapMode.appendChild(classRoot);
        classRoot.appendChild(classDefault);

        final Element classMode = doc.createElement(s_classtypemode);
        classDefault.appendChild(classMode);

        // Set the average, max and burst rate.
        // TODO: Add handling for max and burst.
        final Element police = doc.createElement("police");
        classMode.appendChild(police);

        // Set the committed information rate and its value in mbps.
        final Element cir = doc.createElement("cir");
        police.appendChild(cir);
        final Element cirValue = doc.createElement("cir-val");
        cir.appendChild(cirValue);
        final Element value2 = doc.createElement(s_paramvalue);
        final Element mbps = doc.createElement("mbps");
        value2.setTextContent(Integer.toString(averageRate));
        cirValue.appendChild(value2);
        cirValue.appendChild(mbps);

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    public static String getDeletePolicyMap(final String name) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(deletePolicyMapDetails(doc, name));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating delete policy map message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating delete policy map message : " + e.getMessage());
            return null;
        }
    }

    private static Element deletePolicyMapDetails(final Document doc, final String name) {
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Delete Policy map details
        final Element deletePolicyMap = doc.createElement("no");
        final Element policyMap = doc.createElement("policy-map");
        deletePolicyMap.appendChild(policyMap);
        modeConfigure.appendChild(deletePolicyMap);

        final Element policyDetails = doc.createElement("name");
        policyMap.appendChild(policyDetails);

        // Name of the policy to create/update.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(name);
        policyDetails.appendChild(value);

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    public static String getServicePolicy(final String policyMap, final String portProfile, final boolean attach) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(serviceDetails(doc, policyMap, portProfile, attach));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating attach/detach service policy message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating attach/detach service policy message : " + e.getMessage());
            return null;
        }
    }

    private static Element serviceDetails(final Document doc, final String policyMap, final String portProfile, final boolean attach) {
        // In mode, exec_configure.
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // Port profile name and type configuration.
        final Element profile = doc.createElement("port-profile");
        modeConfigure.appendChild(profile);

        // Port profile type.
        final Element portDetails = doc.createElement("name");
        profile.appendChild(portDetails);

        // Name of the profile to update.
        final Element value = doc.createElement(s_paramvalue);
        value.setAttribute("isKey", "true");
        value.setTextContent(portProfile);
        portDetails.appendChild(value);

        // element for port prof mode.
        final Element portProfMode = doc.createElement(s_portprofmode);
        portDetails.appendChild(portProfMode);

        // Associate/Remove the policy for input.
        if (attach) {
            portProfMode.appendChild(getServicePolicyCmd(doc, policyMap, "input"));
        } else {
            final Element detach = doc.createElement("no");
            portProfMode.appendChild(detach);
            detach.appendChild(getServicePolicyCmd(doc, policyMap, "input"));
        }

        // Associate/Remove the policy for output.
        if (attach) {
            portProfMode.appendChild(getServicePolicyCmd(doc, policyMap, "output"));
        } else {
            final Element detach = doc.createElement("no");
            portProfMode.appendChild(detach);
            detach.appendChild(getServicePolicyCmd(doc, policyMap, "output"));
        }

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    private static Element getServicePolicyCmd(final Document doc, final String policyMap, final String type) {
        final Element service = doc.createElement("service-policy");
        final Element input = doc.createElement(type);
        service.appendChild(input);

        final Element name = doc.createElement("name");
        input.appendChild(name);

        final Element policyValue = doc.createElement(s_paramvalue);
        policyValue.setTextContent(policyMap);
        name.appendChild(policyValue);

        return service;
    }

    public static String getPortProfile(final String name) {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            final Element get = doc.createElement("nf:get");
            doc.getDocumentElement().appendChild(get);

            final Element filter = doc.createElement("nf:filter");
            filter.setAttribute("type", "subtree");
            get.appendChild(filter);

            // Create the show port-profile name <profile-name> command.
            final Element show = doc.createElement("show");
            filter.appendChild(show);
            final Element portProfile = doc.createElement("port-profile");
            show.appendChild(portProfile);
            final Element nameNode = doc.createElement("name");
            portProfile.appendChild(nameNode);

            // Profile name
            final Element profileName = doc.createElement("profile_name");
            profileName.setTextContent(name);
            nameNode.appendChild(profileName);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating the message to get port profile details: " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating the message to get port profile details: " + e.getMessage());
            return null;
        }
    }

    public static String getPolicyMap(final String name) {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            final Element get = doc.createElement("nf:get");
            doc.getDocumentElement().appendChild(get);

            final Element filter = doc.createElement("nf:filter");
            filter.setAttribute("type", "subtree");
            get.appendChild(filter);

            // Create the show port-profile name <profile-name> command.
            final Element show = doc.createElement("show");
            filter.appendChild(show);
            final Element policyMap = doc.createElement("policy-map");
            show.appendChild(policyMap);
            final Element nameNode = doc.createElement("name");
            nameNode.setTextContent(name);
            policyMap.appendChild(nameNode);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating the message to get policy map details : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating the message to get policy map details : " + e.getMessage());
            return null;
        }
    }

    public static String getHello() {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();

            // Root elements.
            final Document doc = domImpl.createDocument(s_namespace, "nc:hello", null);

            // Client capacity. We are only supporting basic capacity.
            final Element capabilities = doc.createElement("nc:capabilities");
            final Element capability = doc.createElement("nc:capability");
            capability.setTextContent("urn:ietf:params:xml:ns:netconf:base:1.0");

            capabilities.appendChild(capability);
            doc.getDocumentElement().appendChild(capabilities);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while creating hello message : " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while creating hello message : " + e.getMessage());
            return null;
        }
    }

    public static String getVServiceNode(final String vlanId, final String ipAddr) {
        try {
            // Create the document and root element.
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final DOMImplementation domImpl = docBuilder.getDOMImplementation();
            final Document doc = createDocument(domImpl);

            // Edit configuration command.
            final Element editConfig = doc.createElement("nf:edit-config");
            doc.getDocumentElement().appendChild(editConfig);

            // Command to get into exec configure mode.
            final Element target = doc.createElement("nf:target");
            final Element running = doc.createElement("nf:running");
            target.appendChild(running);
            editConfig.appendChild(target);

            // Command to create the port profile with the desired configuration.
            final Element config = doc.createElement("nf:config");
            config.appendChild(configVServiceNodeDetails(doc, vlanId, ipAddr));
            editConfig.appendChild(config);

            return serialize(domImpl, doc);
        } catch (final ParserConfigurationException e) {
            s_logger.error("Error while adding vservice node for vlan " + vlanId + ", " + e.getMessage());
            return null;
        } catch (final DOMException e) {
            s_logger.error("Error while adding vservice node for vlan " + vlanId + ", " + e.getMessage());
            return null;
        }
    }

    private static Element configVServiceNodeDetails(final Document doc, final String vlanId, final String ipAddr) {
        // In mode, exec_configure.
        final Element configure = doc.createElementNS(s_ciscons, "nxos:configure");
        final Element modeConfigure = doc.createElement("nxos:" + s_configuremode);
        configure.appendChild(modeConfigure);

        // vservice node %name% type asa
        final Element vservice = doc.createElement("vservice");
        vservice.appendChild(doc.createElement("node"))
                .appendChild(doc.createElement("ASA_" + vlanId))
                .appendChild(doc.createElement("type"))
                .appendChild(doc.createElement("asa"));
        modeConfigure.appendChild(vservice);

        final Element address = doc.createElement(s_paramvalue);
        address.setAttribute("isKey", "true");
        address.setTextContent(ipAddr);

        // ip address %ipAddr%
        modeConfigure.appendChild(doc.createElement("ip")).appendChild(doc.createElement("address")).appendChild(doc.createElement("value")).appendChild(address);

        final Element vlan = doc.createElement(s_paramvalue);
        vlan.setAttribute("isKey", "true");
        vlan.setTextContent(vlanId);

        // adjacency l2 vlan %vlanId%
        modeConfigure.appendChild(doc.createElement("adjacency"))
                     .appendChild(doc.createElement("l2"))
                     .appendChild(doc.createElement("vlan"))
                     .appendChild(doc.createElement("value"))
                     .appendChild(vlan);

        // fail-mode close
        modeConfigure.appendChild(doc.createElement("fail-mode")).appendChild(doc.createElement("close"));

        // Persist the configuration across reboots.
        modeConfigure.appendChild(persistConfiguration(doc));

        return configure;
    }

    public enum PortProfileType {
        none, vethernet, ethernet
    }

    public enum BindingType {
        none, portbindingstatic, portbindingdynamic, portbindingephermal
    }

    public enum SwitchPortMode {
        none, access, trunk, privatevlanhost, privatevlanpromiscuous
    }

    public enum OperationType {
        addvlanid, removevlanid
    }
}
