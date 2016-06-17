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

public class Cluster extends OvmObject {

  public Cluster(Connection connection) {
    setClient(connection);
  }

  public Boolean leaveCluster(String poolfsUuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("leave_cluster", poolfsUuid);
  }

  public Boolean configureServerForCluster(String poolfsUuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("configure_server_for_cluster", poolfsUuid);
  }

  public Boolean deconfigureServerForCluster(String poolfsUuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("deconfigure_server_for_cluster", poolfsUuid);
  }

  public Boolean joinCLuster(String poolfsUuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("join_cluster", poolfsUuid);
  }

  /* TODO: Intepret existing clusters... */
  public Boolean discoverCluster() throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("discover_cluster");
  }

  public Boolean updateClusterConfiguration(String clusterConf) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("update_clusterConfiguration", clusterConf);
  }

  public Boolean destroyCluster(String poolfsUuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("destroy_cluster", poolfsUuid);
  }

  public Boolean isClusterOnline() throws Ovm3ResourceException {
    final Object x = callWrapper("is_cluster_online");
    return Boolean.valueOf(x.toString());
  }

  public Boolean createCluster(String poolfsUuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("create_cluster", poolfsUuid);
  }
}
