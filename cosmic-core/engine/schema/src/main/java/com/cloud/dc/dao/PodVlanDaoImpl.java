package com.cloud.dc.dao;

import com.cloud.dc.PodVlanVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * PodVlanDaoImpl maintains the one-to-many relationship between
 */
@Component
public class PodVlanDaoImpl extends GenericDaoBase<PodVlanVO, Long> implements PodVlanDao {
    private final SearchBuilder<PodVlanVO> FreeVlanSearch;
    private final SearchBuilder<PodVlanVO> VlanPodSearch;
    private final SearchBuilder<PodVlanVO> PodSearchAllocated;

    public PodVlanDaoImpl() {
        super();
        PodSearchAllocated = createSearchBuilder();
        PodSearchAllocated.and("podId", PodSearchAllocated.entity().getPodId(), SearchCriteria.Op.EQ);
        PodSearchAllocated.and("allocated", PodSearchAllocated.entity().getTakenAt(), SearchCriteria.Op.NNULL);
        PodSearchAllocated.done();

        FreeVlanSearch = createSearchBuilder();
        FreeVlanSearch.and("podId", FreeVlanSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        FreeVlanSearch.and("taken", FreeVlanSearch.entity().getTakenAt(), SearchCriteria.Op.NULL);
        FreeVlanSearch.done();

        VlanPodSearch = createSearchBuilder();
        VlanPodSearch.and("vlan", VlanPodSearch.entity().getVlan(), SearchCriteria.Op.EQ);
        VlanPodSearch.and("podId", VlanPodSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        VlanPodSearch.and("taken", VlanPodSearch.entity().getTakenAt(), SearchCriteria.Op.NNULL);
        VlanPodSearch.and("account", VlanPodSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        VlanPodSearch.done();
    }

    @Override
    public List<PodVlanVO> listAllocatedVnets(final long podId) {
        final SearchCriteria<PodVlanVO> sc = PodSearchAllocated.create();
        sc.setParameters("podId", podId);
        return listBy(sc);
    }

    @Override
    public void add(final long podId, final int start, final int end) {
        final String insertVnet = "INSERT INTO `cloud`.`op_pod_vlan_alloc` (vlan, pod_id) VALUES ( ?, ?)";

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final PreparedStatement stmt = txn.prepareAutoCloseStatement(insertVnet);
            for (int i = start; i < end; i++) {
                stmt.setString(1, String.valueOf(i));
                stmt.setLong(2, podId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            txn.commit();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Exception caught adding vnet ", e);
        }
    }

    @Override
    public void delete(final long podId) {
        final String deleteVnet = "DELETE FROM `cloud`.`op_pod_vlan_alloc` WHERE pod_id = ?";

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            final PreparedStatement stmt = txn.prepareAutoCloseStatement(deleteVnet);
            stmt.setLong(1, podId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Exception caught deleting vnet ", e);
        }
    }

    @Override
    public PodVlanVO take(final long podId, final long accountId) {
        final SearchCriteria<PodVlanVO> sc = FreeVlanSearch.create();
        sc.setParameters("podId", podId);
        final Date now = new Date();
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            final PodVlanVO vo = lockOneRandomRow(sc, true);
            if (vo == null) {
                return null;
            }

            vo.setTakenAt(now);
            vo.setAccountId(accountId);
            update(vo.getId(), vo);
            txn.commit();
            return vo;
        } catch (final Exception e) {
            throw new CloudRuntimeException("Caught Exception ", e);
        }
    }

    @Override
    public void release(final String vlan, final long podId, final long accountId) {
        final SearchCriteria<PodVlanVO> sc = VlanPodSearch.create();
        sc.setParameters("vlan", vlan);
        sc.setParameters("podId", podId);
        sc.setParameters("account", accountId);

        final PodVlanVO vo = findOneIncludingRemovedBy(sc);
        if (vo == null) {
            return;
        }

        vo.setTakenAt(null);
        vo.setAccountId(null);
        update(vo.getId(), vo);
    }
}
