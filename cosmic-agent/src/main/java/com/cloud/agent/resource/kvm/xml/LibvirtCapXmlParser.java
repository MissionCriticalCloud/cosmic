package com.cloud.agent.resource.kvm.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LibvirtCapXmlParser extends LibvirtXmlParser {

    private final Logger logger = LoggerFactory.getLogger(LibvirtCapXmlParser.class);
    private final StringBuffer emulator = new StringBuffer();
    private final StringBuffer capXml = new StringBuffer();
    private final ArrayList<String> guestOsTypes = new ArrayList<>();
    private boolean host;
    private boolean guest;
    private boolean osType;
    private boolean domainTypeKvm;
    private boolean emulatorFlag;
    private boolean archTypex8664;

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (this.host) {
            this.capXml.append(ch, start, length);
        } else if (this.osType) {
            this.guestOsTypes.add(new String(ch, start, length));
        } else if (this.emulatorFlag) {
            this.logger.debug("Found " + new String(ch, start, length) + " as a suiteable emulator");
            this.emulator.append(ch, start, length);
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
        if (name.equalsIgnoreCase("host")) {
            this.host = true;
        } else if (name.equalsIgnoreCase("guest")) {
            this.guest = true;
        } else if (name.equalsIgnoreCase("os_type")) {
            if (this.guest) {
                this.osType = true;
            }
        } else if (name.equalsIgnoreCase("arch")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equalsIgnoreCase("name") && attributes.getValue(i).equalsIgnoreCase("x86_64")) {
                    this.archTypex8664 = true;
                }
            }
        } else if (name.equalsIgnoreCase("domain")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equalsIgnoreCase("type") && attributes.getValue(i).equalsIgnoreCase("kvm")) {
                    this.domainTypeKvm = true;
                }
            }
        } else if (name.equalsIgnoreCase("emulator") && this.domainTypeKvm && this.archTypex8664) {
            this.emulatorFlag = true;
            this.emulator.delete(0, this.emulator.length());
        } else if (this.host) {
            this.capXml.append("<").append(name);
            for (int i = 0; i < attributes.getLength(); i++) {
                this.capXml.append(" ").append(attributes.getQName(i)).append("=").append(attributes.getValue(i));
            }
            this.capXml.append(">");
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {
        if (name.equalsIgnoreCase("host")) {
            this.host = false;
        } else if (name.equalsIgnoreCase("os_type")) {
            this.osType = false;
        } else if (name.equalsIgnoreCase("guest")) {
            this.guest = false;
        } else if (name.equalsIgnoreCase("domain")) {
            this.domainTypeKvm = false;
        } else if (name.equalsIgnoreCase("emulator")) {
            this.emulatorFlag = false;
        } else if (name.equalsIgnoreCase("arch")) {
            this.archTypex8664 = false;
        } else if (this.host) {
            this.capXml.append("<").append("/").append(name).append(">");
        }
    }

    public String parseCapabilitiesXml(final String capXml) {
        if (!this.isInitialised) {
            return null;
        }
        try {
            this.saxParser.parse(new InputSource(new StringReader(capXml)), this);
            return capXml.toString();
        } catch (final SAXException se) {
            this.logger.warn(se.getMessage());
        } catch (final IOException ie) {
            this.logger.error(ie.getMessage());
        }
        return null;
    }

    public ArrayList<String> getGuestOsType() {
        return this.guestOsTypes;
    }

    public String getEmulator() {
        return this.emulator.toString();
    }
}
