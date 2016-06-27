package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface SecurityGroupDao extends GenericDao<SecurityGroupVO, Long> {
    List<SecurityGroupVO> listByAccountId(long accountId);

    boolean isNameInUse(Long accountId, Long domainId, String name);

    SecurityGroupVO findByAccountAndName(Long accountId, String name);

    List<SecurityGroupVO> findByAccountAndNames(Long accountId, String... names);

    int removeByAccountId(long accountId);
}
