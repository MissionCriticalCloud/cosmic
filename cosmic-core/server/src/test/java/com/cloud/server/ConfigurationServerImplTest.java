package com.cloud.server;

import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.utils.db.TransactionLegacy;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServerImplTest {

    final static String TEST = "the quick brown fox jumped over the lazy dog";
    @Spy
    ConfigurationServerImpl linuxImpl;
    @Mock
    private ConfigurationDao _configDao;
    @Mock
    private ManagementService _mgrService;
    @InjectMocks
    private ConfigurationServerImpl configurationServer;

    @Test(expected = IOException.class)
    public void testGetBase64KeystoreNoSuchFile() throws IOException {
        ConfigurationServerImpl.getBase64Keystore("notexisting" + System.currentTimeMillis());
    }

    @Test(expected = IOException.class)
    public void testGetBase64KeystoreTooBigFile() throws IOException {
        final File temp = File.createTempFile("keystore", "");
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append("way too long...\n");
        }
        FileUtils.writeStringToFile(temp, builder.toString());
        try {
            ConfigurationServerImpl.getBase64Keystore(temp.getPath());
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testGetBase64Keystore() throws IOException {
        final File temp = File.createTempFile("keystore", "");
        try {
            FileUtils.writeStringToFile(temp, Base64.encodeBase64String(TEST.getBytes()));
            final String keystore = ConfigurationServerImpl.getBase64Keystore(temp.getPath());
            // let's decode it to make sure it makes sense
            Base64.decodeBase64(keystore);
        } finally {
            temp.delete();
        }
    }
    
    @Test
    public void testUpdateSystemvmPassword() {
        //setup
        final String realusername = System.getProperty("user.name");
        System.setProperty("user.name", "cloud");
        Mockito.when(_configDao.getValue("system.vm.random.password")).thenReturn(String.valueOf(true));
        TransactionLegacy.open("cloud");
        Mockito.when(_mgrService.generateRandomPassword()).thenReturn("randomPassword");

        //call the method to test
        configurationServer.updateSystemvmPassword();

        //verify that generateRandomPassword() is called
        Mockito.verify(_mgrService, Mockito.times(1)).generateRandomPassword();
        //teardown
        System.setProperty("user.name", realusername);
    }
}
