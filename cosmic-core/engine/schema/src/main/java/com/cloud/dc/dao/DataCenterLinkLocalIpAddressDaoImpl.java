package com.cloud.dc.dao;

import com.cloud.dc.DataCenterLinkLocalIpAddressVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB
public class DataCenterLinkLocalIpAddressDaoImpl extends GenericDaoBase<DataCenterLinkLocalIpAddressVO, Long> implements DataCenterLinkLocalIpAddressDao {
    private static final Logger s_logger = LoggerFactory.getLogger(DataCenterLinkLocalIpAddressDaoImpl.class);

    private final SearchBuilder<DataCenterLinkLocalIpAddressVO> AllFieldsSearch;
    private final GenericSearchBuilder<DataCenterLinkLocalIpAddressVO, Integer> AllIpCount;
    private final GenericSearchBuilder<DataCenterLinkLocalIpAddressVO, Integer> AllAllocatedIpCount;

    public DataCenterLinkLocalIpAddressDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("ip", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("dc", AllFieldsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("pod", AllFieldsSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("instance", AllFieldsSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("reservation", AllFieldsSearch.entity().getReservationId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("taken", AllFieldsSearch.entity().getTakenAt(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        AllIpCount = createSearchBuilder(Integer.class);
        AllIpCount.select(null, Func.COUNT, AllIpCount.entity().getId());
        AllIpCount.and("pod", AllIpCount.entity().getPodId(), SearchCriteria.Op.EQ);
        AllIpCount.done();

        AllAllocatedIpCount = createSearchBuilder(Integer.class);
        AllAllocatedIpCount.select(null, Func.COUNT, AllAllocatedIpCount.entity().getId());
        AllAllocatedIpCount.and("pod", AllAllocatedIpCount.entity().getPodId(), SearchCriteria.Op.EQ);
        AllAllocatedIpCount.and("removed", AllAllocatedIpCount.entity().getTakenAt(), SearchCriteria.Op.NNULL);
        AllAllocatedIpCount.done();
    }

    @Override
    @DB
    public DataCenterLinkLocalIpAddressVO takeIpAddress(final long dcId, final long podId, final long instanceId, final String reservationId) {
        final SearchCriteria<DataCenterLinkLocalIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("taken", (Date) null);

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final DataCenterLinkLocalIpAddressVO vo = lockOneRandomRow(sc, true);
        if (vo == null) {
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
    public boolean deleteIpAddressByPod(final long podId) {
        final SearchCriteria<DataCenterLinkLocalIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        return remove(sc) > 0;
    }

    @Override
    @DB
    public void addIpRange(final long dcId, final long podId, final String start, final String end) {
        final String insertSql = "INSERT INTO `cloud`.`op_dc_link_local_ip_address_alloc` (ip_address, data_center_id, pod_id) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;

        long startIP = NetUtils.ip2Long(start);
        final long endIP = NetUtils.ip2Long(end);

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            txn.start();
            stmt = txn.prepareAutoCloseStatement(insertSql);
            while (startIP <= endIP) {
                stmt.setString(1, NetUtils.long2Ip(startIP++));
                stmt.setLong(2, dcId);
                stmt.setLong(3, podId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            txn.commit();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to insert", e);
        }
    }

    @Override
    public void releaseIpAddress(final String ipAddress, final long dcId, final long instanceId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing ip address: " + ipAddress + " data center " + dcId);
        }
        final SearchCriteria<DataCenterLinkLocalIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ipAddress);
        sc.setParameters("dc", dcId);
        sc.setParameters("instance", instanceId);

        final DataCenterLinkLocalIpAddressVO vo = createForUpdate();

        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }

    @Override
    public void releaseIpAddress(final long nicId, final String reservationId) {
        final SearchCriteria<DataCenterLinkLocalIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", nicId);
        sc.setParameters("reservation", reservationId);

        final DataCenterLinkLocalIpAddressVO vo = createForUpdate();

        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }

    @Override
    public List<DataCenterLinkLocalIpAddressVO> listByPodIdDcId(final long podId, final long dcId) {
        final SearchCriteria<DataCenterLinkLocalIpAddressVO> sc = AllFieldsSearch.create();
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
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        final SearchCriteria<DataCenterLinkLocalIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", NetUtils.getLinkLocalGateway());
        remove(sc);

        return true;
    }
}
