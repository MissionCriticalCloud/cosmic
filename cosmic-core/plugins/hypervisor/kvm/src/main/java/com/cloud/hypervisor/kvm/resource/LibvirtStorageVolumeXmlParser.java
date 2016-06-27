package com.cloud.hypervisor.kvm.resource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LibvirtStorageVolumeXmlParser {
    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtStorageVolumeXmlParser.class);

    public LibvirtStorageVolumeDef parseStorageVolumeXml(final String volXml) {
        final DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            final InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(volXml));
            final Document doc = builder.parse(is);

            final Element rootElement = doc.getDocumentElement();

            final String VolName = getTagValue("name", rootElement);
            final Element target = (Element) rootElement.getElementsByTagName("target").item(0);
            final String format = getAttrValue("type", "format", target);
            final Long capacity = Long.parseLong(getTagValue("capacity", rootElement));
            return new LibvirtStorageVolumeDef(VolName, capacity, LibvirtStorageVolumeDef.VolumeFormat.getFormat(format),
                    null, null);
        } catch (final ParserConfigurationException e) {
            s_logger.debug(e.toString());
        } catch (final SAXException e) {
            s_logger.debug(e.toString());
        } catch (final IOException e) {
            s_logger.debug(e.toString());
        }
        return null;
    }

    private static String getTagValue(final String tag, final Element element) {
        final NodeList nlList = element.getElementsByTagName(tag).item(0).getChildNodes();
        final Node nValue = nlList.item(0);

        return nValue.getNodeValue();
    }

    private static String getAttrValue(final String tag, final String attr, final Element element) {
        final NodeList tagNode = element.getElementsByTagName(tag);
        if (tagNode.getLength() == 0) {
            return null;
        }
        final Element node = (Element) tagNode.item(0);
        return node.getAttribute(attr);
    }
}
