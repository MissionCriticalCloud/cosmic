package org.apache.cloudstack.affinity.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.affinity.AffinityGroupDomainMapVO;

import java.util.List;

public interface AffinityGroupDomainMapDao extends GenericDao<AffinityGroupDomainMapVO, Long> {

    AffinityGroupDomainMapVO findByAffinityGroup(long affinityGroupId);

    List<AffinityGroupDomainMapVO> listByDomain(Object... domainId);
}
