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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

/*
 * synonym to the pool python lib in the ovs-agent
 */
public class Pool extends OvmObject {

  private final List<String> validRoles = new ArrayList<String>() {
    {
      add("xen");
      add("utility");
    }

    private static final long serialVersionUID = 1L;
  };
  private List<String> poolHosts = new ArrayList<String>();
  private final List<String> poolRoles = new ArrayList<String>();
  private String poolMasterVip;
  private String poolAlias;
  private String poolId = null;

  public Pool(Connection connection) {
    setClient(connection);
  }

  public String getPoolMasterVip() {
    return poolMasterVip;
  }

  public String getPoolAlias() {
    return poolAlias;
  }

  public String getPoolId() {
    return poolId;
  }

  public List<String> getValidRoles() {
    return validRoles;
  }

  public Boolean isInPool(String id) throws Ovm3ResourceException {
    if (poolId == null) {
      discoverServerPool();
    }
    if (poolId == null) {
      return false;
    }
    if (isInAPool() && poolId.equals(id)) {
      return true;
    }
    return false;
  }

  public Boolean isInAPool() throws Ovm3ResourceException {
    if (poolId == null) {
      discoverServerPool();
    }
    if (poolId == null) {
      return false;
    }
    return true;
  }

  private Boolean createServerPool(String alias, String id, String vip,
      int num, String name, String host, List<String> roles) throws Ovm3ResourceException {
    final String role = StringUtils.join(roles, ",");
    if (!isInAPool()) {
      final Object x = callWrapper("create_server_pool", alias, id, vip, num, name,
          host, role);
      if (x == null) {
        return true;
      }
      return false;
    } else if (isInPool(id)) {
      return true;
    } else {
      throw new Ovm3ResourceException("Unable to add host is already in  a pool with id : " + poolId);
    }
  }

  public Boolean createServerPool(String alias, String id, String vip,
      int num, String name, String ip) throws Ovm3ResourceException {
    return createServerPool(alias, id, vip, num, name, ip,
        getValidRoles());
  }

  /*
   * public Boolean updatePoolVirtualIp(String ip) throws Ovm3ResourceException { Object x =
   * callWrapper("update_pool_virtual_ip", ip); if (x == null) { poolMasterVip = ip; return true; } return false; }
   */

  public Boolean leaveServerPool(String uuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("leave_server_pool", uuid);
  }

  public Boolean takeOwnership(String uuid, String apiurl) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("take_ownership", uuid, apiurl);
  }

  public Boolean takeOwnership33x(final String uuid,
      final String eventUrl,
      final String statUrl,
      final String managerCert,
      final String signedCert) throws Ovm3ResourceException {
    final Map<String, String> mgrConfig = new HashMap<String, String>() {
      {
        put("manager_uuid", uuid);
        put("manager_event_url", eventUrl);
        put("manager_statistic_url", statUrl);
        put("manager_certificate", managerCert);
        put("signed_server_certificate", signedCert);
      }

      private static final long serialVersionUID = 1L;
    };
    final Boolean rc = nullIsTrueCallWrapper("take_ownership", mgrConfig);
    /* because it restarts when it's done.... 2000? -sigh- */
    try {
      Thread.sleep(2000);
    } catch (final InterruptedException e) {
      throw new Ovm3ResourceException(e.getMessage());
    }
    return rc;
  }

  /*
   * destroy_server_pool, <class 'agent.api.serverpool.ServerPool'> argument: self - default: None argument: pool_uuid -
   * default: None
   */
  public Boolean destroyServerPool(String uuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("destroy_server_pool", uuid);
  }

  /*
   * release_ownership, <class 'agent.api.serverpool.ServerPool'> argument: self - default: None argument: manager_uuid
   * - default: None
   */
  public Boolean releaseOwnership(String uuid) throws Ovm3ResourceException {
    return nullIsTrueCallWrapper("release_ownership", uuid);
  }

  /* server.discover_pool_filesystem */
  /*
   * discover_server_pool, <class 'agent.api.serverpool.ServerPool'> argument: self - default: None
   */
  public Boolean discoverServerPool() throws Ovm3ResourceException {
    final Object x = callWrapper("discover_server_pool");
    if (x == null) {
      return false;
    }

    final Document xmlDocument = prepParse((String) x);
    final String path = "//Discover_Server_Pool_Result/Server_Pool";
    poolId = xmlToString(path + "/Unique_Id", xmlDocument);
    poolAlias = xmlToString(path + "/Pool_Alias", xmlDocument);
    poolMasterVip = xmlToString(path + "/Master_Virtual_Ip",
        xmlDocument);
    poolHosts.addAll(xmlToList(path + "//Registered_IP", xmlDocument));
    if (poolId == null) {
      return false;
    }
    return true;
  }

  private Boolean setServerRoles() throws Ovm3ResourceException {
    final String roles = StringUtils.join(poolRoles.toArray(), ",");
    return nullIsTrueCallWrapper("update_server_roles", roles);
  }

  /* do some sanity check on the valid poolroles */
  public Boolean setServerRoles(List<String> roles) throws Ovm3ResourceException {
    poolRoles.addAll(roles);
    return setServerRoles();
  }

  private Boolean joinServerPool(String alias, String id, String vip, int num,
      String name, String host, List<String> roles) throws Ovm3ResourceException {
    final String role = StringUtils.join(roles.toArray(), ",");
    if (!isInAPool()) {
      final Object x = callWrapper("join_server_pool", alias, id, vip, num, name,
          host, role);
      if (x == null) {
        return true;
      }
      return false;
    } else if (isInPool(id)) {
      return true;
    } else {
      throw new Ovm3ResourceException("Unable to add host is already in  a pool with id : " + poolId);
    }
  }

  public Boolean joinServerPool(String alias, String id, String vip, int num,
      String name, String host) throws Ovm3ResourceException {
    return joinServerPool(alias, id, vip, num, name, host, getValidRoles());
  }

  private Boolean setPoolMemberList() throws Ovm3ResourceException {
    // should throw exception if no poolHosts set
    return nullIsTrueCallWrapper("set_pool_member_ip_list", poolHosts);
  }

  public Boolean setPoolMemberList(List<String> hosts) throws Ovm3ResourceException {
    poolHosts = new ArrayList<String>();
    poolHosts.addAll(hosts);
    return setPoolMemberList();
  }

  public List<String> getPoolMemberList() throws Ovm3ResourceException {
    if (poolId == null) {
      discoverServerPool();
    }
    return poolHosts;
  }

  public Boolean addPoolMember(String host) throws Ovm3ResourceException {
    getPoolMemberList();
    poolHosts.add(host);
    return setPoolMemberList();
  }

  public Boolean removePoolMember(String host) throws Ovm3ResourceException {
    getPoolMemberList();
    poolHosts.remove(host);
    return setPoolMemberList();
  }
}
