package com.cloud.utils.imagestore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class ImageStoreUtilTest {

    @Test
    public void testgeneratePostUploadUrl() throws MalformedURLException {
        final String ssvmdomain = "*.realhostip.com";
        final String ipAddress = "10.147.28.14";
        final String uuid = UUID.randomUUID().toString();

        //ssvm domain is not set
        String url = ImageStoreUtil.generatePostUploadUrl(null, ipAddress, uuid);
        assertPostUploadUrl(url, ipAddress, uuid);

        //ssvm domain is set to empty value
        url = ImageStoreUtil.generatePostUploadUrl("", ipAddress, uuid);
        assertPostUploadUrl(url, ipAddress, uuid);

        //ssvm domain is set to a valid value
        url = ImageStoreUtil.generatePostUploadUrl(ssvmdomain, ipAddress, uuid);
        assertPostUploadUrl(url, ipAddress.replace(".", "-") + ssvmdomain.substring(1), uuid);
    }

    private void assertPostUploadUrl(final String urlStr, final String domain, final String uuid) throws MalformedURLException {
        final URL url = new URL(urlStr);
        Assert.assertNotNull(url);
        Assert.assertEquals(url.getHost(), domain);
        Assert.assertEquals(url.getPath(), "/upload/" + uuid);
    }
}
