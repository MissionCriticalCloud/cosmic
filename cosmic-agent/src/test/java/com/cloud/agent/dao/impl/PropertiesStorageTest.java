package com.cloud.agent.dao.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class PropertiesStorageTest {
    @Test
    public void configureWithNotExistingFile() {
        final String fileName = "target/notyetexistingfile" + System.currentTimeMillis();
        final File file = new File(fileName);

        final PropertiesStorage storage = new PropertiesStorage();
        final HashMap<String, Object> params = new HashMap<>();
        params.put("path", fileName);
        Assert.assertTrue(storage.configure("test", params));
        Assert.assertTrue(file.exists());
        storage.persist("foo", "bar");
        Assert.assertEquals("bar", storage.get("foo"));

        storage.stop();
        file.delete();
    }

    @Test
    public void configureWithExistingFile() throws IOException {
        final String fileName = "target/existingfile" + System.currentTimeMillis();
        final File file = new File(fileName);

        FileUtils.writeStringToFile(file, "a=b\n\n");

        final PropertiesStorage storage = new PropertiesStorage();
        final HashMap<String, Object> params = new HashMap<>();
        params.put("path", fileName);
        Assert.assertTrue(storage.configure("test", params));
        Assert.assertEquals("b", storage.get("a"));
        Assert.assertTrue(file.exists());
        storage.persist("foo", "bar");
        Assert.assertEquals("bar", storage.get("foo"));

        storage.stop();
        file.delete();
    }
}
