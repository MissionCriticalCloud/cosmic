//
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
//

package com.cloud.network.nicira;

import java.util.List;

public class LogicalSwitchPort extends BaseNiciraNamedEntity {
    private final String type = "LogicalSwitchPortConfig";
    private Integer portno;
    private boolean adminStatusEnabled;
    private String queueUuid;
    private List<String> securityProfiles;
    private List<String> mirrorTargets;

    public LogicalSwitchPort() {
        super();
    }

    public LogicalSwitchPort(final String displayName, final List<NiciraNvpTag> tags, final boolean adminStatusEnabled) {
        super();
        this.displayName = displayName;
        this.tags = tags;
        this.adminStatusEnabled = adminStatusEnabled;
    }

    public Integer getPortno() {
        return portno;
    }

    public void setPortno(final Integer portno) {
        this.portno = portno;
    }

    public boolean isAdminStatusEnabled() {
        return adminStatusEnabled;
    }

    public void setAdminStatusEnabled(final boolean adminStatusEnabled) {
        this.adminStatusEnabled = adminStatusEnabled;
    }

    public String getQueueUuid() {
        return queueUuid;
    }

    public void setQueueUuid(final String queueUuid) {
        this.queueUuid = queueUuid;
    }

    public List<String> getSecurityProfiles() {
        return securityProfiles;
    }

    public void setSecurityProfiles(final List<String> securityProfiles) {
        this.securityProfiles = securityProfiles;
    }

    public List<String> getMirrorTargets() {
        return mirrorTargets;
    }

    public void setMirrorTargets(final List<String> mirrorTargets) {
        this.mirrorTargets = mirrorTargets;
    }
}
