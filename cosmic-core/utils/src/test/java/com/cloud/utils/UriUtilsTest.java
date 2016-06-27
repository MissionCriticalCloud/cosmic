//

//

package com.cloud.utils;

import junit.framework.Assert;
import org.junit.Test;

public class UriUtilsTest {
    @Test
    public void encodeURIComponent() {
        Assert.assertEquals("http://localhost",
                UriUtils.encodeURIComponent("http://localhost"));
        Assert.assertEquals("http://localhost/",
                UriUtils.encodeURIComponent("http://localhost/"));
        Assert.assertEquals("http://localhost/foo/bar",
                UriUtils.encodeURIComponent("http://localhost/foo/bar"));
    }

    @Test
    public void getUpdateUri() {
        // no password param, no request for encryption
        Assert.assertEquals("http://localhost/foo/bar?param=true", UriUtils
                .getUpdateUri("http://localhost/foo/bar?param=true", false));
        // there is password param but still no request for encryption, should
        // be unchanged
        Assert.assertEquals("http://localhost/foo/bar?password=1234", UriUtils
                .getUpdateUri("http://localhost/foo/bar?password=1234", false));
        // if there is password param and encryption is requested then it may or
        // may not be changed depending on how the EncrytionUtils is setup, but
        // at least it needs to start with the same url
        Assert.assertTrue(UriUtils.getUpdateUri(
                "http://localhost/foo/bar?password=1234", true).startsWith(
                "http://localhost/foo/bar"));

        //just to see if it is still ok with multiple parameters
        Assert.assertEquals("http://localhost/foo/bar?param1=true&param2=12345", UriUtils
                .getUpdateUri("http://localhost/foo/bar?param1=true&param2=12345", false));

        //XXX: Interesting cases not covered:
        // * port is ignored and left out from the return value
    }
}
