package com.cloud.storage.template;

import com.cloud.exception.InternalErrorException;
import com.cloud.storage.Storage;
import com.cloud.storage.StorageLayer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VhdProcessorTest {
    VhdProcessor processor;

    @Mock
    StorageLayer mockStorageLayer;

    @Before
    public void setUp() throws Exception {
        processor = Mockito.spy(new VhdProcessor());
        final Map<String, Object> params = new HashMap<>();
        params.put(StorageLayer.InstanceConfigKey, mockStorageLayer);
        processor.configure("VHD Processor", params);
    }

    @Test(expected = InternalErrorException.class)
    public void testProcessWhenVirtualSizeThrowsException() throws Exception {
        final String templatePath = "/tmp";
        final String templateName = "template";

        Mockito.when(mockStorageLayer.exists(Mockito.anyString())).thenReturn(true);
        final File mockFile = Mockito.mock(File.class);

        Mockito.when(mockStorageLayer.getFile(Mockito.anyString())).thenReturn(mockFile);
        Mockito.when(mockStorageLayer.getSize(Mockito.anyString())).thenReturn(1000L);
        Mockito.doThrow(new IOException("virtual size calculation failed")).when(processor).getTemplateVirtualSize((File) Mockito.any());

        processor.process(templatePath, null, templateName);
    }

    @Test
    public void testProcess() throws Exception {
        final String templatePath = "/tmp";
        final String templateName = "template";
        final long virtualSize = 2000;
        final long actualSize = 1000;

        Mockito.when(mockStorageLayer.exists(Mockito.anyString())).thenReturn(true);
        final File mockFile = Mockito.mock(File.class);

        Mockito.when(mockStorageLayer.getFile(Mockito.anyString())).thenReturn(mockFile);
        Mockito.when(mockStorageLayer.getSize(Mockito.anyString())).thenReturn(actualSize);
        Mockito.doReturn(virtualSize).when(processor).getTemplateVirtualSize((File) Mockito.any());

        final Processor.FormatInfo info = processor.process(templatePath, null, templateName);
        Assert.assertEquals(Storage.ImageFormat.VHD, info.format);
        Assert.assertEquals(actualSize, info.size);
        Assert.assertEquals(virtualSize, info.virtualSize);
        Assert.assertEquals(templateName + ".vhd", info.filename);
    }

    @Test
    public void testGetVirtualSizeWhenVirtualSizeThrowsException() throws Exception {
        final long virtualSize = 2000;
        final long actualSize = 1000;
        final File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.length()).thenReturn(actualSize);
        Mockito.doThrow(new IOException("virtual size calculation failed")).when(processor).getTemplateVirtualSize((File) Mockito.any());
        Assert.assertEquals(actualSize, processor.getVirtualSize(mockFile));
        Mockito.verify(mockFile, Mockito.times(1)).length();
    }

    @Test
    public void testGetVirtualSize() throws Exception {
        final long virtualSize = 2000;
        final long actualSize = 1000;
        final File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.length()).thenReturn(actualSize);
        Mockito.doReturn(virtualSize).when(processor).getTemplateVirtualSize((File) Mockito.any());
        Assert.assertEquals(virtualSize, processor.getVirtualSize(mockFile));
        Mockito.verify(mockFile, Mockito.times(0)).length();
    }
}
