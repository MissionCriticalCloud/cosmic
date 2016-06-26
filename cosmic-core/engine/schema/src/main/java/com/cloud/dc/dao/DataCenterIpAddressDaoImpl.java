package com.cloud.dc.dao;

import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB
public class DataCenterIpAddressDaoImpl extends GenericDaoBase<DataCenterIpAddressVO, Long> implements DataCenterIpAddressDao {
    private static final Logger s_logger = LoggerFactory.getLogger(DataCenterIpAddressDaoImpl.class);

    private final SearchBuilder<DataCenterIpAddressVO> AllFieldsSearch;
    private final GenericSearchBuilder<DataCenterIpAddressVO, Integer> AllIpCount;
    private final GenericSearchBuilder<DataCenterIpAddressVO, Integer> AllIpCountForDc;
    private final GenericSearchBuilder<DataCenterIpAddressVO, Integer> AllAllocatedIpCount;
    private final GenericSearchBuilder<DataCenterIpAddressVO, Integer> AllAllocatedIpCountForDc;

    public DataCenterIpAddressDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("ip", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("dc", AllFieldsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("pod", AllFieldsSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("instance", AllFieldsSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("reservation", AllFieldsSearch.entity().getReservationId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("taken", AllFieldsSearch.entity().getTakenAt(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        AllIpCount = createSearchBuilder(Integer.class);
        AllIpCount.select(null, Func.COUNT, AllIpCount.entity().getId());
        AllIpCount.and("pod", AllIpCount.entity().getPodId(), SearchCriteria.Op.EQ);
        AllIpCount.done();

        AllIpCountForDc = createSearchBuilder(Integer.class);
        AllIpCountForDc.select(null, Func.COUNT, AllIpCountForDc.entity().getId());
        AllIpCountForDc.and("data_center_id", AllIpCountForDc.entity().getPodId(), SearchCriteria.Op.EQ);
        AllIpCountForDc.done();

        AllAllocatedIpCount = createSearchBuilder(Integer.class);
        AllAllocatedIpCount.select(null, Func.COUNT, AllAllocatedIpCount.entity().getId());
        AllAllocatedIpCount.and("pod", AllAllocatedIpCount.entity().getPodId(), SearchCriteria.Op.EQ);
        AllAllocatedIpCount.and("removed", AllAllocatedIpCount.entity().getTakenAt(), SearchCriteria.Op.NNULL);
        AllAllocatedIpCount.done();

        AllAllocatedIpCountForDc = createSearchBuilder(Integer.class);
        AllAllocatedIpCountForDc.select(null, Func.COUNT, AllAllocatedIpCountForDc.entity().getId());
        AllAllocatedIpCountForDc.and("data_center_id", AllAllocatedIpCountForDc.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AllAllocatedIpCountForDc.and("removed", AllAllocatedIpCountForDc.entity().getTakenAt(), SearchCriteria.Op.NNULL);
        AllAllocatedIpCountForDc.done();
    }

    @Override
    @DB
    public DataCenterIpAddressVO takeIpAddress(final long dcId, final long podId, final long instanceId, final String reservationId) {
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("taken", (Date) null);

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final DataCenterIpAddressVO vo = lockOneRandomRow(sc, true);
        if (vo == null) {
            txn.rollback();
            return null;
        }
        vo.setTakenAt(new Date());
        vo.setInstanceId(instanceId);
        vo.setReservationId(reservationId);
        update(vo.getId(), vo);
        txn.commit();
        return vo;
    }

    @Override
    @DB
    public DataCenterIpAddressVO takeDataCenterIpAddress(final long dcId, final String reservationId) {
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dc", dcId);
        sc.setParameters("taken", (Date) null);

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final DataCenterIpAddressVO vo = lockOneRandomRow(sc, true);
        if (vo == null) {
            txn.rollback();
            return null;
        }
        vo.setTakenAt(new Date());
        vo.setReservationId(reservationId);
        update(vo.getId(), vo);
        txn.commit();
        return vo;
    }

    @Override
    @DB
    public void addIpRange(final long dcId, final long podId, final String start, final String end) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final String insertSql = "INSERT INTO `cloud`.`op_dc_ip_address_alloc` (ip_address, data_center_id, pod_id, mac_address) " +
                "VALUES (?, ?, ?, (select mac_address from `cloud`.`data_center` where id=?))";
        final String updateSql = "UPDATE `cloud`.`data_center` set mac_address = mac_address+1 where id=?";

        long startIP = NetUtils.ip2Long(start);
        final long endIP = NetUtils.ip2Long(end);

        try {
            txn.start();

            while (startIP <= endIP) {
                try (PreparedStatement insertPstmt = txn.prepareStatement(insertSql)) {
                    insertPstmt.setString(1, NetUtils.long2Ip(startIP++));
                    insertPstmt.setLong(2, dcId);
                    insertPstmt.setLong(3, podId);
                    insertPstmt.setLong(4, dcId);
                    insertPstmt.executeUpdate();
                }
                try (PreparedStatement updatePstmt = txn.prepareStatement(updateSql)) {
                    updatePstmt.setLong(1, dcId);
                    updatePstmt.executeUpdate();
                }
            }
            txn.commit();
        } catch (final SQLException ex) {
            throw new CloudRuntimeException("Unable to persist ip address range ", ex);
        }
    }

    @Override
    public void releaseIpAddress(final String ipAddress, final long dcId, final Long instanceId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing ip address: " + ipAddress + " data center " + dcId);
        }
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ipAddress);
        sc.setParameters("dc", dcId);
        sc.setParameters("instance", instanceId);

        final DataCenterIpAddressVO vo = createForUpdate();

        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }

    @Override
    public void releaseIpAddress(final long nicId, final String reservationId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing ip address for reservationId=" + reservationId + ", instance=" + nicId);
        }
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", nicId);
        sc.setParameters("reservation", reservationId);

        final DataCenterIpAddressVO vo = createForUpdate();
        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }

    @Override
    public void releaseIpAddress(final long nicId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing ip address for instance=" + nicId);
        }
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", nicId);

        final DataCenterIpAddressVO vo = createForUpdate();
        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }

    @Override
    public boolean mark(final long dcId, final long podId, final String ip) {
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("ipAddress", ip);

        final DataCenterIpAddressVO vo = createForUpdate();
        vo.setTakenAt(new Date());

        return update(vo, sc) >= 1;
    }

    @Override
    public List<DataCenterIpAddressVO> listByPodIdDcIdIpAddress(final long podId, final long dcId, final String ipAddress) {
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("ipAddress", ipAddress);
        return listBy(sc);
    }

    @Override
    public List<DataCenterIpAddressVO> listByPodIdDcId(final long podId, final long dcId) {
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        return listBy(sc);
    }

    @Override
    public int countIPs(final long podId, final long dcId, final boolean onlyCountAllocated) {
        final SearchCriteria<Integer> sc;
        if (onlyCountAllocated) {
            sc = AllAllocatedIpCount.create();
        } else {
            sc = AllIpCount.create();
        }

        sc.setParameters("pod", podId);
        final List<Integer> count = customSearch(sc, null);
        return count.get(0);
    }

    @Override
    public int countIPs(final long dcId, final boolean onlyCountAllocated) {
        final SearchCriteria<Integer> sc;
        if (onlyCountAllocated) {
            sc = AllAllocatedIpCountForDc.create();
        } else {
            sc = AllIpCountForDc.create();
        }

        sc.setParameters("data_center_id", dcId);
        final List<Integer> count = customSearch(sc, null);
        return count.get(0);
    }

    @Override
    public boolean deleteIpAddressByPod(final long podId) {
        final SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        return remove(sc) > 0;
    }
}
