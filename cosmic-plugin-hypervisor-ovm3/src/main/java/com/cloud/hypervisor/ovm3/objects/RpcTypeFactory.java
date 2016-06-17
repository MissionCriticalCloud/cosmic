// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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

  public RpcTypeFactory(XmlRpcController controller) {
    super(controller);
  }

  @Override
  public TypeParser getParser(XmlRpcStreamConfig config,
      NamespaceContextImpl context, String uri, String localName) {
    if ("".equals(uri) && NullSerializer.NIL_TAG.equals(localName)) {
      return new NullParser();
    } else if ("i8".equals(localName)) {
      return new LongTypeParser();
    } else {
      return super.getParser(config, context, uri, localName);
    }
  }

  @Override
  public TypeSerializer getSerializer(XmlRpcStreamConfig config,
      Object object) throws SAXException {
    if (object instanceof Long) {
      return new LongTypeSerializer();
    } else {
      return super.getSerializer(config, object);
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
    public void write(ContentHandler handler, Object object)
        throws SAXException {
      write(handler, I8_TAG, EX_I8_TAG, object.toString());
    }
  }

  private class LongTypeParser extends AtomicParser {
    @Override
    protected void setResult(String result) throws SAXException {
      try {
        super.setResult(Long.valueOf(result.trim()));
      } catch (final NumberFormatException e) {
        throw new SAXParseException("Failed to parse long value: "
            + result, getDocumentLocator());
      }
    }
  }
}
