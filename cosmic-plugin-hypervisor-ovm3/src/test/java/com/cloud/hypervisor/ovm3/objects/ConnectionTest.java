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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.util.SAXParsers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/*
 * This is a stub for XML parsing into result sets, it also contains test for
 * Connection
 */
public class ConnectionTest extends Connection {
    private final Logger LOGGER = LoggerFactory.getLogger(ConnectionTest.class);
    private final Map<String, String> methodResponse = new HashMap<>();
    XmlTestResultTest results = new XmlTestResultTest();
    String result;
    List<String> multiRes = new ArrayList<>();
    String hostIp;

    public ConnectionTest() {
    }

    @Override
    public Object callTimeoutInSec(final String method, final List<?> params, final int timeout,
                                   final boolean debug) throws XmlRpcException {
        final XmlRpcStreamConfig config = new XmlRpcHttpRequestConfigImpl();
        final XmlRpcClient client = new XmlRpcClient();
        client.setTypeFactory(new RpcTypeFactory(client));
        final XmlRpcResponseParser parser = new XmlRpcResponseParser(
                (XmlRpcStreamRequestConfig) config, client.getTypeFactory());
        final XMLReader xr = SAXParsers.newXMLReader();
        xr.setContentHandler(parser);
        try {
            String result = null;
            if (getMethodResponse(method) != null) {
                result = getMethodResponse(method);
                LOGGER.debug("methodresponse call: " + method + " - " + params);
                LOGGER.trace("methodresponse reply: " + result);
            }
            if (result == null && multiRes.size() >= 0) {
                result = getResult();
                LOGGER.debug("getresult call: " + method + " - " + params);
                LOGGER.trace("getresult reply: " + result);
            }
            xr.parse(new InputSource(new StringReader(result)));
        } catch (final Exception e) {
            throw new XmlRpcException("Exception: " + e.getMessage(), e);
        }
        if (parser.getErrorCode() != 0) {
            throw new XmlRpcException("Fault received[" + parser.getErrorCode()
                    + "]: " + parser.getErrorMessage());
        }
        return parser.getResult();
    }

    public String getMethodResponse(final String method) {
        if (methodResponse.containsKey(method)) {
            return methodResponse.get(method);
        }
        return null;
    }

    public String getResult() {
        return popResult();
    }

    public void setResult(final List<String> l) {
        multiRes = new ArrayList<>();
        multiRes.addAll(l);
    }

    public String popResult() {
        final String res = multiRes.get(0);
        if (multiRes.size() > 1) {
            multiRes.remove(0);
        }
        return res;
    }

    public void setMethodResponse(final String method, final String response) {
        methodResponse.put(method, response);
    }

    public void removeMethodResponse(final String method) {
        if (methodResponse.containsKey(method)) {
            methodResponse.remove(method);
        }
    }

    public void setResult(final String res) {
        multiRes = new ArrayList<>();
        multiRes.add(0, res);
    }

    public void setNull() {
        multiRes = new ArrayList<>();
        multiRes.add(0, null);
    }

    /* result chainsing */
    public void addResult(final String e) {
        multiRes.add(e);
    }

    public void addNull() {
        multiRes.add(null);
    }

    public List<String> resultList() {
        return multiRes;
    }

    @Test
    public void testConnection() {
        final String host = "ovm-1";
        final String user = "admin";
        final String pass = "password";
        final Integer port = 8899;
        final List<?> emptyParams = new ArrayList<>();
        final Connection con = new Connection(host, port, user, pass);
        results.basicStringTest(con.getIp(), host);
        results.basicStringTest(con.getUserName(), user);
        results.basicStringTest(con.getPassword(), pass);
        results.basicIntTest(con.getPort(), port);
        try {
            con.callTimeoutInSec("ping", emptyParams, 1);
            // con.call("ping", emptyParams, 1, false);
        } catch (final XmlRpcException e) {
            // TODO Auto-generated catch block
            System.out.println("Exception: " + e);
        }
        new Connection(host, user, pass);
    }
}
