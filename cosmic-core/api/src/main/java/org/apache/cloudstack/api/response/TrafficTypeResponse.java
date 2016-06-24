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
package org.apache.cloudstack.api.response;

import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = PhysicalNetworkTrafficType.class)
public class TrafficTypeResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "id of the network provider")
    private String id;

    @SerializedName(ApiConstants.TRAFFIC_TYPE)
    @Param(description = "the trafficType to be added to the physical network")
    private String trafficType;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network this belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.XENSERVER_NETWORK_LABEL)
    @Param(description = "The network name label of the physical device dedicated to this traffic on a XenServer host")
    private String xenNetworkLabel;

    @SerializedName(ApiConstants.KVM_NETWORK_LABEL)
    @Param(description = "The network name label of the physical device dedicated to this traffic on a KVM host")
    private String kvmNetworkLabel;

    @SerializedName(ApiConstants.OVM3_NETWORK_LABEL)
    @Param(description = "The network name of the physical device dedicated to this traffic on an OVM3 host")
    private String ovm3NetworkLabel;

    @Override
    public String getObjectId() {
        return id;
    }

    public void setPhysicalNetworkId(String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getphysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getId() {
        return id;
    }

    public void setId(String uuid) {
        id = uuid;
    }

    public String getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(String trafficType) {
        this.trafficType = trafficType;
    }

    public String getXenLabel() {
        return xenNetworkLabel;
    }

    public void setXenLabel(String xenLabel) {
        xenNetworkLabel = xenLabel;
    }

    public String getKvmLabel() {
        return kvmNetworkLabel;
    }

    public void setKvmLabel(String kvmLabel) {
        kvmNetworkLabel = kvmLabel;
    }

    public String getOvm3Label() {
        return ovm3NetworkLabel;
    }

    public void setOvm3Label(String ovm3Label) {
        ovm3NetworkLabel = ovm3Label;
    }
}
