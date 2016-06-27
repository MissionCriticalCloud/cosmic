package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface SslCertDao extends GenericDao<SslCertVO, Long> {
    List<SslCertVO> listByAccountId(Long id);
}
