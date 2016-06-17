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

package com.cloud.hypervisor.ovm3.objects;

import org.junit.Test;

public class PoolOCFS2Test {
  ConnectionTest con = new ConnectionTest();
  PoolOcfS2 poolfs = new PoolOcfS2(con);
  XmlTestResultTest results = new XmlTestResultTest();

  private final String TYPE = "nfs";
  private final String UUID = "f12842eb-f5ed-3fe7-8da1-eb0e17f5ede8";
  private final String BOGUSUUID = "deadbeef-dead-beef-dead-beef0000002d";
  private final String TARGET = "cs-mgmt:/volumes/cs-data/primary/ovm/VirtualMachines";
  private final String BASE = "f12842ebf5ed3fe78da1eb0e17f5ede8";
  private final String MANAGER = "d1a749d4295041fb99854f52ea4dea97";
  private final String CLUSTER = MANAGER.substring(0, 15);
  private final String VERSION = "3.0";
  private final String POOLUUID = "f12842eb-f5ed-3fe7-8da1-eb0e17f5ede8";
  private final String EMPTY = results.escapeOrNot("<?xml version=\"1.0\" ?>"
      + "<Discover_Pool_Filesystem_Result>"
      + "</Discover_Pool_Filesystem_Result>");
  private final String DISCOVERPOOLFS = results.escapeOrNot("<?xml version=\"1.0\" ?>"
      + "<Discover_Pool_Filesystem_Result>" + "<Pool_Filesystem>"
      + "<Pool_Filesystem_Type>"
      + TYPE
      + "</Pool_Filesystem_Type>"
      + "<Pool_Filesystem_Target>"
      + TARGET
      + "</Pool_Filesystem_Target>"
      + "<Pool_Filesystem_Uuid>"
      + UUID
      + "</Pool_Filesystem_Uuid>"
      + "<Pool_Filesystem_Nfsbase_Uuid>"
      + BASE
      + "</Pool_Filesystem_Nfsbase_Uuid>"
      + "<Pool_Filesystem_Manager_Uuid>"
      + MANAGER
      + "</Pool_Filesystem_Manager_Uuid>"
      + "<Pool_Filesystem_Version>"
      + VERSION
      + "</Pool_Filesystem_Version>"
      + "<Pool_Filesystem_Pool_Uuid>"
      + POOLUUID
      + "</Pool_Filesystem_Pool_Uuid>"
      + "</Pool_Filesystem>"
      + "</Discover_Pool_Filesystem_Result>");

  @Test
  public void testDiscoverPoolFS() throws Ovm3ResourceException {
    con.setResult(results.simpleResponseWrapWrapper(EMPTY));
    results.basicBooleanTest(poolfs.hasAPoolFs(), false);
    results.basicBooleanTest(poolfs.hasPoolFs(BOGUSUUID), false);
    con.setResult(results.simpleResponseWrapWrapper(DISCOVERPOOLFS));
    poolfs.discoverPoolFs();
    results.basicStringTest(poolfs.getPoolFsId(), UUID);
    results.basicStringTest(poolfs.getPoolFsManagerUuid(), MANAGER);
    results.basicStringTest(poolfs.getPoolFsNfsBaseId(), BASE);
    results.basicStringTest(poolfs.getPoolFsTarget(), TARGET);
    results.basicStringTest(poolfs.getPoolFsUuid(), UUID);
    results.basicStringTest(poolfs.getPoolFsVersion(), VERSION);
    results.basicStringTest(poolfs.getPoolPoolFsId(), POOLUUID);
    results.basicStringTest(poolfs.getPoolFsType(), TYPE);
    results.basicBooleanTest(poolfs.hasAPoolFs());
    results.basicBooleanTest(poolfs.hasPoolFs(UUID));
  }

  @Test
  public void testCreatePoolFS() throws Ovm3ResourceException {
    con.setResult(results.getNil());
    poolfs.createPoolFs(TYPE, TARGET, CLUSTER, UUID, BASE, MANAGER);

    con.setResult(results.simpleResponseWrapWrapper(DISCOVERPOOLFS));
    results.basicBooleanTest(poolfs.hasPoolFs(UUID));
    poolfs.createPoolFs(TYPE, TARGET, CLUSTER, UUID, BASE, MANAGER);
  }

  @Test
  public void testDestroyPoolFS() throws Ovm3ResourceException {
    con.setResult(results.getNil());
    poolfs.destroyPoolFs(TYPE, TARGET, UUID, BASE);

    con.setResult(results.simpleResponseWrapWrapper(DISCOVERPOOLFS));
    results.basicBooleanTest(poolfs.hasPoolFs(UUID));
    poolfs.createPoolFs(TYPE, TARGET, CLUSTER, UUID, BASE, MANAGER);
  }

  @Test(expected = Ovm3ResourceException.class)
  public void testCreatePoolFSError() throws Ovm3ResourceException {
    con.setResult(results.simpleResponseWrapWrapper(DISCOVERPOOLFS));
    poolfs.createPoolFs(TYPE, TARGET, CLUSTER, BOGUSUUID, BASE, MANAGER);
  }
}
