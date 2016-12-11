package com.cloud.affinity.dao;

import com.cloud.affinity.AffinityGroupDomainMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AffinityGroupDomainMapDao extends GenericDao<AffinityGroupDomainMapVO, Long> {

    AffinityGroupDomainMapVO findByAffinityGroup(long affinityGroupId);

    List<AffinityGroupDomainMapVO> listByDomain(Object... domainId);
}
