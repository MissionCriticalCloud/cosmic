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
package org.apache.cloudstack.framework.config.impl;

import com.cloud.dao.EntityManager;
import junit.framework.TestCase;
import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.ScopedConfigStorage;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConfigDepotAdminTest extends TestCase {
    private final static ConfigKey<Integer> DynamicIntCK = new ConfigKey<>(Integer.class, "dynIntKey", "Advance", "10", "Test Key", true);
    private final static ConfigKey<Integer> StaticIntCK = new ConfigKey<>(Integer.class, "statIntKey", "Advance", "10", "Test Key", false);

    @Mock
    Configurable _configurable;

    @Mock
    ConfigDepot _configDepot;

    ConfigDepotImpl _depotAdmin;

    @Mock
    EntityManager _entityMgr;

    @Mock
    ConfigurationDao _configDao;

    @Mock
    ScopedConfigStorage _scopedStorage;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        _depotAdmin = new ConfigDepotImpl();
        _depotAdmin._configDao = _configDao;
        _depotAdmin._configurables = new ArrayList<>();
        _depotAdmin._configurables.add(_configurable);
        _depotAdmin._scopedStorages = new ArrayList<>();
        _depotAdmin._scopedStorages.add(_scopedStorage);
    }

    @Test
    public void testAutoPopulation() {
        final ConfigurationVO dynamicIntCV = new ConfigurationVO("UnitTestComponent", DynamicIntCK);
        dynamicIntCV.setValue("100");
        final ConfigurationVO staticIntCV = new ConfigurationVO("UnitTestComponent", StaticIntCK);
        dynamicIntCV.setValue("200");

        when(_configurable.getConfigComponentName()).thenReturn("UnitTestComponent");
        when(_configurable.getConfigKeys()).thenReturn(new ConfigKey<?>[]{DynamicIntCK, StaticIntCK});
        when(_configDao.findById(StaticIntCK.key())).thenReturn(null);
        when(_configDao.findById(DynamicIntCK.key())).thenReturn(dynamicIntCV);
        when(_configDao.persist(any(ConfigurationVO.class))).thenReturn(dynamicIntCV);

        _depotAdmin.populateConfigurations();

        // This is once because DynamicIntCK is returned.
        verify(_configDao, times(1)).persist(any(ConfigurationVO.class));

        when(_configDao.findById(DynamicIntCK.key())).thenReturn(dynamicIntCV);
        _depotAdmin._configured.clear();
        _depotAdmin.populateConfigurations();
        // This is two because DynamicIntCK also returns null.
        verify(_configDao, times(2)).persist(any(ConfigurationVO.class));
    }
}
