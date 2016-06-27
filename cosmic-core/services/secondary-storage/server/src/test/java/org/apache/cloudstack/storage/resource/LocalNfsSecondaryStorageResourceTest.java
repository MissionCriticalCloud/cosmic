package org.apache.cloudstack.storage.resource;

import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.SwiftTO;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.to.TemplateObjectTO;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalNfsSecondaryStorageResourceTest extends TestCase {
    private static final Logger s_logger = LoggerFactory.getLogger(LocalNfsSecondaryStorageResourceTest.class.getName());
    private static Map<String, Object> testParams;
    LocalNfsSecondaryStorageResource resource;

    @Before
    @Override
    public void setUp() throws ConfigurationException {
        resource = new LocalNfsSecondaryStorageResource();
        resource.setInSystemVM(true);

        testParams = PropertiesUtil.toMap(loadProperties());
        resource.configureStorageLayerClass(testParams);
        final Object testLocalRoot = testParams.get("testLocalRoot");
        resource.setParentPath("/mnt");

        if (testLocalRoot != null) {
            resource.setParentPath((String) testLocalRoot);
        }

        System.setProperty("paths.script", "/Users/edison/develop/asf-master/script");
        //resource.configure("test", new HashMap<String, Object>());
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
    public void testExecuteRequest() throws Exception {
        final TemplateObjectTO template = Mockito.mock(TemplateObjectTO.class);
        final NfsTO cacheStore = Mockito.mock(NfsTO.class);
        Mockito.when(cacheStore.getUrl()).thenReturn("nfs://nfs2.lab.vmops.com/export/home/edison/");
        final SwiftTO swift = Mockito.mock(SwiftTO.class);
        Mockito.when(swift.getEndPoint()).thenReturn("https://objects.dreamhost.com/auth");
        Mockito.when(swift.getAccount()).thenReturn("cloudstack");
        Mockito.when(swift.getUserName()).thenReturn("images");
        Mockito.when(swift.getKey()).thenReturn("oxvELQaOD1U5_VyosGfA-wpZ7uBWEff-CUBGCM0u");

        Mockito.when(template.getDataStore()).thenReturn(swift);
        Mockito.when(template.getPath()).thenReturn("template/1/1/");
        Mockito.when(template.isRequiresHvm()).thenReturn(true);
        Mockito.when(template.getId()).thenReturn(1L);
        Mockito.when(template.getFormat()).thenReturn(Storage.ImageFormat.VHD);
        Mockito.when(template.getOrigUrl()).thenReturn("http://nfs1.lab.vmops.com/templates/test.bz2");
        Mockito.when(template.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(template.getObjectType()).thenReturn(DataObjectType.TEMPLATE);

        final DownloadCommand cmd = new DownloadCommand(template, 100000L);
        cmd.setCacheStore(cacheStore);
        final DownloadAnswer answer = (DownloadAnswer) resource.executeRequest(cmd);
        Assert.assertTrue(answer.getResult());

        Mockito.when(template.getPath()).thenReturn(answer.getInstallPath());
        Mockito.when(template.getDataStore()).thenReturn(swift);
        //download swift:
        Mockito.when(cacheStore.getRole()).thenReturn(DataStoreRole.ImageCache);
        final TemplateObjectTO destTemplate = Mockito.mock(TemplateObjectTO.class);
        Mockito.when(destTemplate.getPath()).thenReturn("template/1/2");
        Mockito.when(destTemplate.getDataStore()).thenReturn(cacheStore);
        Mockito.when(destTemplate.getObjectType()).thenReturn(DataObjectType.TEMPLATE);
        final CopyCommand cpyCmd = new CopyCommand(template, destTemplate, 10000, true);
        final CopyCmdAnswer copyCmdAnswer = (CopyCmdAnswer) resource.executeRequest(cpyCmd);
        Assert.assertTrue(copyCmdAnswer.getResult());

        //list template
        final ListTemplateCommand listCmd = new ListTemplateCommand(swift);
        final ListTemplateAnswer listAnswer = (ListTemplateAnswer) resource.executeRequest(listCmd);

        Assert.assertTrue(listAnswer.getTemplateInfo().size() > 0);
    }
}
