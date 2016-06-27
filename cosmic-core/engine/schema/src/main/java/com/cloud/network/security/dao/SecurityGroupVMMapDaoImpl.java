package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupVMMapVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.vm.VirtualMachine.State;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SecurityGroupVMMapDaoImpl extends GenericDaoBase<SecurityGroupVMMapVO, Long> implements SecurityGroupVMMapDao {
    protected GenericSearchBuilder<SecurityGroupVMMapVO, Long> CountSGForVm;
    private final SearchBuilder<SecurityGroupVMMapVO> ListByIpAndVmId;
    private final SearchBuilder<SecurityGroupVMMapVO> ListByVmId;
    private final SearchBuilder<SecurityGroupVMMapVO> ListByVmIdGroupId;
    private final GenericSearchBuilder<SecurityGroupVMMapVO, Long> ListVmIdBySecurityGroup;

    private final SearchBuilder<SecurityGroupVMMapVO> ListByIp;
    private final SearchBuilder<SecurityGroupVMMapVO> ListBySecurityGroup;
    private final SearchBuilder<SecurityGroupVMMapVO> ListBySecurityGroupAndStates;

    protected SecurityGroupVMMapDaoImpl() {
        ListByIpAndVmId = createSearchBuilder();
        ListByIpAndVmId.and("ipAddress", ListByIpAndVmId.entity().getGuestIpAddress(), SearchCriteria.Op.EQ);
        ListByIpAndVmId.and("instanceId", ListByIpAndVmId.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListByIpAndVmId.done();

        ListVmIdBySecurityGroup = createSearchBuilder(Long.class);
        ListVmIdBySecurityGroup.and("securityGroupId", ListVmIdBySecurityGroup.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        ListVmIdBySecurityGroup.selectFields(ListVmIdBySecurityGroup.entity().getInstanceId());
        ListVmIdBySecurityGroup.done();

        ListBySecurityGroup = createSearchBuilder();
        ListBySecurityGroup.and("securityGroupId", ListBySecurityGroup.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        ListBySecurityGroup.done();

        ListByIp = createSearchBuilder();
        ListByIp.and("ipAddress", ListByIp.entity().getGuestIpAddress(), SearchCriteria.Op.EQ);
        ListByIp.done();

        ListByVmId = createSearchBuilder();
        ListByVmId.and("instanceId", ListByVmId.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListByVmId.done();

        ListBySecurityGroupAndStates = createSearchBuilder();
        ListBySecurityGroupAndStates.and("securityGroupId", ListBySecurityGroupAndStates.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        ListBySecurityGroupAndStates.and("states", ListBySecurityGroupAndStates.entity().getVmState(), SearchCriteria.Op.IN);
        ListBySecurityGroupAndStates.done();

        ListByVmIdGroupId = createSearchBuilder();
        ListByVmIdGroupId.and("instanceId", ListByVmIdGroupId.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListByVmIdGroupId.and("securityGroupId", ListByVmIdGroupId.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        ListByVmIdGroupId.done();

        CountSGForVm = createSearchBuilder(Long.class);
        CountSGForVm.select(null, Func.COUNT, null);
        CountSGForVm.and("vmId", CountSGForVm.entity().getInstanceId(), SearchCriteria.Op.EQ);
        CountSGForVm.done();
    }

    @Override
    public List<SecurityGroupVMMapVO> listByIpAndInstanceId(final String ipAddress, final long vmId) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListByIpAndVmId.create();
        sc.setParameters("ipAddress", ipAddress);
        sc.setParameters("instanceId", vmId);
        return listBy(sc);
    }

    @Override
    public List<SecurityGroupVMMapVO> listByInstanceId(final long vmId) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListByVmId.create();
        sc.setParameters("instanceId", vmId);
        return listBy(sc);
    }

    @Override
    public Pair<List<SecurityGroupVMMapVO>, Integer> listByInstanceId(final long instanceId, final Filter filter) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListByVmId.create();
        sc.setParameters("instanceId", instanceId);
        return this.searchAndCount(sc, filter);
    }

    @Override
    public List<SecurityGroupVMMapVO> listByIp(final String ipAddress) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListByIp.create();
        sc.setParameters("ipAddress", ipAddress);
        return listBy(sc);
    }

    @Override
    public List<SecurityGroupVMMapVO> listBySecurityGroup(final long securityGroupId) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListBySecurityGroup.create();
        sc.setParameters("securityGroupId", securityGroupId);
        return listBy(sc);
    }

    @Override
    public List<SecurityGroupVMMapVO> listBySecurityGroup(final long securityGroupId, final State... vmStates) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListBySecurityGroupAndStates.create();
        sc.setParameters("securityGroupId", securityGroupId);
        sc.setParameters("states", (Object[]) vmStates);
        return listBy(sc, null, true);
    }

    @Override
    public int deleteVM(final long instanceId) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListByVmId.create();
        sc.setParameters("instanceId", instanceId);
        return super.expunge(sc);
    }

    @Override
    public List<Long> listVmIdsBySecurityGroup(final long securityGroupId) {
        final SearchCriteria<Long> sc = ListVmIdBySecurityGroup.create();
        sc.setParameters("securityGroupId", securityGroupId);
        return customSearchIncludingRemoved(sc, null);
    }

    @Override
    public SecurityGroupVMMapVO findByVmIdGroupId(final long instanceId, final long securityGroupId) {
        final SearchCriteria<SecurityGroupVMMapVO> sc = ListByVmIdGroupId.create();
        sc.setParameters("securityGroupId", securityGroupId);
        sc.setParameters("instanceId", instanceId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public long countSGForVm(final long instanceId) {
        final SearchCriteria<Long> sc = CountSGForVm.create();
        sc.setParameters("vmId", instanceId);
        return customSearch(sc, null).get(0);
    }
}
