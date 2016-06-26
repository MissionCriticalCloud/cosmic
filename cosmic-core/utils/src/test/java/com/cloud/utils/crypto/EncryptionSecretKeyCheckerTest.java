//

//

package com.cloud.utils.crypto;

import com.cloud.utils.crypt.EncryptionSecretKeyChecker;
import com.cloud.utils.db.DbProperties;
import com.cloud.utils.exception.CloudRuntimeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class EncryptionSecretKeyCheckerTest {

    private final EncryptionSecretKeyChecker checker = new EncryptionSecretKeyChecker();

    @Test(expected = CloudRuntimeException.class)
    public void testKeyFileDoesNotExists() throws IOException, URISyntaxException {
        Assert.assertNotNull(checker);
        final Properties properties = DbProperties.getDbProperties();
        properties.setProperty("db.cloud.encryption.type", "file");
        checker.check(properties);
    }
}
