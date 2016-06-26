//

//

package com.cloud.utils;

import org.junit.Assert;
import org.junit.Test;

public class TernaryTest {
    @Test
    public void testEquals() {
        Assert.assertEquals(new Ternary<>("a", "b", "c"), new Ternary<>("a", "b", "c"));
        Assert.assertFalse(new Ternary<>("a", "b", "c").equals(new Ternary<>("a", "b", "d")));
        Assert.assertFalse(new Ternary<>("a", "b", "c").equals(""));
        Assert.assertFalse(new Ternary<>("a", "b", "c").equals(null));
        Assert.assertFalse(new Ternary<>("a", "b", "c").equals(new Pair<>("a", "b")));
    }
}
