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
package com.cloud.network.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkExternalLoadBalancerDaoImpl extends GenericDaoBase<NetworkExternalLoadBalancerVO, Long> implements NetworkExternalLoadBalancerDao {

    final SearchBuilder<NetworkExternalLoadBalancerVO> networkIdSearch;
    final SearchBuilder<NetworkExternalLoadBalancerVO> deviceIdSearch;

    protected NetworkExternalLoadBalancerDaoImpl() {
        super();
        networkIdSearch = createSearchBuilder();
        networkIdSearch.and("networkId", networkIdSearch.entity().getNetworkId(), Op.EQ);
        networkIdSearch.done();

        deviceIdSearch = createSearchBuilder();
        deviceIdSearch.and("externalLBDeviceId", deviceIdSearch.entity().getExternalLBDeviceId(), Op.EQ);
        deviceIdSearch.done();
    }

    @Override
    public NetworkExternalLoadBalancerVO findByNetworkId(final long networkId) {
        final SearchCriteria<NetworkExternalLoadBalancerVO> sc = networkIdSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public List<NetworkExternalLoadBalancerVO> listByLoadBalancerDeviceId(final long lbDeviceId) {
        final SearchCriteria<NetworkExternalLoadBalancerVO> sc = deviceIdSearch.create();
        sc.setParameters("externalLBDeviceId", lbDeviceId);
        return search(sc, null);
    }
}
