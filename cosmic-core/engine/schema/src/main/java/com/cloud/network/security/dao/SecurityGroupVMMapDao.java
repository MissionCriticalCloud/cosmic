package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupVMMapVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.VirtualMachine.State;

import java.util.List;

public interface SecurityGroupVMMapDao extends GenericDao<SecurityGroupVMMapVO, Long> {
    List<SecurityGroupVMMapVO> listByIpAndInstanceId(String ipAddress, long instanceId);

    List<SecurityGroupVMMapVO> listByInstanceId(long instanceId);

    Pair<List<SecurityGroupVMMapVO>, Integer> listByInstanceId(long instanceId, Filter filter);

    List<SecurityGroupVMMapVO> listByIp(String ipAddress);

    List<SecurityGroupVMMapVO> listBySecurityGroup(long securityGroupId);

    List<SecurityGroupVMMapVO> listBySecurityGroup(long securityGroupId, State... vmStates);

    int deleteVM(long instanceid);

    List<Long> listVmIdsBySecurityGroup(long securityGroupId);

    SecurityGroupVMMapVO findByVmIdGroupId(long instanceId, long securityGroupId);

    long countSGForVm(long instanceId);
}
