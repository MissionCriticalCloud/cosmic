package com.cloud.hypervisor.ovm3.objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OvmObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(OvmObject.class);
    private static final List<?> emptyParams = new ArrayList<>();
    private volatile Connection client;
    private boolean success = false;

    public OvmObject() {
    }

    public Connection getClient() {
        return client;
    }

    public synchronized void setClient(final Connection connection) {
        client = connection;
    }

    /* remove dashes from uuids */
    public String deDash(final String str) {
        return str.replaceAll("-", "");
    }

    /* generate a uuid */
    public String newUuid() {
        return UUID.randomUUID().toString();
    }

    /* generate a uuid */
    public String newUuid(final String str) {
        return UUID.nameUUIDFromBytes(str.getBytes(Charset.defaultCharset())).toString();
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }

    @SafeVarargs
    public final <T> Boolean nullIsFalseCallWrapper(final String call, final T... args) throws Ovm3ResourceException {
        return nullCallWrapper(call, false, args);
    }

    /* should check on nil ? */
    @SafeVarargs
    public final <T> Boolean nullCallWrapper(final String call, final Boolean nullReturn, final T... args) throws Ovm3ResourceException {
        final Object x = callWrapper(call, args);
        if (x == null) {
            return nullReturn;
        } else if (!nullReturn) {
            return true;
        }
        return false;
    }

    public final <T> Object callWrapper(final String call, final T... args)
            throws Ovm3ResourceException {
        final List<T> params = new ArrayList<>();
        for (final T param : args) {
            params.add(param);
        }
        try {
            return client.call(call, params);
        } catch (final XmlRpcException e) {
            final String msg = "Client call " + call + " to " + client.getIp() + " with " + params + " went wrong: "
                    + e.getMessage();
            throw new Ovm3ResourceException(msg, e);
        }
    }

    @SafeVarargs
    public final <T> Boolean nullIsTrueCallWrapper(final String call, final T... args) throws Ovm3ResourceException {
        return nullCallWrapper(call, true, args);
    }

    /* returns a single string */
    public Map<String, Long> callMap(final String call) throws Ovm3ResourceException {
        return (HashMap<String, Long>) callWrapper(call);
    }

    /* capture most of the calls here */
    public Object callWrapper(final String call) throws Ovm3ResourceException {
        try {
            return client.call(call, emptyParams);
        } catch (final XmlRpcException e) {
            final String msg = "Client call " + call + " to " + client.getIp() + " went wrong: " + e.getMessage();
            throw new Ovm3ResourceException(msg, e);
        }
    }

    public <T> String callString(final String call, final T... args) throws Ovm3ResourceException {
        final Object result = callWrapper(call, args);
        if (result == null) {
            return null;
        }
        if (result instanceof String || result instanceof Integer || result instanceof Long || result instanceof HashMap) {
            return result.toString();
        }

        final Object[] results = (Object[]) result;

        if (results.length == 0) {
            return null;
        }
        if (results.length == 1) {
            return results[0].toString();
        }
        return null;
    }

    /* was String, Object before */
    public <E> Map<String, E> xmlToMap(final String path, final Document xmlDocument)
            throws Ovm3ResourceException {
        final XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
        final XPath xPath = factory.newXPath();
        try {
            final XPathExpression xPathExpression = xPath.compile(path);
            final NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument,
                    XPathConstants.NODESET);
            final Map<String, E> myMap = new HashMap<>();
            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                final NodeList nodeListFor = nodeList.item(ind).getChildNodes();
                for (int index = 0; index < nodeListFor.getLength(); index++) {
                    final String rnode = nodeListFor.item(index).getNodeName();
                    final NodeList nodeListFor2 = nodeListFor.item(index).getChildNodes();
                    if (nodeListFor2.getLength() > 1) {
            /* Do we need to figure out all the sub elements here and put them in a map? */
                    } else {
                        final String element = nodeListFor.item(index).getTextContent();
                        myMap.put(rnode, (E) element);
                    }
                }
            }
            return myMap;
        } catch (final XPathExpressionException e) {
            throw new Ovm3ResourceException("Problem parsing XML to Map:", e);
        }
    }

    public List<String> xmlToList(final String path, final Document xmlDocument)
            throws Ovm3ResourceException {
        final List<String> list = new ArrayList<>();
        final XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
        final XPath xPath = factory.newXPath();
        try {
            final XPathExpression xPathExpression = xPath.compile(path);
            final NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument,
                    XPathConstants.NODESET);
            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                if (!nodeList.item(ind).getTextContent().isEmpty()) {
                    list.add("" + nodeList.item(ind).getTextContent());
                } else {
                    list.add("" + nodeList.item(ind).getNodeValue());
                }
            }
            return list;
        } catch (final XPathExpressionException e) {
            throw new Ovm3ResourceException("Problem parsing XML to List: ", e);
        }
    }

    public String xmlToString(final String path, final Document xmlDocument)
            throws Ovm3ResourceException {
        final XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
        final XPath xPath = factory.newXPath();
        try {
            final XPathExpression xPathExpression = xPath.compile(path);
            final NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument,
                    XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        } catch (final NullPointerException e) {
            LOGGER.info("Got no items back from parsing, returning null: " + e);
            return null;
        } catch (final XPathExpressionException e) {
            throw new Ovm3ResourceException("Problem parsing XML to String: ", e);
        }
    }

    public Document prepParse(final String input)
            throws Ovm3ResourceException {
        final DocumentBuilderFactory builderfactory = DocumentBuilderFactory.newInstance();
        builderfactory.setNamespaceAware(true);

        final DocumentBuilder builder;
        try {
            builder = builderfactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new Ovm3ResourceException("Unable to create document Builder: ", e);
        }
        final Document xmlDocument;
        try {
            xmlDocument = builder.parse(new InputSource(new StringReader(
                    input)));
        } catch (SAXException | IOException e) {
            LOGGER.info(e.getClass() + ": ", e);
            throw new Ovm3ResourceException("Unable to parse XML: ", e);
        }
        return xmlDocument;
    }
}
