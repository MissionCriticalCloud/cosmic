// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.agent.dao.impl;

import com.cloud.agent.dao.StorageComponent;
import com.cloud.utils.PropertiesUtil;

import javax.ejb.Local;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Properties to implement storage.
 *
 * @config {@table || Param Name | Description | Values | Default || || path |
 * path to the properties _file | String | db/db.properties || * }
 **/
@Local(value = {StorageComponent.class})
public class PropertiesStorage implements StorageComponent {
    private static final Logger s_logger = LoggerFactory.getLogger(PropertiesStorage.class);
    Properties _properties = new Properties();
    File _file;
    String _name;

    @Override
    public synchronized String get(final String key) {
        return _properties.getProperty(key);
    }

    @Override
    public synchronized void persist(final String key, final String value) {
        _properties.setProperty(key, value);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(_file);
            _properties.store(output, _name);
            output.flush();
            output.close();
        } catch (final IOException e) {
            s_logger.error("Uh-oh: ", e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    @Override
    public synchronized String getName() {
        return _name;
    }

    @Override
    public synchronized boolean configure(final String name, final Map<String, Object> params) {
        _name = name;
        String path = (String) params.get("path");
        if (path == null) {
            path = "agent.properties";
        }

        File file = PropertiesUtil.findConfigFile(path);
        if (file == null) {
            file = new File(path);
            try {
                if (!file.createNewFile()) {
                    s_logger.error("Unable to create _file: " + file.getAbsolutePath());
                    return false;
                }
            } catch (final IOException e) {
                s_logger.error("Unable to create _file: " + file.getAbsolutePath(), e);
                return false;
            }
        }
        try {
            PropertiesUtil.loadFromFile(_properties, file);
            _file = file;
        } catch (final FileNotFoundException e) {
            s_logger.error("How did we get here? ", e);
            return false;
        } catch (final IOException e) {
            s_logger.error("IOException: ", e);
            return false;
        }
        return true;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRunLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
        // TODO Auto-generated method stub
    }
}
