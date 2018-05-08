package com.cloud.ldap;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DistinguishedNameParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void testParseLeafeNameEmptyString() {
        final String distinguishedName = "";
        DistinguishedNameParser.parseLeafName(distinguishedName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLeafeNameSingleMalformedKeyValue() {
        final String distinguishedName = "someMalformedKeyValue";
        DistinguishedNameParser.parseLeafName(distinguishedName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLeafeNameNonSingleMalformedKeyValue() {
        final String distinguishedName = "someMalformedKeyValue,key=value";
        DistinguishedNameParser.parseLeafName(distinguishedName);
    }

    @Test
    public void testParseLeafeNameSingleKeyValue() {
        final String distinguishedName = "key=value";
        final String value = DistinguishedNameParser.parseLeafName(distinguishedName);

        assertThat(value, equalTo("value"));
    }

    @Test
    public void testParseLeafeNameMultipleKeyValue() {
        final String distinguishedName = "key1=leaf,key2=nonleaf";
        final String value = DistinguishedNameParser.parseLeafName(distinguishedName);

        assertThat(value, equalTo("leaf"));
    }
}
