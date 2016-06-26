package org.apache.cloudstack.affinity.dao;

import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.affinity.AffinityGroupVMMapVO;

import java.util.List;

public interface AffinityGroupVMMapDao extends GenericDao<AffinityGroupVMMapVO, Long> {

    List<AffinityGroupVMMapVO> listByInstanceId(long instanceId);

    Pair<List<AffinityGroupVMMapVO>, Integer> listByInstanceId(long instanceId, Filter filter);

    List<AffinityGroupVMMapVO> listByAffinityGroup(long affinityGroupId);

    List<Long> listVmIdsByAffinityGroup(long affinityGroupId);

    AffinityGroupVMMapVO findByVmIdGroupId(long instanceId, long affinityGroupId);

    long countAffinityGroupsForVm(long instanceId);

    int deleteVM(long instanceId);

    List<AffinityGroupVMMapVO> findByVmIdType(long instanceId, String type);

    void updateMap(Long vmId, List<Long> affinityGroupIds);

    List<Long> listAffinityGroupIdsByVmId(long instanceId);
}
