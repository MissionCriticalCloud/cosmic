package com.cloud.utils.net;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils.SupersetOrSubset;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6Network;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetUtilsTest {

    private static final Logger s_logger = LoggerFactory.getLogger(NetUtilsTest.class);

    @Test
    public void testGetRandomIpFromCidrWithSize24() throws Exception {
        final String cidr = "192.168.124.1";
        final int size = 24;
        final int netCharacters = 12;

        final long ip = NetUtils.getRandomIpFromCidr(cidr, size, new TreeSet<>());

        assertThat("The ip " + NetUtils.long2Ip(ip) + " retrieved must be within the cidr " + cidr + "/" + size, cidr.substring(0, netCharacters), equalTo(NetUtils.long2Ip(ip)
                                                                                                                                                                   .substring(0,
                                                                                                                                                                           netCharacters)));
    }

    @Test
    public void testGetRandomIpFromCidrWithSize16() throws Exception {
        final String cidr = "192.168.124.1";
        final int size = 16;
        final int netCharacters = 8;

        final long ip = NetUtils.getRandomIpFromCidr(cidr, 16, new TreeSet<>());

        assertThat("The ip " + NetUtils.long2Ip(ip) + " retrieved must be within the cidr " + cidr + "/" + size, cidr.substring(0, netCharacters), equalTo(NetUtils.long2Ip(ip)
                                                                                                                                                                   .substring(0,
                                                                                                                                                                           netCharacters)));
    }

    @Test
    public void testGetRandomIpFromCidrWithSize8() throws Exception {
        final String cidr = "192.168.124.1";
        final int size = 8;
        final int netCharacters = 4;

        final long ip = NetUtils.getRandomIpFromCidr(cidr, 16, new TreeSet<>());

        assertThat("The ip " + NetUtils.long2Ip(ip) + " retrieved must be within the cidr " + cidr + "/" + size, cidr.substring(0, netCharacters), equalTo(NetUtils.long2Ip(ip)
                                                                                                                                                                   .substring(0,
                                                                                                                                                                           netCharacters)));
    }

    @Test
    public void testGetRandomIpFromCidrUsignAvoid() throws Exception {
        final String cidr = "192.168.124.1";
        final int size = 30;

        final SortedSet<Long> avoid = new TreeSet<>();
        long ip = NetUtils.getRandomIpFromCidr(cidr, size, avoid);
        assertThat("We should be able to retrieve an ip on the first call.", ip, not(equalTo(-1L)));
        avoid.add(ip);
        ip = NetUtils.getRandomIpFromCidr(cidr, size, avoid);
        assertThat("We should be able to retrieve an ip on the second call.", ip, not(equalTo(-1L)));
        assertThat("ip returned is not in the avoid list", avoid, not(contains(ip)));
        avoid.add(ip);
        ip = NetUtils.getRandomIpFromCidr(cidr, size, avoid);
        assertThat("We should be able to retrieve an ip on the third call.", ip, not(equalTo(-1L)));
        assertThat("ip returned is not in the avoid list", avoid, not(contains(ip)));
        avoid.add(ip);
        ip = NetUtils.getRandomIpFromCidr(cidr, size, avoid);
        assertEquals("This should be -1 because we ran out of ip addresses: " + ip, ip, -1);
    }

    @Test
    public void testIsValidS2SVpnPolicy() {
        assertTrue(NetUtils.isValidS2SVpnPolicy("esp", "aes128-sha1"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("esp", "3des-sha1"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("esp", "3des-sha1,aes256-sha1"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("esp", "3des-md5;modp1024"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("esp", "3des-sha256,aes128-sha512;modp1536"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("ike", "3des-sha1;modp3072,aes128-sha1;modp1536"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("ike", "3des-md5;modp1024"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("ike", "3des-sha1;modp3072,aes128-sha1;modp1536"));
        assertTrue(NetUtils.isValidS2SVpnPolicy("ike", "3des-sha256;modp3072,aes128-sha512;modp1536"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("ike", "aes128-sha1"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("ike", "3des-sha1"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("ike", "3des-sha1,aes256-sha1"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", "des-md5;modp1024,aes128-sha1;modp1536"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", "des-sha1"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", "abc-123,ase-sha1"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", "de-sh,aes-sha1"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", ""));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", ";modp1536"));
        assertFalse(NetUtils.isValidS2SVpnPolicy("esp", ",aes;modp1536,,,"));
    }

    @Test
    public void testGetIp6FromRange() {
        assertEquals(NetUtils.getIp6FromRange("1234:5678::1-1234:5678::1"), "1234:5678::1");
        for (int i = 0; i < 5; i++) {
            final String ip = NetUtils.getIp6FromRange("1234:5678::1-1234:5678::2");
            assertThat(ip, anyOf(equalTo("1234:5678::1"), equalTo("1234:5678::2")));
            s_logger.info("IP is " + ip);
        }
        String ipString = null;
        final IPv6Address ipStart = IPv6Address.fromString("1234:5678::1");
        final IPv6Address ipEnd = IPv6Address.fromString("1234:5678::ffff:ffff:ffff:ffff");
        for (int i = 0; i < 10; i++) {
            ipString = NetUtils.getIp6FromRange(ipStart.toString() + "-" + ipEnd.toString());
            s_logger.info("IP is " + ipString);
            final IPv6Address ip = IPv6Address.fromString(ipString);
            assertThat(ip, greaterThanOrEqualTo(ipStart));
            assertThat(ip, lessThanOrEqualTo(ipEnd));
        }
    }

    @Test
    public void testCountIp6InRange() {
        assertEquals(new BigInteger("2"), NetUtils.countIp6InRange("1234:5678::1-1234:5678::2"));
    }

    @Test
    public void testCountIp6InRangeWithInvalidRange() {
        assertEquals(null, NetUtils.countIp6InRange("1234:5678::2-1234:5678::0"));
    }

    @Test
    public void testCountIp6InRangeWithNullStart() {
        assertEquals(null, NetUtils.countIp6InRange("-1234:5678::0"));
    }

    @Test
    public void testCountIp6InRangeWithNoEnd() {
        assertEquals(new BigInteger("1"), NetUtils.countIp6InRange("1234:5678::2"));
    }

    @Test
    public void testIsValidMacAddr() {
        assertTrue(NetUtils.isValidMac("ee:12:34:5:32:ff"));
        assertTrue(NetUtils.isValidMac("ee.12.34.5.32.ff"));
        assertTrue(NetUtils.isValidMac("ee-12-34-5-32-ff"));
        assertFalse(NetUtils.isValidMac("aa.12:34:5:32:ff"));
        assertFalse(NetUtils.isValidMac("gg.gg:gg:gg:gg:gg"));
    }

    @Test
    public void testIsUnicastMac() {
        assertTrue(NetUtils.isUnicastMac("ee:12:34:5:32:ff"));
        assertFalse(NetUtils.isUnicastMac("ff:12:34:5:32:ff"));
        assertFalse(NetUtils.isUnicastMac("01:12:34:5:32:ff"));
        assertTrue(NetUtils.isUnicastMac("00:ff:ff:ff:ff:ff"));
    }

    @Test
    public void testGetIp6CidrSize() {
        assertEquals(NetUtils.getIp6CidrSize("1234:5678::1/32"), 32);
        assertEquals(NetUtils.getIp6CidrSize("1234:5678::1"), 0);
    }

    @Test
    public void testIsValidIp6Cidr() {
        assertTrue(NetUtils.isValidIp6Cidr("1234:5678::1/64"));
        assertFalse(NetUtils.isValidIp6Cidr("1234:5678::1"));
    }

    @Test
    public void testIsValidIpv6() {
        assertTrue(NetUtils.isValidIp6("fc00::1"));
        assertFalse(NetUtils.isValidIp6(""));
        assertFalse(NetUtils.isValidIp6(null));
        assertFalse(NetUtils.isValidIp6("1234:5678::1/64"));
    }

    @Test
    public void testIsIp6InRange() {
        assertTrue(NetUtils.isIp6InRange("1234:5678:abcd::1", "1234:5678:abcd::1-1234:5678:abcd::1"));
        assertFalse(NetUtils.isIp6InRange("1234:5678:abcd::1", "1234:5678:abcd::2-1234:5678:abcd::1"));
        assertFalse(NetUtils.isIp6InRange("1234:5678:abcd::1", null));
        assertTrue(NetUtils.isIp6InRange("1234:5678:abcd::1", "1234:5678::1-1234:5679::1"));
    }

    @Test
    public void testIsIp6InNetwork() {
        assertFalse(NetUtils.isIp6InNetwork(IPv6Address.fromString("1234:5678:abcd::1"), IPv6Network.fromString("1234:5678::/64")));
        assertTrue(NetUtils.isIp6InNetwork(IPv6Address.fromString("1234:5678::1"), IPv6Network.fromString("1234:5678::/64")));
        assertTrue(NetUtils.isIp6InNetwork(IPv6Address.fromString("1234:5678::ffff:ffff:ffff:ffff"), IPv6Network.fromString("1234:5678::/64")));
        assertTrue(NetUtils.isIp6InNetwork(IPv6Address.fromString("1234:5678::"), IPv6Network.fromString("1234:5678::/64")));
    }

    @Test
    public void testGetNextIp6InRange() {
        String range = "1234:5678::1-1234:5678::8000:0000";
        assertEquals(NetUtils.getNextIp6InRange("1234:5678::8000:0", range), "1234:5678::1");
        assertEquals(NetUtils.getNextIp6InRange("1234:5678::7fff:ffff", range), "1234:5678::8000:0");
        assertEquals(NetUtils.getNextIp6InRange("1234:5678::1", range), "1234:5678::2");
        range = "1234:5678::1-1234:5678::ffff:ffff:ffff:ffff";
        assertEquals(NetUtils.getNextIp6InRange("1234:5678::ffff:ffff:ffff:ffff", range), "1234:5678::1");
    }

    @Test
    public void testIsIp6RangeOverlap() {
        assertFalse(NetUtils.isIp6RangeOverlap("1234:5678::1-1234:5678::ffff", "1234:5678:1::1-1234:5678:1::ffff"));
        assertTrue(NetUtils.isIp6RangeOverlap("1234:5678::1-1234:5678::ffff", "1234:5678::2-1234:5678::f"));
        assertTrue(NetUtils.isIp6RangeOverlap("1234:5678::f-1234:5678::ffff", "1234:5678::2-1234:5678::f"));
        assertFalse(NetUtils.isIp6RangeOverlap("1234:5678::f-1234:5678::ffff", "1234:5678::2-1234:5678::e"));
        assertFalse(NetUtils.isIp6RangeOverlap("1234:5678::f-1234:5678::f", "1234:5678::2-1234:5678::e"));
    }

    @Test
    public void testStandardizeIp6Address() {
        assertEquals(NetUtils.standardizeIp6Address("1234:0000:0000:5678:0000:0000:ABCD:0001"), "1234::5678:0:0:abcd:1");
        assertEquals(NetUtils.standardizeIp6Cidr("1234:0000:0000:5678:0000:0000:ABCD:0001/64"), "1234:0:0:5678::/64");
    }

    @Test
    public void testGenerateUriForPvlan() {
        assertEquals("pvlan://123-i456", NetUtils.generateUriForPvlan("123", "456").toString());
    }

    @Test
    public void testGetPrimaryPvlanFromUri() {
        assertEquals("123", NetUtils.getPrimaryPvlanFromUri(NetUtils.generateUriForPvlan("123", "456")));
    }

    @Test
    public void testGetIsolatedPvlanFromUri() {
        assertEquals("456", NetUtils.getIsolatedPvlanFromUri(NetUtils.generateUriForPvlan("123", "456")));
    }

    @Test
    public void testIsValidCIDR() throws Exception {
        //Test to check IP Range of 2 CIDR
        final String cidrFirst = "10.0.144.0/20";
        final String cidrSecond = "10.0.151.0/20";
        final String cidrThird = "10.0.144.0/21";

        assertTrue(NetUtils.isValidIp4Cidr(cidrFirst));
        assertTrue(NetUtils.isValidIp4Cidr(cidrSecond));
        assertTrue(NetUtils.isValidIp4Cidr(cidrThird));
    }

    @Test
    public void testGetAllIpsFromCidr() throws Exception {
        final Set<Long> emptyList = new HashSet<>();

        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0", 24L, emptyList).size() == 254);
        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0", 25L, emptyList).size() == 126);
        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0", 30L, emptyList).size() == 2);
        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0/31", emptyList).size() == 0);
        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0/32", emptyList).size() == 0);

        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0", 23L, emptyList).size() == 510);
        assertTrue(NetUtils.getAllIpsFromCidr("10.0.0.0", 16L, emptyList).size() == 65534);
    }

    @Test
    public void testIsValidCidrList() throws Exception {
        final String cidrFirst = "10.0.144.0/20,1.2.3.4/32,5.6.7.8/24";
        final String cidrSecond = "10.0.151.0/20,129.0.0.0/4";
        final String cidrThird = "10.0.144.0/21";

        assertTrue(NetUtils.isValidCidrList(cidrFirst));
        assertTrue(NetUtils.isValidCidrList(cidrSecond));
        assertTrue(NetUtils.isValidCidrList(cidrThird));
    }

    @Test
    public void testIsSameIpRange() {
        final String cidrFirst = "10.0.144.0/20";
        final String cidrSecond = "10.0.151.0/20";
        final String cidrThird = "10.0.144.0/21";

        //Check for exactly same CIDRs
        assertTrue(NetUtils.isSameIpRange(cidrFirst, cidrFirst));
        //Check for 2 different CIDRs, but same IP Range
        assertTrue(NetUtils.isSameIpRange(cidrFirst, cidrSecond));
        //Check for 2 different CIDRs and different IP Range
        assertFalse(NetUtils.isSameIpRange(cidrFirst, cidrThird));
        //Check for Incorrect format of CIDR
        assertFalse(NetUtils.isSameIpRange(cidrFirst, "10.3.6.5/50"));
    }

    @Test
    public void testGenerateMacOnIncrease() {
        String mac = "06:01:23:00:45:67";
        assertEquals("06:01:25:00:45:67", NetUtils.generateMacOnIncrease(mac, 2));
        assertEquals("06:01:33:00:45:67", NetUtils.generateMacOnIncrease(mac, 16));
        mac = "06:ff:ff:00:45:67";
        assertEquals("06:00:00:00:45:67", NetUtils.generateMacOnIncrease(mac, 1));
        assertEquals("06:00:0f:00:45:67", NetUtils.generateMacOnIncrease(mac, 16));
    }

    @Test
    public void testGetLocalIPString() {
        assertNotNull(NetUtils.getLocalIPString());
    }

    @Test
    public void testSameIsolationId() {
        assertTrue(NetUtils.isSameIsolationId("1", "vlan://1"));
        assertTrue(NetUtils.isSameIsolationId("", null));
        assertTrue(NetUtils.isSameIsolationId("UnTagged", "vlan://uNtAGGED"));
        assertFalse(NetUtils.isSameIsolationId("2", "vlan://uNtAGGED"));
        assertFalse(NetUtils.isSameIsolationId("2", "vlan://3"));
        assertFalse(NetUtils.isSameIsolationId("bla", null));
    }

    @Test
    public void testValidateGuestCidr() throws Exception {
        final String guestCidr = "192.168.1.0/24";

        assertTrue(NetUtils.validateGuestCidr(guestCidr));
    }

    @Test
    public void testMac2Long() {
        assertEquals(0l, NetUtils.mac2Long("00:00:00:00:00:00"));
        assertEquals(1l, NetUtils.mac2Long("00:00:00:00:00:01"));
        assertEquals(0xFFl, NetUtils.mac2Long("00:00:00:00:00:FF"));
        assertEquals(0xFFAAl, NetUtils.mac2Long("00:00:00:00:FF:AA"));
        assertEquals(0x11FFAAl, NetUtils.mac2Long("00:00:00:11:FF:AA"));
        assertEquals(0x12345678l, NetUtils.mac2Long("00:00:12:34:56:78"));
        assertEquals(0x123456789Al, NetUtils.mac2Long("00:12:34:56:78:9A"));
        assertEquals(0x123456789ABCl, NetUtils.mac2Long("12:34:56:78:9A:BC"));
    }

    @Test
    public void testLong2Mac() {
        assertEquals("00:00:00:00:00:00", NetUtils.long2Mac(0l));
        assertEquals("00:00:00:00:00:01", NetUtils.long2Mac(1l));
        assertEquals("00:00:00:00:00:ff", NetUtils.long2Mac(0xFFl));
        assertEquals("00:00:00:00:ff:aa", NetUtils.long2Mac(0xFFAAl));
        assertEquals("00:00:00:11:ff:aa", NetUtils.long2Mac(0x11FFAAl));
        assertEquals("00:00:12:34:56:78", NetUtils.long2Mac(0x12345678l));
        assertEquals("00:12:34:56:78:9a", NetUtils.long2Mac(0x123456789Al));
        assertEquals("12:34:56:78:9a:bc", NetUtils.long2Mac(0x123456789ABCl));
    }

    @Test
    public void testIp2Long() {
        assertEquals(0x7f000001l, NetUtils.ip2Long("127.0.0.1"));
        assertEquals(0xc0a80001l, NetUtils.ip2Long("192.168.0.1"));
        assertEquals(0x08080808l, NetUtils.ip2Long("8.8.8.8"));
    }

    @Test
    public void testLong2Ip() {
        assertEquals("127.0.0.1", NetUtils.long2Ip(0x7f000001l));
        assertEquals("192.168.0.1", NetUtils.long2Ip(0xc0a80001l));
        assertEquals("8.8.8.8", NetUtils.long2Ip(0x08080808l));
    }

    @Test
    public void test31BitPrefixStart() {
        final String ipAddress = "192.168.0.0";
        final String cidr = "192.168.0.0/31";

        final boolean isInRange = NetUtils.isIpWithtInCidrRange(ipAddress, cidr);

        assertTrue("Check if the subnetUtils.setInclusiveHostCount(true) has been called.", isInRange);
    }

    @Test
    public void test31BitPrefixEnd() {
        final String ipAddress = "192.168.0.1";
        final String cidr = "192.168.0.0/31";

        final boolean isInRange = NetUtils.isIpWithtInCidrRange(ipAddress, cidr);

        assertTrue("Check if the subnetUtils.setInclusiveHostCount(true) has been called.", isInRange);
    }

    @Test
    public void test31BitPrefixFail() {
        final String ipAddress = "192.168.0.2";
        final String cidr = "192.168.0.0/31";

        final boolean isInRange = NetUtils.isIpWithtInCidrRange(ipAddress, cidr);

        assertFalse("Out of the range. Why did it return true?", isInRange);
    }

    @Test
    public void test31BitPrefixIpRangesOverlapd() {
        final String gw = "192.168.0.0";
        String ip1;
        String ip2;

        for (int i = 1, j = 2; i <= 254; i++, j++) {
            ip1 = "192.168.0." + i;
            ip2 = "192.168.0." + j;

            final boolean doesOverlap = NetUtils.ipRangesOverlap(ip1, ip2, gw, gw);
            assertFalse("It should overlap, but it's a 31-bit ip", doesOverlap);
        }
    }

    @Test
    public void test31BitPrefixIpRangesOverlapdFail() {
        String gw;
        String ip1;
        String ip2;

        for (int i = 10, j = 12; i <= 254; i++, j++) {
            gw = "192.168.0." + i;
            ip1 = "192.168.0." + i;
            ip2 = "192.168.0." + j;

            final boolean doesOverlap = NetUtils.ipRangesOverlap(ip1, ip2, gw, gw);
            assertTrue("It overlaps!", doesOverlap);
        }
    }

    @Test
    public void testIs31PrefixCidrFail() {
        final String cidr = "10.10.0.0/32";
        final boolean is31PrefixCidr = NetUtils.is31PrefixCidr(cidr);

        assertFalse("It should fail! 32 bit prefix.", is31PrefixCidr);
    }

    @Test
    public void testIs31PrefixCidr() {
        final String cidr = "10.10.0.0/31";
        final boolean is31PrefixCidr = NetUtils.is31PrefixCidr(cidr);

        assertTrue("It should pass! 31 bit prefix.", is31PrefixCidr);
    }

    @Test
    public void testGetCidrNetMask() {
        final String cidr = "10.10.0.0/16";
        final String netmask = NetUtils.getCidrNetmask("10.10.10.10/16");
        assertTrue(cidr + " does not generate valid netmask " + netmask, NetUtils.isValidIp4Netmask(netmask));
    }

    @Test
    public void testGetCidrHostAddress() {
        final String cidr = "10.10.0.1/24";
        final String address = NetUtils.getCidrHostAddress(cidr);
        assertTrue(cidr + " does not generate valid gateway address.", NetUtils.isValidIp4(address));
    }

    @Test
    public void testGetCidrHostAddressNetworkAddress() {
        final String cidr = "10.10.0.0/24";
        final String address = NetUtils.getCidrHostAddress(cidr);
        assertFalse(address + " is a not the network address of CIDR:" + cidr, NetUtils.isValidIp4(address));
    }

    @Test
    public void testGetCidrHostAddressBroadcastAddress() {
        final String cidr = "10.10.0.255/24";
        final String address = NetUtils.getCidrHostAddress(cidr);
        assertFalse(address + " is a not the broadcast address of CIDR:" + cidr, NetUtils.isValidIp4(address));
    }

    @Test
    public void testGetCidrHostAddressIPv6() {
        final String cidr = "2a00:16:a::1/64";
        final String address = NetUtils.getCidrHostAddress6(cidr);
        assertTrue(cidr + " does not generate valid gateway address.", NetUtils.isValidIp6(address));
    }

    @Test
    public void testGetCidrHostAddressNetworkAddressIPv6() {
        final String cidr = "2a00:16:a::/64";
        final String address = NetUtils.getCidrHostAddress6(cidr);
        assertFalse(address + " is a not the network address of CIDR:" + cidr, NetUtils.isValidIp6(address));
    }

    @Test
    public void testGetCidrHostAddressBroadcastAddressIPv6() {
        final String cidr = "2a00:16:a::ffff:ffff:ffff:ffff/64";
        final String address = NetUtils.getCidrHostAddress6(cidr);
        assertFalse(address + " is a not the broadcast address of CIDR:" + cidr, NetUtils.isValidIp6(address));
    }

    @Test
    public void testGetCidrSubNet() {
        final String cidr = "10.10.0.0/16";
        final String subnet = NetUtils.getCidrSubNet("10.10.10.10/16");
        assertTrue(cidr + " does not contain " + subnet, NetUtils.isIpWithtInCidrRange(subnet, cidr));
    }

    @Test
    public void testGetCidrSubNetWithWidth() {
        final String cidr = "10.10.0.0/16";
        final String subnet = NetUtils.getCidrSubNet("10.10.10.10", 16);
        assertTrue(cidr + " does not contain " + subnet, NetUtils.isIpWithtInCidrRange(subnet, cidr));
    }

    @Test
    public void testIsValidCidrSize() {
        final String cidrsize = "16";
        final long netbits = NetUtils.getCidrSizeFromString(cidrsize);
        assertTrue(" does not compute " + cidrsize, netbits == 16);
    }

    @Test(expected = CloudRuntimeException.class)
    public void testIsInvalidCidrSize() {
        final String cidrsize = "33";
        final long netbits = NetUtils.getCidrSizeFromString(cidrsize);
        assertTrue(" does not compute " + cidrsize, netbits == 16);
    }

    @Test(expected = CloudRuntimeException.class)
    public void testIsInvalidCidrString() {
        final String cidrsize = "ggg";
        final long netbits = NetUtils.getCidrSizeFromString(cidrsize);
        assertTrue(" does not compute " + cidrsize, netbits == 16);
    }

    @Test
    public void testCidrToLongArray() {
        final String cidr = "10.192.10.10/10";
        final Long[] netbits = NetUtils.cidrToLong(cidr);
        assertEquals("unexpected cidrsize " + netbits[1], 10l, netbits[1].longValue());
        assertEquals("(un)expected <" + 0x0ac00000L + "> netaddress " + netbits[0].longValue(), netbits[0].longValue(), 0x0ac00000l);
    }

    @Test
    public void testNetmaskFromCidr() {
        long mask = NetUtils.netMaskFromCidr(1l);
        assertEquals("mask not right: " + mask, 0x80000000, mask);
        mask = NetUtils.netMaskFromCidr(32l);
        assertEquals("mask not right: " + mask, 0xffffffff, mask);
    }

    @Test
    public void testIsCidrsNotEmptyWithNullCidrs() {
        assertEquals(false, NetUtils.areCidrsNotEmpty(null, null));
    }

    @Test
    public void testIsCidrsNotEmptyWithEmptyCidrs() {
        assertEquals(false, NetUtils.areCidrsNotEmpty("", "  "));
    }

    @Test
    public void testIsCidrsNotEmpty() {
        assertEquals(true, NetUtils.areCidrsNotEmpty("10.10.0.0/16", "10.1.2.3/16"));
    }

    @Test
    public void testIsNetowrkASubsetOrSupersetOfNetworkBWithEmptyValues() {
        assertEquals(SupersetOrSubset.errorInCidrFormat, NetUtils.isNetworkASubsetOrSupersetOfNetworkB("", null));
    }

    @Test
    public void testIsNetworkAWithinNetworkBWithEmptyValues() {
        assertEquals(false, NetUtils.isNetworkAWithinNetworkB("", null));
    }

    @Test
    public void testIsNetworkAWithinNetworkB() {
        assertTrue(NetUtils.isNetworkAWithinNetworkB("192.168.30.0/24", "192.168.30.0/23"));
        assertTrue(NetUtils.isNetworkAWithinNetworkB("192.168.30.0/24", "192.168.30.0/22"));
        assertFalse(NetUtils.isNetworkAWithinNetworkB("192.168.30.0/23", "192.168.30.0/24"));
        assertFalse(NetUtils.isNetworkAWithinNetworkB("192.168.30.0/22", "192.168.30.0/24"));
        assertTrue(NetUtils.isNetworkAWithinNetworkB("192.168.28.0/24", "192.168.28.0/23"));
        assertTrue(NetUtils.isNetworkAWithinNetworkB("192.168.28.0/24", "192.168.28.0/22"));
        assertFalse(NetUtils.isNetworkAWithinNetworkB("192.168.28.0/23", "192.168.28.0/24"));
        assertFalse(NetUtils.isNetworkAWithinNetworkB("192.168.28.0/22", "192.168.28.0/24"));
        assertTrue(NetUtils.isNetworkAWithinNetworkB("192.168.30.0/24", "192.168.28.0/22"));
    }

    @Test
    public void testIsNetworksOverlapWithEmptyValues() {
        assertEquals(false, NetUtils.isNetworksOverlap("", null));
    }

    @Test
    public void testisNetworkorBroadCastIP() {
        //Checking the True conditions
        assertTrue(NetUtils.isNetworkorBroadcastIP("192.168.0.0", "255.255.255.0"));
        assertTrue(NetUtils.isNetworkorBroadcastIP("192.168.0.255", "255.255.255.0"));
        assertTrue(NetUtils.isNetworkorBroadcastIP("192.168.0.127", "255.255.255.128"));
        assertTrue(NetUtils.isNetworkorBroadcastIP("192.168.0.63", "255.255.255.192"));

        //Checking the False conditions
        assertFalse(NetUtils.isNetworkorBroadcastIP("192.168.0.1", "255.255.255.0"));
        assertFalse(NetUtils.isNetworkorBroadcastIP("192.168.0.127", "255.255.255.0"));
        assertFalse(NetUtils.isNetworkorBroadcastIP("192.168.0.126", "255.255.255.128"));
        assertFalse(NetUtils.isNetworkorBroadcastIP("192.168.0.62", "255.255.255.192"));

        assertTrue(NetUtils.isNetworkorBroadcastIP("192.168.0.63", "255.255.255.192"));
        assertFalse(NetUtils.isNetworkorBroadcastIP("192.168.0.63", "255.255.255.128"));
    }

    @Test
    public void testGetAllIpsFromRange() {
        List<String> ips = NetUtils.getAllIpsFromRange("10.0.0.1-10.0.0.3");
        Assert.assertEquals(ips.get(0), "10.0.0.1");
        Assert.assertEquals(ips.get(1), "10.0.0.2");
        Assert.assertEquals(ips.get(2), "10.0.0.3");
        assertTrue(ips.size() == 3);

        ips = NetUtils.getAllIpsFromRange("10.0.1.254-10.0.2.2");
        assertTrue(ips.size() == 5);
    }

    @Test
    public void testGetAllIpsFromRangeList() {
        List<String> ips = NetUtils.getAllIpsFromRangeList("10.0.0.1-10.0.0.3,10.0.0.7");
        Assert.assertEquals(ips.get(0), "10.0.0.1");
        Assert.assertEquals(ips.get(1), "10.0.0.2");
        Assert.assertEquals(ips.get(2), "10.0.0.3");
        Assert.assertEquals(ips.get(3), "10.0.0.7");
        assertTrue(ips.size() == 4);

        ips = NetUtils.getAllIpsFromRangeList(null);
        assertTrue(ips.isEmpty());
    }

    @Test
    public void testValidIpRangeList() {

        assertFalse(NetUtils.validIpRangeList(""));
        assertFalse(NetUtils.validIpRangeList(null));

        // Test (specifically/single) address
        assertTrue(NetUtils.validIpRangeList("1.2.3.4"));
        assertFalse(NetUtils.validIpRangeList("256.2.3.4"));

        // Test range addresses
        assertTrue(NetUtils.validIpRangeList("1.2.3.4-2.3.4.5"));
        assertFalse(NetUtils.validIpRangeList("1.2.3.4-"));

        //Test collection addresses
        assertTrue(NetUtils.validIpRangeList("1.1.1.1,2.2.2.2-3.3.3.3,4.4.4.4"));
    }

    @Test
    public void testIsIpRangeListInCidr() {
        assertTrue(NetUtils.isIpRangeListInCidr("10.0.0.1,10.0.0.3-10.0.0.6", "10.0.0.0/29"));
        assertFalse(NetUtils.isIpRangeListInCidr("10.0.0.1-10.0.0.8", "10.0.0.0/29"));
    }

    @Test
    public void testListIp2LongList() {
        List<String> stringList = new ArrayList<>();
        stringList.add("10.0.0.1");
        stringList.add("10.0.0.2");
        stringList.add("10.0.0.3");

        SortedSet<Long> longList = NetUtils.listIp2LongList(stringList);

        assertTrue(stringList.size() == longList.size());
        assertTrue(longList.contains(167772161L));
        assertTrue(longList.contains(167772162L));
        assertTrue(longList.contains(167772163L));
    }
    @Test
    public void testIsIpInCidrList() throws UnknownHostException {
        String[] cidrs = "0.0.0.0/0,::/0".split(",");
        System.out.println(NetUtils.isIpInCidrList(InetAddress.getByName("192.168.1.1"), cidrs));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("192.168.1.1"), cidrs));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("172.16.8.9"), cidrs));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("127.0.0.1"), cidrs));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("2001:db8:100::1"), cidrs));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("::1"), cidrs));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("2a01:4f8:130:2192::2"), cidrs));

        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("127.0.0.1"), "127.0.0.1/8".split(",")));
        assertFalse(NetUtils.isIpInCidrList(InetAddress.getByName("192.168.1.1"), "127.0.0.1/8".split(",")));

        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("127.0.0.1"), "127.0.0.1/8,::1/128".split(",")));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("::1"), "127.0.0.1/8,::1/128".split(",")));

        assertFalse(NetUtils.isIpInCidrList(InetAddress.getByName("192.168.29.47"), "127.0.0.1/8,::1/128".split(",")));
        assertFalse(NetUtils.isIpInCidrList(InetAddress.getByName("2001:db8:1938:3ff1::1"), "127.0.0.1/8,::1/128".split(",")));

        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("2a01:4f8:130:2192::2"), "::/0,127.0.0.1".split(",")));
        assertTrue(NetUtils.isIpInCidrList(InetAddress.getByName("2001:db8:200:300::1"), "2001:db8:200::/48,127.0.0.1".split(",")));
        assertFalse(NetUtils.isIpInCidrList(InetAddress.getByName("2001:db8:200:300::1"), "2001:db8:300::/64,127.0.0.1".split(",")));
        assertFalse(NetUtils.isIpInCidrList(InetAddress.getByName("2a01:4f8:130:2192::2"), "2001:db8::/64,127.0.0.1".split(",")));
    }

    @Test
    public void testIsSiteLocalAddress() {
        assertTrue(NetUtils.isSiteLocalAddress("192.168.0.1"));
        assertTrue(NetUtils.isSiteLocalAddress("10.0.0.1"));
        assertTrue(NetUtils.isSiteLocalAddress("172.16.0.1"));
        assertTrue(NetUtils.isSiteLocalAddress("192.168.254.56"));
        assertTrue(NetUtils.isSiteLocalAddress("10.254.254.254"));
        assertFalse(NetUtils.isSiteLocalAddress("8.8.8.8"));
        assertFalse(NetUtils.isSiteLocalAddress("8.8.4.4"));
        assertFalse(NetUtils.isSiteLocalAddress(""));
        assertFalse(NetUtils.isSiteLocalAddress(null));
    }

    @Test
    public void testStaticVariables() {
        assertEquals(80, NetUtils.HTTP_PORT);
        assertEquals(443, NetUtils.HTTPS_PORT);
        assertEquals(500, NetUtils.VPN_PORT);
        assertEquals(4500, NetUtils.VPN_NATT_PORT);
        assertEquals(1701, NetUtils.VPN_L2TP_PORT);
        assertEquals(8081, NetUtils.HAPROXY_STATS_PORT);

        assertEquals("udp", NetUtils.UDP_PROTO);
        assertEquals("tcp", NetUtils.TCP_PROTO);
        assertEquals("any", NetUtils.ANY_PROTO);
        assertEquals("icmp", NetUtils.ICMP_PROTO);
        assertEquals("http", NetUtils.HTTP_PROTO);
        assertEquals("ssl", NetUtils.SSL_PROTO);

        assertEquals("0.0.0.0/0", NetUtils.ALL_IP4_CIDRS);
        assertEquals("::/0", NetUtils.ALL_IP6_CIDRS);
    }

    @Test
    public void testIsValidPort() {
        assertTrue(NetUtils.isValidPort(80));
        assertTrue(NetUtils.isValidPort("80"));
        assertTrue(NetUtils.isValidPort(443));
        assertTrue(NetUtils.isValidPort("443"));
        assertTrue(NetUtils.isValidPort(0));
        assertTrue(NetUtils.isValidPort(65535));
        assertFalse(NetUtils.isValidPort(-1));
        assertFalse(NetUtils.isValidPort(65536));
    }
}
