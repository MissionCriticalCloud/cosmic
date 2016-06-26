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
package com.cloud.offering;

import com.cloud.storage.Storage.ProvisioningType;
import org.apache.cloudstack.acl.InfrastructureEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

/**
 * Represents a disk offering that specifies what the end user needs in
 * the disk offering.
 */
public interface DiskOffering extends InfrastructureEntity, Identity, InternalIdentity {
    State getState();

    String getUniqueName();

    boolean getUseLocalStorage();

    Long getDomainId();

    String getName();

    boolean getSystemUse();

    String getDisplayText();

    public ProvisioningType getProvisioningType();

    public String getTags();

    public String[] getTagsArray();

    Date getCreated();

    boolean isCustomized();

    long getDiskSize();

    void setDiskSize(long diskSize);

    void setCustomizedIops(Boolean customizedIops);

    Boolean isCustomizedIops();

    Long getMinIops();

    void setMinIops(Long minIops);

    Long getMaxIops();

    void setMaxIops(Long maxIops);

    boolean isRecreatable();

    Long getBytesReadRate();

    void setBytesReadRate(Long bytesReadRate);

    Long getBytesWriteRate();

    void setBytesWriteRate(Long bytesWriteRate);

    Long getIopsReadRate();

    void setIopsReadRate(Long iopsReadRate);

    Long getIopsWriteRate();

    void setIopsWriteRate(Long iopsWriteRate);

    Integer getHypervisorSnapshotReserve();

    void setHypervisorSnapshotReserve(Integer hypervisorSnapshotReserve);

    DiskCacheMode getCacheMode();

    void setCacheMode(DiskCacheMode cacheMode);

    Type getType();

    enum State {
        Inactive, Active,
    }

    public enum Type {
        Disk, Service
    }

    public enum DiskCacheMode {
        NONE("none"), WRITEBACK("writeback"), WRITETHROUGH("writethrough");

        private final String _diskCacheMode;

        DiskCacheMode(final String cacheMode) {
            _diskCacheMode = cacheMode;
        }

        @Override
        public String toString() {
            return _diskCacheMode;
        }
    }
}
