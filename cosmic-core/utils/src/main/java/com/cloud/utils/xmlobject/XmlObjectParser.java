//

//

package com.cloud.utils.xmlobject;

import com.cloud.utils.exception.CloudRuntimeException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlObjectParser {
    final private InputStream is;

    private XmlObjectParser(final InputStream is) {
        super();
        this.is = is;
    }

    public static XmlObject parseFromFile(final String filePath) {
        final FileInputStream fs;
        try {
            fs = new FileInputStream(new File(filePath));
            final XmlObjectParser p = new XmlObjectParser(fs);
            return p.parse();
        } catch (final FileNotFoundException e) {
            throw new CloudRuntimeException(e);
        }
    }

    private XmlObject parse() {
        final SAXParserFactory spfactory = SAXParserFactory.newInstance();
        try {
            final SAXParser saxParser = spfactory.newSAXParser();
            final XmlHandler handler = new XmlHandler();
            saxParser.parse(is, handler);
            return handler.getRoot();
        } catch (final Exception e) {
            throw new CloudRuntimeException(e);
        }
    }

    public static XmlObject parseFromString(final String xmlString) {
        final InputStream stream = new ByteArrayInputStream(xmlString.getBytes());
        final XmlObjectParser p = new XmlObjectParser(stream);
        final XmlObject obj = p.parse();
        if (obj.getText() != null && obj.getText().replaceAll("\\n", "").replaceAll("\\r", "").replaceAll(" ", "").isEmpty()) {
            obj.setText(null);
        }
        return obj;
    }

    private class XmlHandler extends DefaultHandler {
        private final Stack<XmlObject> stack;
        private String currentValue;
        private XmlObject root;

        XmlHandler() {
            stack = new Stack<>();
        }

        @Override
        public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
            //System.out.println(String.format("startElement: namespaceURI:%s, localName:%s, qName:%s", namespaceURI, localName, qName));
            currentValue = null;
            final XmlObject obj = new XmlObject();
            for (int i = 0; i < atts.getLength(); i++) {
                obj.putElement(atts.getQName(i), atts.getValue(i));
            }
            obj.setTag(qName);
            if (!stack.isEmpty()) {
                final XmlObject parent = stack.peek();
                parent.putElement(qName, obj);
            }
            stack.push(obj);
        }

        @Override
        public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
            final XmlObject currObj = stack.pop();
            if (currentValue != null) {
                currObj.setText(currentValue);
            }

            if (stack.isEmpty()) {
                root = currObj;
            }

            //System.out.println(String.format("endElement: namespaceURI:%s, localName:%s, qName:%s", namespaceURI, localName, qName));
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            final StringBuilder str = new StringBuilder();
            str.append(ch, start, length);
            currentValue = str.toString();
            //System.out.println(String.format("characters: %s", str.toString()));
        }

        XmlObject getRoot() {
            return root;
        }
    }
}
