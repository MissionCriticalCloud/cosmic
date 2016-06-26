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
package com.cloud.network;

/**
 * PhysicalNetworkNames provides the labels to identify per traffic type
 * the physical networks available to the host .
 */
public class PhysicalNetworkSetupInfo {

    // physical network ID as seen by Mgmt server
    Long physicalNetworkId;
    String privateNetworkName;
    String publicNetworkName;
    String guestNetworkName;
    String storageNetworkName;
    String mgmtVlan;

    public PhysicalNetworkSetupInfo() {
    }

    public String getPrivateNetworkName() {
        return privateNetworkName;
    }

    public void setPrivateNetworkName(final String privateNetworkName) {
        this.privateNetworkName = privateNetworkName;
    }

    public String getPublicNetworkName() {
        return publicNetworkName;
    }

    public void setPublicNetworkName(final String publicNetworkName) {
        this.publicNetworkName = publicNetworkName;
    }

    public String getGuestNetworkName() {
        return guestNetworkName;
    }

    public void setGuestNetworkName(final String guestNetworkName) {
        this.guestNetworkName = guestNetworkName;
    }

    public String getStorageNetworkName() {
        return storageNetworkName;
    }

    public void setStorageNetworkName(final String storageNetworkName) {
        this.storageNetworkName = storageNetworkName;
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getMgmtVlan() {
        return mgmtVlan;
    }

    public void setMgmtVlan(final String mgmtVlan) {
        this.mgmtVlan = mgmtVlan;
    }
}
