package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface LoadBalancerCertMapDao extends GenericDao<LoadBalancerCertMapVO, Long> {
    List<LoadBalancerCertMapVO> listByCertId(Long certId);

    List<LoadBalancerCertMapVO> listByAccountId(Long accountId);

    LoadBalancerCertMapVO findByLbRuleId(Long id);
}
