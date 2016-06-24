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

import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.util.List;

public class RevertToVMSnapshotCommand extends VMSnapshotBaseCommand {

    private boolean reloadVm = false;
    private final String vmUuid;

    public RevertToVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType, final
    boolean reloadVm) {
        this(vmName, vmUuid, snapshot, volumeTOs, guestOSType);
        setReloadVm(reloadVm);
    }

    public RevertToVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType) {
        super(vmName, snapshot, volumeTOs, guestOSType);
        this.vmUuid = vmUuid;
    }

    public boolean isReloadVm() {
        return reloadVm;
    }

    public void setReloadVm(final boolean reloadVm) {
        this.reloadVm = reloadVm;
    }

    public String getVmUuid() {
        return vmUuid;
    }
}
