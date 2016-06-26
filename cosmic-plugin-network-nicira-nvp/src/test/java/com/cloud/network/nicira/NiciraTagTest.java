//

//

package com.cloud.network.nicira;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NiciraTagTest {
    @Test
    public void testCreateTag() {
        final NiciraNvpTag tag = new NiciraNvpTag("scope", "tag");
        assertEquals("scope part set", "scope", tag.getScope());
        assertEquals("tag part set", "tag", tag.getTag());
    }

    @Test
    public void testCreateLongTag() {
        final NiciraNvpTag tag = new NiciraNvpTag("scope", "verylongtagthatshouldattheminimumexceedthefortycharacterlenght");
        assertEquals("scope part set", "scope", tag.getScope());
        assertEquals("tag part set", "verylongtagthatshouldattheminimumexceedt", tag.getTag());
    }

    @Test
    public void testSetTag() {
        final NiciraNvpTag tag = new NiciraNvpTag();
        tag.setScope("scope");
        tag.setTag("tag");
        assertEquals("scope part set", "scope", tag.getScope());
        assertEquals("tag part set", "tag", tag.getTag());
    }

    @Test
    public void testSetLongTag() {
        final NiciraNvpTag tag = new NiciraNvpTag();
        tag.setScope("scope");
        tag.setTag("verylongtagthatshouldattheminimumexceedthefortycharacterlenght");
        assertEquals("scope part set", "scope", tag.getScope());
        assertEquals("tag part set", "verylongtagthatshouldattheminimumexceedt", tag.getTag());
    }
}
