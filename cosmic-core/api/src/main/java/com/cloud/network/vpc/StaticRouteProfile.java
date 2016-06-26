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
package com.cloud.network.vpc;

public class StaticRouteProfile implements StaticRoute {
    String ipAddress;
    private long id;
    private String uuid;
    private String targetCidr;
    private long accountId;
    private long domainId;
    private StaticRoute.State state;
    private long vpcId;

    public StaticRouteProfile(StaticRoute staticRoute) {
        id = staticRoute.getId();
        uuid = staticRoute.getUuid();
        targetCidr = staticRoute.getCidr();
        accountId = staticRoute.getAccountId();
        domainId = staticRoute.getDomainId();
        state = staticRoute.getState();
        vpcId = staticRoute.getVpcId();
        ipAddress = staticRoute.getGwIpAddress();
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public String getCidr() {
        return targetCidr;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    public String getGwIpAddress() {
        return ipAddress;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getIp4Address() {
        return ipAddress;
    }

    @Override
    public Class<?> getEntityType() {
        return StaticRoute.class;
    }
}
