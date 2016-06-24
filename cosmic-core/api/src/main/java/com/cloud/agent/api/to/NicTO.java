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
package com.cloud.agent.api.to;

import java.util.List;

public class NicTO extends NetworkTO {
    int deviceId;
    Integer networkRateMbps;
    Integer networkRateMulticastMbps;
    boolean defaultNic;
    String nicUuid;
    List<String> nicSecIps;

    public NicTO() {
        super();
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final int deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getNetworkRateMbps() {
        return networkRateMbps;
    }

    public void setNetworkRateMbps(final Integer networkRateMbps) {
        this.networkRateMbps = networkRateMbps;
    }

    public Integer getNetworkRateMulticastMbps() {
        return networkRateMulticastMbps;
    }

    public boolean isDefaultNic() {
        return defaultNic;
    }

    public void setDefaultNic(final boolean defaultNic) {
        this.defaultNic = defaultNic;
    }

    @Override
    public String getUuid() {
        return nicUuid;
    }

    @Override
    public void setUuid(final String uuid) {
        this.nicUuid = uuid;
    }

    @Override
    public String toString() {
        return new StringBuilder("[Nic:").append(type).append("-").append(ip).append("-").append(broadcastUri).append("]").toString();
    }

    public List<String> getNicSecIps() {
        return nicSecIps;
    }

    public void setNicSecIps(final List<String> secIps) {
        this.nicSecIps = secIps;
    }

    public String getNetworkUuid() {
        return super.getUuid();
    }

    public void setNetworkUuid(final String uuid) {
        super.setUuid(uuid);
    }
}
