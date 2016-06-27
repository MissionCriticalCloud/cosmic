package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

public class SslCertDaoImpl extends GenericDaoBase<SslCertVO, Long> implements SslCertDao {

    private final SearchBuilder<SslCertVO> listByAccountId;

    public SslCertDaoImpl() {
        listByAccountId = createSearchBuilder();
        listByAccountId.and("accountId", listByAccountId.entity().getAccountId(), SearchCriteria.Op.EQ);
        listByAccountId.done();
    }

    @Override
    public List<SslCertVO> listByAccountId(final Long accountId) {
        final SearchCriteria<SslCertVO> sc = listByAccountId.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }
}
