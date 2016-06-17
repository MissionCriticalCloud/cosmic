package com.cloud.hypervisor.kvm.resource;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LibvirtStoragePoolXmlParser {

  private static final Logger s_logger = LoggerFactory.getLogger(LibvirtStoragePoolXmlParser.class);

  public LibvirtStoragePoolDef parseStoragePoolXml(String poolXml) {
    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      final InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(poolXml));
      final Document doc = builder.parse(is);

      final Element rootElement = doc.getDocumentElement();
      final String type = rootElement.getAttribute("type");

      final String uuid = getTagValue("uuid", rootElement);

      final String poolName = getTagValue("name", rootElement);

      final Element source = (Element) rootElement.getElementsByTagName("source").item(0);
      final String host = getAttrValue("host", "name", source);
      final String format = getAttrValue("format", "type", source);

      if (type.equalsIgnoreCase("rbd")) {
        final int port = Integer.parseInt(getAttrValue("host", "port", source));
        final String pool = getTagValue("name", source);

        final Element auth = (Element) source.getElementsByTagName("auth").item(0);

        if (auth != null) {
          final String authUsername = auth.getAttribute("username");
          final String authType = auth.getAttribute("type");
          return new LibvirtStoragePoolDef(LibvirtStoragePoolDef.PoolType.valueOf(type.toUpperCase()), poolName, uuid,
              host, port, pool, authUsername,
              LibvirtStoragePoolDef.AuthenticationType.valueOf(authType.toUpperCase()), uuid);
        } else {
          return new LibvirtStoragePoolDef(LibvirtStoragePoolDef.PoolType.valueOf(type.toUpperCase()), poolName, uuid,
              host, port, pool, "");
        }
        /* Gluster is a sub-type of LibvirtStoragePoolDef.poolType.NETFS, need to check format */
      } else if (format != null && format.equalsIgnoreCase("glusterfs")) {
        /* libvirt does not return the default port, but requires it for a disk-definition */
        int port = 24007;

        final String path = getAttrValue("dir", "path", source);

        final Element target = (Element) rootElement.getElementsByTagName(
            "target").item(0);
        final String targetPath = getTagValue("path", target);

        final String portValue = getAttrValue("host", "port", source);
        if (portValue != null && !portValue.isEmpty()) {
          port = Integer.parseInt(portValue);
        }

        return new LibvirtStoragePoolDef(LibvirtStoragePoolDef.PoolType.valueOf(format.toUpperCase()),
            poolName, uuid, host, port, path, targetPath);
      } else {
        final String path = getAttrValue("dir", "path", source);

        final Element target = (Element) rootElement.getElementsByTagName("target").item(0);
        final String targetPath = getTagValue("path", target);

        return new LibvirtStoragePoolDef(LibvirtStoragePoolDef.PoolType.valueOf(type.toUpperCase()), poolName, uuid,
            host, path, targetPath);
      }
    } catch (final ParserConfigurationException e) {
      s_logger.debug(e.toString());
    } catch (final SAXException e) {
      s_logger.debug(e.toString());
    } catch (final IOException e) {
      s_logger.debug(e.toString());
    }
    return null;
  }

  private static String getTagValue(String tag, Element element) {
    final NodeList nlList = element.getElementsByTagName(tag).item(0).getChildNodes();
    final Node nValue = nlList.item(0);

    return nValue.getNodeValue();
  }

  private static String getAttrValue(String tag, String attr, Element element) {
    final NodeList tagNode = element.getElementsByTagName(tag);
    if (tagNode.getLength() == 0) {
      return null;
    }
    final Element node = (Element) tagNode.item(0);
    return node.getAttribute(attr);
  }
}
