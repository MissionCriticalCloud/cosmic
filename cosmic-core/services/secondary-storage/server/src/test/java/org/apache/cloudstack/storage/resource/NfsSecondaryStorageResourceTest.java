package org.apache.cloudstack.storage.resource;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class NfsSecondaryStorageResourceTest extends TestCase {
    private static final Logger s_logger = Logger.getLogger(NfsSecondaryStorageResourceTest.class.getName());
    private static Map<String, Object> testParams;
    NfsSecondaryStorageResource resource;

    @Before
    @Override
    public void setUp() throws ConfigurationException {
        s_logger.setLevel(Level.ALL);
        resource = new NfsSecondaryStorageResource();
        resource.setInSystemVM(true);
        testParams = PropertiesUtil.toMap(loadProperties());
        resource.configureStorageLayerClass(testParams);
        final Object testLocalRoot = testParams.get("testLocalRoot");
        if (testLocalRoot != null) {
            resource.setParentPath((String) testLocalRoot);
        }
    }

    public static Properties loadProperties() throws ConfigurationException {
        final Properties properties = new Properties();
        final File file = PropertiesUtil.findConfigFile("agent.properties");
        if (file == null) {
            throw new ConfigurationException("Unable to find agent.properties.");
        }
        s_logger.info("agent.properties found at " + file.getAbsolutePath());
        try (FileInputStream fs = new FileInputStream(file)) {
            properties.load(fs);
        } catch (final FileNotFoundException ex) {
            throw new CloudRuntimeException("Cannot find the file: " + file.getAbsolutePath(), ex);
        } catch (final IOException ex) {
            throw new CloudRuntimeException("IOException in reading " + file.getAbsolutePath(), ex);
        }
        return properties;
    }

    @Test
    public void testMount() throws Exception {
        final String sampleUriStr = "cifs://192.168.1.128/CSHV3?user=administrator&password=1pass%40word1&foo=bar";
        final URI sampleUri = new URI(sampleUriStr);

        s_logger.info("Check HostIp parsing");
        final String hostIpStr = resource.getUriHostIp(sampleUri);
        Assert.assertEquals("Expected host IP " + sampleUri.getHost() + " and actual host IP " + hostIpStr + " differ.", sampleUri.getHost(), hostIpStr);

        s_logger.info("Check option parsing");
        final String expected = "user=administrator,password=1pass@word1,foo=bar,";
        final String actualOpts = resource.parseCifsMountOptions(sampleUri);
        Assert.assertEquals("Options should be " + expected + " and not " + actualOpts, expected, actualOpts);

        // attempt a configured mount
        final Map<String, Object> params = PropertiesUtil.toMap(loadProperties());
        final String sampleMount = (String) params.get("testCifsMount");
        if (!sampleMount.isEmpty()) {
            s_logger.info("functional test, mount " + sampleMount);
            final URI realMntUri = new URI(sampleMount);
            final String mntSubDir = resource.mountUri(realMntUri);
            s_logger.info("functional test, umount " + mntSubDir);
            resource.umount(resource.getMountingRoot() + mntSubDir, realMntUri);
        } else {
            s_logger.info("no entry for testCifsMount in " + "./conf/agent.properties - skip functional test");
        }
    }
}
