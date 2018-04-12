package com.cloud.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.cloud.api.response.ExceptionResponse;
import com.cloud.api.response.NetworkACLItemResponse;
import com.cloud.api.response.SuccessResponse;

import org.junit.Test;

public class ApiSerializerHelperTest {

    @Test
    public void testFromSerializedStringSimpleSuccessResponse() {
        final String string = "com.cloud.api.response.SuccessResponse/null/{\"success\":true}";
        final Object object = ApiSerializerHelper.fromSerializedString(string);

        assertNotNull(object);
        assertEquals(SuccessResponse.class, object.getClass());

        final SuccessResponse response = (SuccessResponse) object;
        assertTrue(response.getSuccess());
        assertEquals("null", response.getObjectName());
    }

    @Test
    public void testFromSerializedStringOtherSuccessResponse() {
        final String string = "com.cloud.api.response.NetworkACLItemResponse/networkacl/{\"id\":\"00000000-0000-0000-0000-000000000000\",\"protocol\":\"tcp\"," +
                "\"startport\":\"22\",\"traffictype\":\"Ingress\",\"state\":\"Active\",\"cidrlist\":\"10.1.0.0/16,10.2.0.0/16\"," +
                "\"tags\":[],\"aclid\":\"00000000-0000-0000-0000-000000000000\",\"number\":1,\"action\":\"Allow\",\"fordisplay\":true}";
        final Object object = ApiSerializerHelper.fromSerializedString(string);

        assertNotNull(object);
        assertEquals(NetworkACLItemResponse.class, object.getClass());

        final NetworkACLItemResponse response = (NetworkACLItemResponse) object;
        assertEquals("networkacl", response.getObjectName());
        assertEquals(string, ApiSerializerHelper.toSerializedString(response));
    }

    @Test
    public void testFromSerializedStringSimpleExceptionResponse() {
        final String string = "com.cloud.api.response.ExceptionResponse/{\"errorcode\":530," +
                "\"errortext\":\"CloudRuntimeException: A simple runtime exception\"}";
        final Object object = ApiSerializerHelper.fromSerializedString(string);

        assertNotNull(object);
        assertEquals(ExceptionResponse.class, object.getClass());

        final ExceptionResponse response = (ExceptionResponse) object;
        assertEquals(530L, (long) response.getErrorCode());
        assertEquals("CloudRuntimeException: A simple runtime exception", response.getErrorText());
    }

    @Test
    public void testFromSerializedStringSimpleExceptionResponseWithName() {
        final String string = "com.cloud.api.response.ExceptionResponse/exception/{\"errorcode\":530," +
                "\"errortext\":\"CloudRuntimeException: A simple runtime exception\"}";
        final Object object = ApiSerializerHelper.fromSerializedString(string);

        assertNotNull(object);
        assertEquals(ExceptionResponse.class, object.getClass());

        final ExceptionResponse response = (ExceptionResponse) object;
        assertEquals("exception", response.getObjectName());
        assertEquals(530L, (long) response.getErrorCode());
        assertEquals("CloudRuntimeException: A simple runtime exception", response.getErrorText());
    }

    @Test
    public void testFromSerializedStringOtherExceptionResponse() {
        final String string = "com.cloud.api.response.ExceptionResponse/{\"errorcode\":530," +
                "\"errortext\":\"CloudRuntimeException: Failed to attach volume vol001 to VM vm001; " +
                "org.libvirt.LibvirtException: XML error: target 'vdx' duplicated for disk sources " +
                "'/mnt/00000000-0000-0000-0000-000000000000/00000000-0000-0000-0000-000000000000' and " +
                "'/mnt/00000000-0000-0000-0000-000000000000/00000000-0000-0000-0000-000000000000'\"}";
        final Object object = ApiSerializerHelper.fromSerializedString(string);

        assertNotNull(object);
        assertEquals(ExceptionResponse.class, object.getClass());

        final ExceptionResponse response = (ExceptionResponse) object;
        assertNull(response.getObjectName());
        assertEquals(530L, (long) response.getErrorCode());
        assertEquals("CloudRuntimeException: Failed to attach volume vol001 to VM vm001; " +
                "org.libvirt.LibvirtException: XML error: target 'vdx' duplicated for disk sources " +
                "'/mnt/00000000-0000-0000-0000-000000000000/00000000-0000-0000-0000-000000000000' and " +
                "'/mnt/00000000-0000-0000-0000-000000000000/00000000-0000-0000-0000-000000000000'", response.getErrorText());
    }
}
