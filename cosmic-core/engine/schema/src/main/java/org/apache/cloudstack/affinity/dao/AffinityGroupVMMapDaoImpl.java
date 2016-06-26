package org.apache.cloudstack.affinity.dao;

import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.affinity.AffinityGroupVMMapVO;
import org.apache.cloudstack.affinity.AffinityGroupVO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

public class AffinityGroupVMMapDaoImpl extends GenericDaoBase<AffinityGroupVMMapVO, Long> implements AffinityGroupVMMapDao {
    protected GenericSearchBuilder<AffinityGroupVMMapVO, Long> CountSGForVm;
    @Inject
    protected AffinityGroupDao _affinityGroupDao;
    private SearchBuilder<AffinityGroupVMMapVO> ListByVmId;
    private SearchBuilder<AffinityGroupVMMapVO> ListByVmIdGroupId;
    private GenericSearchBuilder<AffinityGroupVMMapVO, Long> ListVmIdByAffinityGroup;
    private SearchBuilder<AffinityGroupVMMapVO> ListByAffinityGroup;
    private SearchBuilder<AffinityGroupVMMapVO> ListByVmIdType;
    private GenericSearchBuilder<AffinityGroupVMMapVO, Long> ListAffinityGroupIdByVm;

    public AffinityGroupVMMapDaoImpl() {
    }

    @PostConstruct
    protected void init() {
        ListVmIdByAffinityGroup = createSearchBuilder(Long.class);
        ListVmIdByAffinityGroup.and("affinityGroupId", ListVmIdByAffinityGroup.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
        ListVmIdByAffinityGroup.selectFields(ListVmIdByAffinityGroup.entity().getInstanceId());
        ListVmIdByAffinityGroup.done();

        ListByAffinityGroup = createSearchBuilder();
        ListByAffinityGroup.and("affinityGroupId", ListByAffinityGroup.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
        ListByAffinityGroup.done();

        ListByVmId = createSearchBuilder();
        ListByVmId.and("instanceId", ListByVmId.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListByVmId.done();

        ListByVmIdGroupId = createSearchBuilder();
        ListByVmIdGroupId.and("instanceId", ListByVmIdGroupId.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListByVmIdGroupId.and("affinityGroupId", ListByVmIdGroupId.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
        ListByVmIdGroupId.done();

        final SearchBuilder<AffinityGroupVO> groupSearch = _affinityGroupDao.createSearchBuilder();
        groupSearch.and("type", groupSearch.entity().getType(), SearchCriteria.Op.EQ);

        ListByVmIdType = createSearchBuilder();
        ListByVmIdType.and("instanceId", ListByVmIdType.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListByVmIdType.join("groupSearch", groupSearch, ListByVmIdType.entity().getAffinityGroupId(), groupSearch.entity().getId(), JoinType.INNER);
        ListByVmIdType.done();

        CountSGForVm = createSearchBuilder(Long.class);
        CountSGForVm.select(null, Func.COUNT, null);
        CountSGForVm.and("vmId", CountSGForVm.entity().getInstanceId(), SearchCriteria.Op.EQ);
        CountSGForVm.done();

        ListAffinityGroupIdByVm = createSearchBuilder(Long.class);
        ListAffinityGroupIdByVm.and("instanceId", ListAffinityGroupIdByVm.entity().getInstanceId(), SearchCriteria.Op.EQ);
        ListAffinityGroupIdByVm.selectFields(ListAffinityGroupIdByVm.entity().getAffinityGroupId());
        ListAffinityGroupIdByVm.done();
    }

    @Override
    public List<AffinityGroupVMMapVO> listByInstanceId(final long vmId) {
        final SearchCriteria<AffinityGroupVMMapVO> sc = ListByVmId.create();
        sc.setParameters("instanceId", vmId);
        return listBy(sc);
    }

    @Override
    public Pair<List<AffinityGroupVMMapVO>, Integer> listByInstanceId(final long instanceId, final Filter filter) {
        final SearchCriteria<AffinityGroupVMMapVO> sc = ListByVmId.create();
        sc.setParameters("instanceId", instanceId);
        return this.searchAndCount(sc, filter);
    }

    @Override
    public List<AffinityGroupVMMapVO> listByAffinityGroup(final long affinityGroupId) {
        final SearchCriteria<AffinityGroupVMMapVO> sc = ListByAffinityGroup.create();
        sc.setParameters("affinityGroupId", affinityGroupId);
        return listBy(sc);
    }

    @Override
    public List<Long> listVmIdsByAffinityGroup(final long affinityGroupId) {
        final SearchCriteria<Long> sc = ListVmIdByAffinityGroup.create();
        sc.setParameters("affinityGroupId", affinityGroupId);
        return customSearchIncludingRemoved(sc, null);
    }

    @Override
    public AffinityGroupVMMapVO findByVmIdGroupId(final long instanceId, final long affinityGroupId) {
        final SearchCriteria<AffinityGroupVMMapVO> sc = ListByVmIdGroupId.create();
        sc.setParameters("affinityGroupId", affinityGroupId);
        sc.setParameters("instanceId", instanceId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public long countAffinityGroupsForVm(final long instanceId) {
        final SearchCriteria<Long> sc = CountSGForVm.create();
        sc.setParameters("vmId", instanceId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public int deleteVM(final long instanceId) {
        final SearchCriteria<AffinityGroupVMMapVO> sc = ListByVmId.create();
        sc.setParameters("instanceId", instanceId);
        return super.expunge(sc);
    }

    @Override
    public List<AffinityGroupVMMapVO> findByVmIdType(final long instanceId, final String type) {
        final SearchCriteria<AffinityGroupVMMapVO> sc = ListByVmIdType.create();
        sc.setParameters("instanceId", instanceId);
        sc.setJoinParameters("groupSearch", "type", type);
        return listBy(sc);
    }

    @Override
    public void updateMap(final Long vmId, final List<Long> affinityGroupIds) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final SearchCriteria<AffinityGroupVMMapVO> sc = createSearchCriteria();
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, vmId);
        expunge(sc);

        for (final Long groupId : affinityGroupIds) {
            final AffinityGroupVMMapVO vo = new AffinityGroupVMMapVO(groupId, vmId);
            persist(vo);
        }

        txn.commit();
    }

    @Override
    public List<Long> listAffinityGroupIdsByVmId(final long instanceId) {
        final SearchCriteria<Long> sc = ListAffinityGroupIdByVm.create();
        sc.setParameters("instanceId", instanceId);
        return customSearchIncludingRemoved(sc, null);
    }
}
