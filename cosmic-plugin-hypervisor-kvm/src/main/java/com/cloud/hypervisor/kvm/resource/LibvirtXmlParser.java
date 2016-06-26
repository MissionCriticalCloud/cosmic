package com.cloud.hypervisor.kvm.resource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LibvirtXmlParser extends DefaultHandler {

    protected static final SAXParserFactory saxParserFactory;

    static {
        saxParserFactory = SAXParserFactory.newInstance();
    }

    private final Logger logger = LoggerFactory.getLogger(LibvirtXmlParser.class);
    protected SAXParser saxParser;
    protected boolean isInitialised;

    public LibvirtXmlParser() {
        try {
            saxParser = saxParserFactory.newSAXParser();
            isInitialised = true;
        } catch (final ParserConfigurationException e) {
            logger.trace("Ignoring xml parser error.", e);
        } catch (final SAXException e) {
            logger.trace("Ignoring xml parser error.", e);
        }
    }

    public boolean parseDomainXml(final String domXml) {
        if (!isInitialised) {
            return false;
        }
        try {
            saxParser.parse(new InputSource(new StringReader(domXml)), this);
            return true;
        } catch (final SAXException se) {
            logger.warn(se.getMessage());
        } catch (final IOException ie) {
            logger.error(ie.getMessage());
        }
        return false;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
    }
}
