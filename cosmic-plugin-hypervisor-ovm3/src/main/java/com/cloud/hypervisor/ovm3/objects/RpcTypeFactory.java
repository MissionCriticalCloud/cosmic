package com.cloud.hypervisor.ovm3.objects;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.AtomicParser;
import org.apache.xmlrpc.parser.NullParser;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.NullSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.apache.xmlrpc.serializer.TypeSerializerImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class RpcTypeFactory extends TypeFactoryImpl {

    public RpcTypeFactory(final XmlRpcController controller) {
        super(controller);
    }

    @Override
    public TypeSerializer getSerializer(final XmlRpcStreamConfig config,
                                        final Object object) throws SAXException {
        if (object instanceof Long) {
            return new LongTypeSerializer();
        } else {
            return super.getSerializer(config, object);
        }
    }

    @Override
    public TypeParser getParser(final XmlRpcStreamConfig config,
                                final NamespaceContextImpl context, final String uri, final String localName) {
        if ("".equals(uri) && NullSerializer.NIL_TAG.equals(localName)) {
            return new NullParser();
        } else if ("i8".equals(localName)) {
            return new LongTypeParser();
        } else {
            return super.getParser(config, context, uri, localName);
        }
    }

    private class LongTypeSerializer extends TypeSerializerImpl {
        /*
         * Tag name of an i8 value.
         */
        public static final String I8_TAG = "i8";
        /*
         * Fully qualified name of an i8 value.
         */
        public static final String EX_I8_TAG = "i8";

        @Override
        public void write(final ContentHandler handler, final Object object)
                throws SAXException {
            write(handler, I8_TAG, EX_I8_TAG, object.toString());
        }
    }

    private class LongTypeParser extends AtomicParser {
        @Override
        protected void setResult(final String result) throws SAXException {
            try {
                super.setResult(Long.valueOf(result.trim()));
            } catch (final NumberFormatException e) {
                throw new SAXParseException("Failed to parse long value: "
                        + result, getDocumentLocator());
            }
        }
    }
}
