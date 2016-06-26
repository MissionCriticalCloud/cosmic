package com.cloud.hypervisor.ovm3.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class XmlTestResultTest {
    private static final String NULL = "<nil/>";

    public String escapeOrNot(final String s) {
        if (s.startsWith("<")) {
            return StringEscapeUtils.escapeXml10(s);
        }
        return s;
    }

    public String errorResponseWrap(final String message) {
        return errorResponseWrap(1, message);
    }

    /*
     * example exceptions.OSError:[Errno.17].File.exists:
     * '/OVS/Repositories/f12842ebf5ed3fe78da1eb0e17f5ede8/VilualDisks/test.raw'
     */
    public String errorResponseWrap(final Integer faultCode, final String message) {
        final String rs = "<?xml version='1.0'?>" + "<methodResponse>" + "<fault>"
                + "<value><struct>" + "<member>" + "<name>faultCode</name>"
                + "<value><int>" + faultCode + "</int></value>" + "</member>"
                + "<member>" + "<name>faultString</name>" + "<value><string>"
                + message + "</string></value>" + "</member>"
                + "</struct></value>" + "</fault>" + "</methodResponse>";
        return rs;
    }

    public String simpleResponseWrap(final String s) {
        return simpleResponseWrapWrapper(s);
    }

    public String simpleResponseWrapWrapper(final String s) {
        return methodResponseWrap("<param>\n" + "<value>" + s + "</value>\n"
                + "</param>\n");
    }

    public String methodResponseWrap(final String towrap) {
        return "<?xml version='1.0'?>\n" + "<methodResponse>\n" + "<params>\n"
                + towrap + "</params>\n" + "</methodResponse>";
    }

    public String getBoolean(final boolean bool) {
        String b = "1";
        if (!bool) {
            b = "0";
        }
        return simpleResponseWrap("boolean", b);
    }

    /* brack the entire wrap ? :) */
    public String simpleResponseWrap(final String type, String s) {
        if (type.contentEquals(NULL)) {
            s = NULL;
        } else {
            s = brack(type, s);
        }
        return simpleResponseWrapWrapper(s);
    }

    private String brack(final String type, final String s) {
        return "<" + type + ">" + s + "</" + type + ">";
    }

    public String getString(final String s) {
        return simpleResponseWrap("string", s);
    }

    public String getNil() {
        return simpleResponseWrap(NULL, NULL);
    }

    public void basicBooleanTest(final boolean result) {
        basicBooleanTest(result, true);
    }

    public void basicBooleanTest(final boolean result, final boolean desired) {
        assertNotNull(result);
        assertEquals(desired, result);
    }

    public void basicStringTest(final String result, final String desired) {
        assertNotNull(result);
        assertEquals(desired, result);
    }

    public void basicIntTest(final Integer result, final Integer desired) {
        assertNotNull(result);
        assertEquals(desired, result);
    }

    public void basicLongTest(final Long result, final Long desired) {
        assertEquals(desired, result);
    }

    public Boolean basicListHasString(final List<String> list, final String x) {
        for (final String y : list) {
            if (y.matches(x)) {
                return true;
            }
        }
        return false;
    }

    public void basicDoubleTest(final Double result, final Double desired) {
        assertEquals(desired, result);
    }
}
