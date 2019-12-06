package com.cloud.affinity.dao;

import com.cloud.affinity.AffinityGroupVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AffinityGroupDao extends GenericDao<AffinityGroupVO, Long> {
    List<AffinityGroupVO> listByAccountId(long accountId);

    boolean isNameInUse(Long accountId, Long domainId, String name);

    AffinityGroupVO findByAccountAndName(Long accountId, String name);

    List<AffinityGroupVO> findByAccountAndNames(Long accountId, String... names);

    int removeByAccountId(long accountId);

    AffinityGroupVO findDomainLevelGroupByName(Long domainId, String affinityGroupName);

    AffinityGroupVO findByAccountAndType(Long accountId, String string);

    AffinityGroupVO findByAccountAndTypeAndRouterFlag(Long accountId, String string, Long routerFlag);

    AffinityGroupVO findDomainLevelGroupByTypeAndRouterFlag(Long accountId, String string, Long routerFlag);

    AffinityGroupVO findDomainLevelGroupByType(Long domainId, String string);
}
