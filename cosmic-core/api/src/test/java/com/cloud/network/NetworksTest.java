package com.cloud.network;

import com.cloud.dc.Vlan;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.IsolationType;
import com.cloud.utils.exception.CloudRuntimeException;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dhoogland
 */
public class NetworksTest {

    @Before
    public void setUp() {
    }

    @Test
    public void emptyBroadcastDomainTypeTest() throws URISyntaxException {
        final BroadcastDomainType type = BroadcastDomainType.getTypeOf("");
        Assert.assertEquals("an empty uri should mean a broadcasttype of undecided", BroadcastDomainType.UnDecided, type);
    }

    @Test
    public void vlanBroadcastDomainTypeTest() throws URISyntaxException {
        final String uri1 = "vlan://1";
        final Long value2 = 2L;
        final String uri2 = BroadcastDomainType.Vlan.toUri(value2).toString();
        final BroadcastDomainType type1 = BroadcastDomainType.getTypeOf(uri1);
        final String id1 = BroadcastDomainType.getValue(uri1);
        final String id2 = BroadcastDomainType.getValue(uri2);
        Assert.assertEquals("uri1 should be of broadcasttype vlan", BroadcastDomainType.Vlan, type1);
        Assert.assertEquals("id1 should be \"1\"", "1", id1);
        Assert.assertEquals("id2 should be \"2\"", "2", id2);
    }

    @Test
    public void vlanValueTest() throws URISyntaxException {
        final String uri1 = "vlan://1";
        final String uri2 = "1";
        final String vtag = BroadcastDomainType.Vlan.getValueFrom(BroadcastDomainType.fromString(uri1));
        Assert.assertEquals("vtag should be \"1\"", "1", vtag);
        final BroadcastDomainType tiep1 = BroadcastDomainType.getTypeOf(uri1);
        Assert.assertEquals("the type of uri1 should be 'Vlan'", BroadcastDomainType.Vlan, tiep1);
        final BroadcastDomainType tiep2 = BroadcastDomainType.getTypeOf(uri2);
        Assert.assertEquals("the type of uri1 should be 'Undecided'", BroadcastDomainType.UnDecided, tiep2);
        final BroadcastDomainType tiep3 = BroadcastDomainType.getTypeOf(Vlan.UNTAGGED);
        Assert.assertEquals("the type of uri1 should be 'vlan'", BroadcastDomainType.Native, tiep3);
    }

    @Test
    public void vlanIsolationTypeTest() throws URISyntaxException {
        final String uri1 = "vlan://1";
        final Long value2 = 2L;
        final String uri2 = IsolationType.Vlan.toUri(value2).toString();
        Assert.assertEquals("id1 should be \"vlan://1\"", "vlan://1", uri1);
        Assert.assertEquals("id2 should be \"vlan://2\"", "vlan://2", uri2);
    }

    @Test
    public void otherTypesTest() throws URISyntaxException {
        final String bogeyUri = "lswitch://0";
        final String uri1 = "lswitch:1";
        final String uri2 = "mido://2";
        BroadcastDomainType type = BroadcastDomainType.getTypeOf(bogeyUri);
        String id = BroadcastDomainType.getValue(bogeyUri);
        Assert.assertEquals("uri0 should be of broadcasttype vlan", BroadcastDomainType.Lswitch, type);
        Assert.assertEquals("id0 should be \"//0\"", "//0", id);
        type = BroadcastDomainType.getTypeOf(uri1);
        id = BroadcastDomainType.getValue(uri1);
        Assert.assertEquals("uri1 should be of broadcasttype vlan", BroadcastDomainType.Lswitch, type);
        Assert.assertEquals("id1 should be \"1\"", "1", id);
        type = BroadcastDomainType.getTypeOf(uri2);
        id = BroadcastDomainType.getValue(uri2);
        Assert.assertEquals("uri2 should be of broadcasttype vlan", BroadcastDomainType.Mido, type);
        Assert.assertEquals("id2 should be \"2\"", "2", id);
    }

    @Test
    public void invalidTypesTest() throws URISyntaxException {
        final String uri1 = "https://1";
        final String uri2 = "bla:0";
        final BroadcastDomainType type = BroadcastDomainType.getTypeOf(uri1);
        try {
            /* URI result = */
            BroadcastDomainType.fromString(uri1);
        } catch (final CloudRuntimeException e) {
            Assert.assertEquals("unexpected parameter exception", "string 'https://1' has an unknown BroadcastDomainType.", e.getMessage());
        }
        try {
            /* URI result = */
            BroadcastDomainType.fromString(uri2);
        } catch (final CloudRuntimeException e) {
            Assert.assertEquals("unexpected parameter exception", "string 'bla:0' has an unknown BroadcastDomainType.", e.getMessage());
        }
    }
}
