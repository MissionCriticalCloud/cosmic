//

//

package com.cloud.utils.net;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MacAddressTest {

    @Test
    public final void testMacAddress() throws Exception {
        final MacAddress mac = new MacAddress();
        assertEquals(0L, mac.toLong());
    }

    @Test
    public final void testMacAddressLong() throws Exception {
        final MacAddress mac = new MacAddress(1L);
        assertEquals(1L, mac.toLong());
    }

    @Test
    public final void testMacAddressToLong() throws Exception {
        // TODO this test should fail this address is beyond the acceptable range for macaddresses
        final MacAddress mac = new MacAddress(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, mac.toLong());
        System.out.println(mac.toString());
    }

    // TODO    public final void testToLong() throws Exception {
    // TODO    public final void testToByteArray() throws Exception {
    // TODO    public final void testToStringString() throws Exception {
    // TODO    public final void testToString() throws Exception {
    // TODO    public final void testGetMacAddress() throws Exception {
    // TODO    public final void testParse() throws Exception {
    // TODO    public final void testMain() throws Exception {
    // TODO    public final void testParseLong() throws Exception {
    // TODO    public final void testParseInt() throws Exception {
    // TODO    public final void testParseShort() throws Exception {
    // TODO    public final void testParseByte() throws Exception {
}
