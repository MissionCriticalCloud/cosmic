package com.cloud.vm.dao;

import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.vm.SecondaryStorageVm;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine.State;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecondaryStorageVmDaoImpl extends GenericDaoBase<SecondaryStorageVmVO, Long> implements SecondaryStorageVmDao {
    private static final Logger s_logger = LoggerFactory.getLogger(SecondaryStorageVmDaoImpl.class);
    protected final Attribute _updateTimeAttr;
    protected SearchBuilder<SecondaryStorageVmVO> DataCenterStatusSearch;
    protected SearchBuilder<SecondaryStorageVmVO> StateSearch;
    protected SearchBuilder<SecondaryStorageVmVO> HostSearch;
    protected SearchBuilder<SecondaryStorageVmVO> LastHostSearch;
    protected SearchBuilder<SecondaryStorageVmVO> HostUpSearch;
    protected SearchBuilder<SecondaryStorageVmVO> ZoneSearch;
    protected SearchBuilder<SecondaryStorageVmVO> StateChangeSearch;
    protected SearchBuilder<SecondaryStorageVmVO> InstanceSearch;

    public SecondaryStorageVmDaoImpl() {
        DataCenterStatusSearch = createSearchBuilder();
        DataCenterStatusSearch.and("dc", DataCenterStatusSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DataCenterStatusSearch.and("states", DataCenterStatusSearch.entity().getState(), SearchCriteria.Op.IN);
        DataCenterStatusSearch.and("role", DataCenterStatusSearch.entity().getRole(), SearchCriteria.Op.EQ);
        DataCenterStatusSearch.done();

        StateSearch = createSearchBuilder();
        StateSearch.and("states", StateSearch.entity().getState(), SearchCriteria.Op.IN);
        StateSearch.and("role", StateSearch.entity().getRole(), SearchCriteria.Op.EQ);
        StateSearch.done();

        HostSearch = createSearchBuilder();
        HostSearch.and("host", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.and("role", HostSearch.entity().getRole(), SearchCriteria.Op.EQ);
        HostSearch.done();

        InstanceSearch = createSearchBuilder();
        InstanceSearch.and("instanceName", InstanceSearch.entity().getInstanceName(), SearchCriteria.Op.EQ);
        InstanceSearch.done();

        LastHostSearch = createSearchBuilder();
        LastHostSearch.and("lastHost", LastHostSearch.entity().getLastHostId(), SearchCriteria.Op.EQ);
        LastHostSearch.and("state", LastHostSearch.entity().getState(), SearchCriteria.Op.EQ);
        LastHostSearch.and("role", LastHostSearch.entity().getRole(), SearchCriteria.Op.EQ);
        LastHostSearch.done();

        HostUpSearch = createSearchBuilder();
        HostUpSearch.and("host", HostUpSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostUpSearch.and("states", HostUpSearch.entity().getState(), SearchCriteria.Op.NIN);
        HostUpSearch.and("role", HostUpSearch.entity().getRole(), SearchCriteria.Op.EQ);
        HostUpSearch.done();

        ZoneSearch = createSearchBuilder();
        ZoneSearch.and("zone", ZoneSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneSearch.and("role", ZoneSearch.entity().getRole(), SearchCriteria.Op.EQ);
        ZoneSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("states", StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("host", StateChangeSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("update", StateChangeSearch.entity().getUpdated(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("role", StateChangeSearch.entity().getUpdated(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();

        _updateTimeAttr = _allAttributes.get("updateTime");
        assert _updateTimeAttr != null : "Couldn't get this updateTime attribute";
    }

    @Override
    public boolean remove(final Long id) {
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            txn.start();
            final SecondaryStorageVmVO proxy = createForUpdate();
            proxy.setPublicIpAddress(null);
            proxy.setPrivateIpAddress(null);

            final UpdateBuilder ub = getUpdateBuilder(proxy);
            ub.set(proxy, "state", State.Destroyed);
            ub.set(proxy, "privateIpAddress", null);
            update(id, ub, proxy);

            final boolean result = super.remove(id);
            txn.commit();
            return result;
        }
    }

    @Override
    public List<SecondaryStorageVmVO> getSecStorageVmListInStates(final SecondaryStorageVm.Role role, final long dataCenterId, final State... states) {
        final SearchCriteria<SecondaryStorageVmVO> sc = DataCenterStatusSearch.create();
        sc.setParameters("states", (Object[]) states);
        sc.setParameters("dc", dataCenterId);
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> getSecStorageVmListInStates(final SecondaryStorageVm.Role role, final State... states) {
        final SearchCriteria<SecondaryStorageVmVO> sc = StateSearch.create();
        sc.setParameters("states", (Object[]) states);
        if (role != null) {
            sc.setParameters("role", role);
        }

        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listByHostId(final SecondaryStorageVm.Role role, final long hostId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = HostSearch.create();
        sc.setParameters("host", hostId);
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listByLastHostId(final SecondaryStorageVm.Role role, final long hostId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = LastHostSearch.create();
        sc.setParameters("lastHost", hostId);
        sc.setParameters("state", State.Stopped);
        if (role != null) {
            sc.setParameters("role", role);
        }

        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listUpByHostId(final SecondaryStorageVm.Role role, final long hostId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = HostUpSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("states", new Object[]{State.Destroyed, State.Stopped, State.Expunging});
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<SecondaryStorageVmVO> listByZoneId(final SecondaryStorageVm.Role role, final long zoneId) {
        final SearchCriteria<SecondaryStorageVmVO> sc = ZoneSearch.create();
        sc.setParameters("zone", zoneId);
        if (role != null) {
            sc.setParameters("role", role);
        }
        return listBy(sc);
    }

    @Override
    public List<Long> getRunningSecStorageVmListByMsid(final SecondaryStorageVm.Role role, final long msid) {
        final List<Long> l = new ArrayList<>();
        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            final String sql;
            if (role == null) {
                sql =
                        "SELECT s.id FROM secondary_storage_vm s, vm_instance v, host h " + "WHERE s.id=v.id AND v.state='Running' AND v.host_id=h.id AND h.mgmt_server_id=?";
            } else {
                sql =
                        "SELECT s.id FROM secondary_storage_vm s, vm_instance v, host h "
                                + "WHERE s.id=v.id AND v.state='Running' AND s.role=? AND v.host_id=h.id AND h.mgmt_server_id=?";
            }

            pstmt = txn.prepareAutoCloseStatement(sql);

            if (role == null) {
                pstmt.setLong(1, msid);
            } else {
                pstmt.setString(1, role.toString());
                pstmt.setLong(2, msid);
            }

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                l.add(rs.getLong(1));
            }
        } catch (final SQLException e) {
            s_logger.debug("Caught SQLException: ", e);
        }
        return l;
    }

    @Override
    public List<Long> listRunningSecStorageOrderByLoad(final SecondaryStorageVm.Role role, final long zoneId) {

        final List<Long> l = new ArrayList<>();
        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            final String sql;
            if (role == null) {
                sql =
                        "SELECT s.id, count(l.id) as count FROM secondary_storage_vm s INNER JOIN vm_instance v ON s.id=v.id LEFT JOIN cmd_exec_log l ON s.id=l.instance_id WHERE" +
                                " v.state='Running' AND v.data_center_id=? GROUP BY s.id ORDER BY count";
            } else {
                sql =
                        "SELECT s.id, count(l.id) as count FROM secondary_storage_vm s INNER JOIN vm_instance v ON s.id=v.id LEFT JOIN cmd_exec_log l ON s.id=l.instance_id WHERE" +
                                " v.state='Running' AND v.data_center_id=? AND s.role=? GROUP BY s.id ORDER BY count";
            }

            pstmt = txn.prepareAutoCloseStatement(sql);

            if (role == null) {
                pstmt.setLong(1, zoneId);
            } else {
                pstmt.setLong(1, zoneId);
                pstmt.setString(2, role.toString());
            }

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                l.add(rs.getLong(1));
            }
        } catch (final SQLException e) {
            s_logger.error("Unexpected exception ", e);
        }

        return l;
    }

    @Override
    public SecondaryStorageVmVO findByInstanceName(final String instanceName) {
        final SearchCriteria<SecondaryStorageVmVO> sc = InstanceSearch.create();
        sc.setParameters("instanceName", instanceName);
        final List<SecondaryStorageVmVO> list = listBy(sc);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
