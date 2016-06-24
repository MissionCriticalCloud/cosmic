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
package com.cloud.event.dao;

import com.cloud.event.UsageEventDetailsVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageEventDetailsDaoImpl extends GenericDaoBase<UsageEventDetailsVO, Long> implements UsageEventDetailsDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageEventDetailsDaoImpl.class.getName());

    protected final SearchBuilder<UsageEventDetailsVO> EventDetailsSearch;
    protected final SearchBuilder<UsageEventDetailsVO> DetailSearch;

    public UsageEventDetailsDaoImpl() {

        EventDetailsSearch = createSearchBuilder();
        EventDetailsSearch.and("eventId", EventDetailsSearch.entity().getUsageEventId(), SearchCriteria.Op.EQ);
        EventDetailsSearch.done();

        DetailSearch = createSearchBuilder();
        DetailSearch.and("eventId", DetailSearch.entity().getUsageEventId(), SearchCriteria.Op.EQ);
        DetailSearch.and("key", DetailSearch.entity().getKey(), SearchCriteria.Op.EQ);
        DetailSearch.done();
    }

    @Override
    public void persist(final long eventId, final Map<String, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<UsageEventDetailsVO> sc = EventDetailsSearch.create();
        sc.setParameters("eventId", eventId);
        expunge(sc);

        for (final Map.Entry<String, String> detail : details.entrySet()) {
            final UsageEventDetailsVO vo = new UsageEventDetailsVO(eventId, detail.getKey(), detail.getValue());
            persist(vo);
        }
        txn.commit();
    }

    @Override
    public UsageEventDetailsVO findDetail(final long eventId, final String key) {
        final SearchCriteria<UsageEventDetailsVO> sc = DetailSearch.create();
        sc.setParameters("eventId", eventId);
        sc.setParameters("key", key);

        return findOneBy(sc);
    }

    @Override
    public void deleteDetails(final long eventId) {
        final SearchCriteria<UsageEventDetailsVO> sc = EventDetailsSearch.create();
        sc.setParameters("eventId", eventId);

        final List<UsageEventDetailsVO> results = search(sc, null);
        for (final UsageEventDetailsVO result : results) {
            remove(result.getId());
        }
    }
}
