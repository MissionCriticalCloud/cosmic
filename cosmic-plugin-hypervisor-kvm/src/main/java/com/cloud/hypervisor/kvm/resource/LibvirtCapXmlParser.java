package com.cloud.hypervisor.kvm.resource;

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

  private boolean host;
  private boolean guest;
  private boolean osType;
  private boolean domainTypeKvm;
  private boolean emulatorFlag;
  private boolean archTypex8664;

  private final StringBuffer emulator = new StringBuffer();
  private final StringBuffer capXml = new StringBuffer();
  private final ArrayList<String> guestOsTypes = new ArrayList<String>();

  @Override
  public void endElement(String uri, String localName, String name) throws SAXException {
    if (name.equalsIgnoreCase("host")) {
      host = false;
    } else if (name.equalsIgnoreCase("os_type")) {
      osType = false;
    } else if (name.equalsIgnoreCase("guest")) {
      guest = false;
    } else if (name.equalsIgnoreCase("domain")) {
      domainTypeKvm = false;
    } else if (name.equalsIgnoreCase("emulator")) {
      emulatorFlag = false;
    } else if (name.equalsIgnoreCase("arch")) {
      archTypex8664 = false;
    } else if (host) {
      capXml.append("<").append("/").append(name).append(">");
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (host) {
      capXml.append(ch, start, length);
    } else if (osType) {
      guestOsTypes.add(new String(ch, start, length));
    } else if (emulatorFlag) {
      logger.debug("Found " + new String(ch, start, length) + " as a suiteable emulator");
      emulator.append(ch, start, length);
    }
  }

  @Override
  public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    if (name.equalsIgnoreCase("host")) {
      host = true;
    } else if (name.equalsIgnoreCase("guest")) {
      guest = true;
    } else if (name.equalsIgnoreCase("os_type")) {
      if (guest) {
        osType = true;
      }
    } else if (name.equalsIgnoreCase("arch")) {
      for (int i = 0; i < attributes.getLength(); i++) {
        if (attributes.getQName(i).equalsIgnoreCase("name") && attributes.getValue(i).equalsIgnoreCase("x86_64")) {
          archTypex8664 = true;
        }
      }
    } else if (name.equalsIgnoreCase("domain")) {
      for (int i = 0; i < attributes.getLength(); i++) {
        if (attributes.getQName(i).equalsIgnoreCase("type") && attributes.getValue(i).equalsIgnoreCase("kvm")) {
          domainTypeKvm = true;
        }
      }
    } else if (name.equalsIgnoreCase("emulator") && domainTypeKvm && archTypex8664) {
      emulatorFlag = true;
      emulator.delete(0, emulator.length());
    } else if (host) {
      capXml.append("<").append(name);
      for (int i = 0; i < attributes.getLength(); i++) {
        capXml.append(" ").append(attributes.getQName(i)).append("=").append(attributes.getValue(i));
      }
      capXml.append(">");
    }

  }

  public String parseCapabilitiesXml(String capXml) {
    if (!isInitialised) {
      return null;
    }
    try {
      saxParser.parse(new InputSource(new StringReader(capXml)), this);
      return capXml.toString();
    } catch (final SAXException se) {
      logger.warn(se.getMessage());
    } catch (final IOException ie) {
      logger.error(ie.getMessage());
    }
    return null;
  }

  public ArrayList<String> getGuestOsType() {
    return guestOsTypes;
  }

  public String getEmulator() {
    return emulator.toString();
  }
}