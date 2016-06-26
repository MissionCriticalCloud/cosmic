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

package com.cloud.agent.api;

public class ModifyVmNicConfigCommand extends Command {
    String vmName;
    String vlan;
    String macAddress;
    int index;
    boolean enable;
    String switchLableName;

    protected ModifyVmNicConfigCommand() {
    }

    public ModifyVmNicConfigCommand(final String vmName, final String vlan, final String macAddress) {
        this.vmName = vmName;
        this.vlan = vlan;
        this.macAddress = macAddress;
    }

    public ModifyVmNicConfigCommand(final String vmName, final String vlan, final int position) {
        this.vmName = vmName;
        this.vlan = vlan;
        this.index = position;
    }

    public ModifyVmNicConfigCommand(final String vmName, final String vlan, final int position, final boolean enable) {
        this.vmName = vmName;
        this.vlan = vlan;
        this.index = position;
        this.enable = enable;
    }

    public String getVmName() {
        return vmName;
    }

    public String getSwitchLableName() {
        return switchLableName;
    }

    public void setSwitchLableName(final String switchlableName) {
        this.switchLableName = switchlableName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
