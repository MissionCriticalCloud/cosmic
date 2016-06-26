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
package com.cloud.user;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.ConfigKey.Scope;
import org.apache.cloudstack.framework.config.ScopedConfigStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDetailsDaoImpl extends GenericDaoBase<AccountDetailVO, Long> implements AccountDetailsDao, ScopedConfigStorage {
    protected final SearchBuilder<AccountDetailVO> accountSearch;

    protected AccountDetailsDaoImpl() {
        accountSearch = createSearchBuilder();
        accountSearch.and("accountId", accountSearch.entity().getAccountId(), Op.EQ);
        accountSearch.done();
    }

    @Override
    public Scope getScope() {
        return ConfigKey.Scope.Account;
    }

    @Override
    public String getConfigValue(final long id, final ConfigKey<?> key) {
        final AccountDetailVO vo = findDetail(id, key.key());
        return vo == null ? null : vo.getValue();
    }

    @Override
    public Map<String, String> findDetails(final long accountId) {
        final QueryBuilder<AccountDetailVO> sc = QueryBuilder.create(AccountDetailVO.class);
        sc.and(sc.entity().getAccountId(), Op.EQ, accountId);
        final List<AccountDetailVO> results = sc.list();
        final Map<String, String> details = new HashMap<>(results.size());
        for (final AccountDetailVO r : results) {
            details.put(r.getName(), r.getValue());
        }
        return details;
    }

    @Override
    public void persist(final long accountId, final Map<String, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<AccountDetailVO> sc = accountSearch.create();
        sc.setParameters("accountId", accountId);
        expunge(sc);
        for (final Map.Entry<String, String> detail : details.entrySet()) {
            final AccountDetailVO vo = new AccountDetailVO(accountId, detail.getKey(), detail.getValue());
            persist(vo);
        }
        txn.commit();
    }

    @Override
    public AccountDetailVO findDetail(final long accountId, final String name) {
        final QueryBuilder<AccountDetailVO> sc = QueryBuilder.create(AccountDetailVO.class);
        sc.and(sc.entity().getAccountId(), Op.EQ, accountId);
        sc.and(sc.entity().getName(), Op.EQ, name);
        return sc.find();
    }

    @Override
    public void deleteDetails(final long accountId) {
        final SearchCriteria<AccountDetailVO> sc = accountSearch.create();
        sc.setParameters("accountId", accountId);
        final List<AccountDetailVO> results = search(sc, null);
        for (final AccountDetailVO result : results) {
            remove(result.getId());
        }
    }

    @Override
    public void update(final long accountId, final Map<String, String> details) {
        final Map<String, String> oldDetails = findDetails(accountId);
        oldDetails.putAll(details);
        persist(accountId, oldDetails);
    }
}
