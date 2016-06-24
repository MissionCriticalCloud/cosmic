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
package org.apache.cloudstack.storage.allocator;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StoragePool;
import com.cloud.user.Account;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZoneWideStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(ZoneWideStoragePoolAllocator.class);
    @Inject
    PrimaryDataStoreDao _storagePoolDao;
    @Inject
    DataStoreManager dataStoreMgr;

    @Override
    protected List<StoragePool> select(DiskProfile dskCh, VirtualMachineProfile vmProfile, DeploymentPlan plan, ExcludeList avoid, int returnUpTo) {
        s_logger.debug("ZoneWideStoragePoolAllocator to find storage pool");

        if (dskCh.useLocalStorage()) {
            return null;
        }

        if (s_logger.isTraceEnabled()) {
            // Log the pools details that are ignored because they are in disabled state
            List<StoragePoolVO> disabledPools = _storagePoolDao.findDisabledPoolsByScope(plan.getDataCenterId(), null, null, ScopeType.ZONE);
            if (disabledPools != null && !disabledPools.isEmpty()) {
                for (StoragePoolVO pool : disabledPools) {
                    s_logger.trace("Ignoring pool " + pool + " as it is in disabled state.");
                }
            }
        }

        List<StoragePool> suitablePools = new ArrayList<StoragePool>();

        List<StoragePoolVO> storagePools = _storagePoolDao.findZoneWideStoragePoolsByTags(plan.getDataCenterId(), dskCh.getTags());
        if (storagePools == null) {
            storagePools = new ArrayList<StoragePoolVO>();
        }

        List<StoragePoolVO> anyHypervisorStoragePools = new ArrayList<StoragePoolVO>();
        for (StoragePoolVO storagePool : storagePools) {
            if (HypervisorType.Any.equals(storagePool.getHypervisor())) {
                anyHypervisorStoragePools.add(storagePool);
            }
        }

        List<StoragePoolVO> storagePoolsByHypervisor = _storagePoolDao.findZoneWideStoragePoolsByHypervisor(plan.getDataCenterId(), dskCh.getHypervisorType());
        storagePools.retainAll(storagePoolsByHypervisor);
        storagePools.addAll(anyHypervisorStoragePools);

        // add remaining pools in zone, that did not match tags, to avoid set
        List<StoragePoolVO> allPools = _storagePoolDao.findZoneWideStoragePoolsByTags(plan.getDataCenterId(), null);
        allPools.removeAll(storagePools);
        for (StoragePoolVO pool : allPools) {
            avoid.addPool(pool.getId());
        }

        for (StoragePoolVO storage : storagePools) {
            if (suitablePools.size() == returnUpTo) {
                break;
            }
            StoragePool storagePool = (StoragePool) this.dataStoreMgr.getPrimaryDataStore(storage.getId());
            if (filter(avoid, storagePool, dskCh, plan)) {
                suitablePools.add(storagePool);
            } else {
                avoid.addPool(storagePool.getId());
            }
        }
        return suitablePools;
    }

    @Override
    protected List<StoragePool> reorderPoolsByNumberOfVolumes(DeploymentPlan plan, List<StoragePool> pools, Account account) {
        if (account == null) {
            return pools;
        }
        long dcId = plan.getDataCenterId();

        List<Long> poolIdsByVolCount = _volumeDao.listZoneWidePoolIdsByVolumeCount(dcId, account.getAccountId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of pools in ascending order of number of volumes for account id: " + account.getAccountId() + " is: " + poolIdsByVolCount);
        }

        // now filter the given list of Pools by this ordered list
        Map<Long, StoragePool> poolMap = new HashMap<Long, StoragePool>();
        for (StoragePool pool : pools) {
            poolMap.put(pool.getId(), pool);
        }
        List<Long> matchingPoolIds = new ArrayList<Long>(poolMap.keySet());

        poolIdsByVolCount.retainAll(matchingPoolIds);

        List<StoragePool> reorderedPools = new ArrayList<StoragePool>();
        for (Long id : poolIdsByVolCount) {
            reorderedPools.add(poolMap.get(id));
        }

        return reorderedPools;
    }
}
