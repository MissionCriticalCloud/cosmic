package org.apache.cloudstack.api.response;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

public final class HostResponseTest extends TestCase {

    private static final String VALID_KEY = "validkey";
    private static final String VALID_VALUE = "validvalue";

    @Test
    public void testSetDetailsNull() {

        final HostResponse hostResponse = new HostResponse();
        hostResponse.setDetails(null);

        assertEquals(null, hostResponse.getDetails());
    }

    @Test
    public void testSetDetailsWithRootCredentials() {

        final HostResponse hostResponse = new HostResponse();
        final Map details = new HashMap<>();

        details.put(VALID_KEY, VALID_VALUE);
        details.put("username", "test");
        details.put("password", "password");

        final Map expectedDetails = new HashedMap();
        expectedDetails.put(VALID_KEY, VALID_VALUE);

        hostResponse.setDetails(details);
        final Map actualDetails = hostResponse.getDetails();

        assertTrue(details != actualDetails);
        assertEquals(expectedDetails, actualDetails);
    }

    @Test
    public void testSetDetailsWithoutRootCredentials() {

        final HostResponse hostResponse = new HostResponse();
        final Map details = new HashMap<>();

        details.put(VALID_KEY, VALID_VALUE);

        final Map expectedDetails = new HashedMap();
        expectedDetails.put(VALID_KEY, VALID_VALUE);

        hostResponse.setDetails(details);
        final Map actualDetails = hostResponse.getDetails();

        assertTrue(details != actualDetails);
        assertEquals(expectedDetails, actualDetails);
    }
}
