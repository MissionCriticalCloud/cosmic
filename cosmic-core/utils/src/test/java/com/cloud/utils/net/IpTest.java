//

//

package com.cloud.utils.net;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.junit.Assert;
import org.junit.Test;

public class IpTest {

    @Test
    public void testUltimate() {
        final Ip max = new Ip(2L * Integer.MAX_VALUE + 1);
        assertEquals("Maximal address not created", "255.255.255.255", max.addr());
    }

    @Test
    public void testTurningOfTheCentury() {
        final Ip eve = new Ip(Integer.MAX_VALUE);
        assertEquals("Minimal address not created", "127.255.255.255", eve.addr());
        final Ip dawn = new Ip(Integer.MAX_VALUE + 1L);
        assertEquals("Minimal address not created", "128.0.0.0", dawn.addr());
    }

    @Test
    public void testStart() {
        final Ip min = new Ip(0);
        assertEquals("Minimal address not created", "0.0.0.0", min.addr());
    }

    @Test
    public void testEquals() {
        new EqualsTester()
                .addEqualityGroup(new Ip("0.0.0.1"), new Ip(1L))
                .addEqualityGroup(new Ip("0.0.0.0"), new Ip(0L))
                .testEquals();
    }

    @Test
    public void testIsSameAddressAs() {
        Assert.assertTrue("1 and one should be considdered the same address", new Ip(1L).isSameAddressAs("0.0.0.1"));
        Assert.assertTrue("zero and 0L should be considdered the same address", new Ip("0.0.0.0").isSameAddressAs(0L));
    }
}
