/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.NetworkACLItemCidrsDao;
import com.cloud.network.vpc.NetworkACLItemCidrsVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author daan
 */
@Component
public class NetworkACLItemCidrsDaoImpl extends GenericDaoBase<NetworkACLItemCidrsVO, Long> implements NetworkACLItemCidrsDao {
    private static final Logger s_logger = LoggerFactory.getLogger(NetworkACLItemCidrsDaoImpl.class);
    protected final SearchBuilder<NetworkACLItemCidrsVO> cidrsSearch;

    protected NetworkACLItemCidrsDaoImpl() {
        cidrsSearch = createSearchBuilder();
        cidrsSearch.and("networkAclItemId", cidrsSearch.entity().getNetworkACLItemId(), SearchCriteria.Op.EQ);
        cidrsSearch.done();
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkAclItemCidrsDao#persist(long, java.util.List)
     */
    @Override
    public void persist(final long networkACLItemId, final List<String> cidrs) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        for (final String cidr : cidrs) {
            final NetworkACLItemCidrsVO vo = new NetworkACLItemCidrsVO(networkACLItemId, cidr);
            persist(vo);
        }
        txn.commit();
    }

    @Override
    public void updateCidrs(final long networkACLItemId, final List<String> cidrs) {
        final List<String> oldCidrs = getCidrs(networkACLItemId);
        if (!(oldCidrs.size() == cidrs.size() && oldCidrs.equals(cidrs))) {
            final SearchCriteria<NetworkACLItemCidrsVO> sc = cidrsSearch.create();
            sc.setParameters("networkAclItemId", networkACLItemId);
            remove(sc);
            persist(networkACLItemId, cidrs);
        }
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkAclItemCidrsDao#getCidrs(long)
     */
    @Override
    public List<String> getCidrs(final long networkACLItemId) {
        final SearchCriteria<NetworkACLItemCidrsVO> sc = cidrsSearch.create();
        sc.setParameters("networkAclItemId", networkACLItemId);

        final List<NetworkACLItemCidrsVO> results = search(sc, null);
        final List<String> cidrs = new ArrayList<>(results.size());
        for (final NetworkACLItemCidrsVO result : results) {
            cidrs.add(result.getCidr());
        }

        return cidrs;
    }

    @Override
    public List<NetworkACLItemCidrsVO> listByNetworkACLItemId(final long networkACLItemId) {
        final SearchCriteria<NetworkACLItemCidrsVO> sc = cidrsSearch.create();
        sc.setParameters("networkAclItemId", networkACLItemId);

        final List<NetworkACLItemCidrsVO> results = search(sc, null);

        return results;
    }
}
