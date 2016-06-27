package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.inject.Inject;
import java.util.List;

public class LoadBalancerCertMapDaoImpl extends GenericDaoBase<LoadBalancerCertMapVO, Long> implements LoadBalancerCertMapDao {

    private final SearchBuilder<LoadBalancerCertMapVO> listByCertId;
    private final SearchBuilder<LoadBalancerCertMapVO> findByLbRuleId;

    @Inject
    SslCertDao _sslCertDao;

    public LoadBalancerCertMapDaoImpl() {

        listByCertId = createSearchBuilder();
        listByCertId.and("certificateId", listByCertId.entity().getCertId(), SearchCriteria.Op.EQ);
        listByCertId.done();

        findByLbRuleId = createSearchBuilder();
        findByLbRuleId.and("loadBalancerId", findByLbRuleId.entity().getLbId(), SearchCriteria.Op.EQ);
        findByLbRuleId.done();
    }

    @Override
    public List<LoadBalancerCertMapVO> listByCertId(final Long certId) {
        final SearchCriteria<LoadBalancerCertMapVO> sc = listByCertId.create();
        sc.setParameters("certificateId", certId);
        return listBy(sc);
    }

    @Override
    public List<LoadBalancerCertMapVO> listByAccountId(final Long accountId) {

        final SearchBuilder<LoadBalancerCertMapVO> listByAccountId;
        final SearchBuilder<SslCertVO> certsForAccount;

        listByAccountId = createSearchBuilder();
        certsForAccount = _sslCertDao.createSearchBuilder();
        certsForAccount.and("accountId", certsForAccount.entity().getAccountId(), SearchCriteria.Op.EQ);
        listByAccountId.join("certsForAccount", certsForAccount, certsForAccount.entity().getId(), listByAccountId.entity().getLbId(), JoinBuilder.JoinType.INNER);
        certsForAccount.done();
        listByAccountId.done();

        final SearchCriteria<LoadBalancerCertMapVO> sc = listByAccountId.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public LoadBalancerCertMapVO findByLbRuleId(final Long lbId) {
        final SearchCriteria<LoadBalancerCertMapVO> sc = findByLbRuleId.create();
        sc.setParameters("loadBalancerId", lbId);
        return findOneBy(sc);
    }
}
