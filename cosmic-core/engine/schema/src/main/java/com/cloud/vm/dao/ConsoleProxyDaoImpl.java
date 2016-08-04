package com.cloud.vm.dao;

import com.cloud.info.ConsoleProxyLoadInfo;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.VirtualMachine.State;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleProxyDaoImpl extends GenericDaoBase<ConsoleProxyVO, Long> implements ConsoleProxyDao {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyDaoImpl.class);

    //
    // query SQL for returnning console proxy assignment info as following
    //         proxy vm id, count of assignment
    //
    private static final String PROXY_ASSIGNMENT_MATRIX = "SELECT c.id, count(runningVm.id) AS count "
            + " FROM console_proxy AS c LEFT JOIN vm_instance AS i ON c.id=i.id LEFT JOIN" + " (SELECT v.id AS id, v.proxy_id AS proxy_id FROM vm_instance AS v WHERE "
            + "  (v.state='Running' OR v.state='Creating' OR v.state='Starting' OR v.state='Migrating')) "
            + " AS runningVm ON c.id = runningVm.proxy_id WHERE i.state='Running' " + " GROUP BY c.id";

    //
    // query SQL for returnning running VM count at data center basis
    //
    private static final String DATACENTER_VM_MATRIX = "SELECT d.id, d.name, count(v.id) AS count"
            + " FROM data_center AS d LEFT JOIN vm_instance AS v ON v.data_center_id=d.id "
            + " WHERE (v.state='Creating' OR v.state='Starting' OR v.state='Running' OR v.state='Migrating')" + " GROUP BY d.id, d.name";

    private static final String DATACENTER_ACTIVE_SESSION_MATRIX = "SELECT d.id, d.name, sum(c.active_session) AS count"
            + " FROM data_center AS d LEFT JOIN vm_instance AS v ON v.data_center_id=d.id " + " LEFT JOIN console_proxy AS c ON v.id=c.id "
            + " WHERE v.type='ConsoleProxy' AND (v.state='Creating' OR v.state='Starting' OR v.state='Running' OR v.state='Migrating')" + " GROUP BY d.id, d.name";

    //
    // query SQL for returnning running console proxy count at data center basis
    //
    private static final String DATACENTER_PROXY_MATRIX =
            "SELECT d.id, d.name, count(dcid) as count"
                    + " FROM data_center as d"
                    + " LEFT JOIN ("
                    + " SELECT v.data_center_id as dcid, c.active_session as active_session from vm_instance as v"
                    + " INNER JOIN console_proxy as c ON v.id=c.id AND v.type='ConsoleProxy' AND (v.state='Creating' OR v.state='Starting' OR v.state='Running' OR v" +
                    ".state='Migrating')"
                    + " ) as t ON d.id = t.dcid" + " GROUP BY d.id, d.name";

    private static final String GET_PROXY_LOAD = "SELECT count(*) AS count" + " FROM vm_instance AS v "
            + " WHERE v.proxy_id=? AND (v.state='Running' OR v.state='Starting' OR v.state='Creating' OR v.state='Migrating')";

    private static final String GET_PROXY_ACTIVE_LOAD = "SELECT active_session AS count" + " FROM console_proxy" + " WHERE id=?";

    private static final String STORAGE_POOL_HOST_INFO = "SELECT p.data_center_id,  count(ph.host_id) " + " FROM storage_pool p, storage_pool_host_ref ph "
            + " WHERE p.id = ph.pool_id AND p.data_center_id = ? " + " GROUP by p.data_center_id";

    private static final String SHARED_STORAGE_POOL_HOST_INFO = "SELECT p.data_center_id,  count(ph.host_id) " + " FROM storage_pool p, storage_pool_host_ref ph "
            + " WHERE p.pool_type <> 'LVM' AND p.id = ph.pool_id AND p.data_center_id = ? " + " GROUP by p.data_center_id";
    protected final Attribute _updateTimeAttr;
    protected SearchBuilder<ConsoleProxyVO> DataCenterStatusSearch;
    protected SearchBuilder<ConsoleProxyVO> StateSearch;
    protected SearchBuilder<ConsoleProxyVO> HostSearch;
    protected SearchBuilder<ConsoleProxyVO> LastHostSearch;
    protected SearchBuilder<ConsoleProxyVO> HostUpSearch;
    protected SearchBuilder<ConsoleProxyVO> StateChangeSearch;

    public ConsoleProxyDaoImpl() {
        DataCenterStatusSearch = createSearchBuilder();
        DataCenterStatusSearch.and("dc", DataCenterStatusSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DataCenterStatusSearch.and("states", DataCenterStatusSearch.entity().getState(), SearchCriteria.Op.IN);
        DataCenterStatusSearch.done();

        StateSearch = createSearchBuilder();
        StateSearch.and("states", StateSearch.entity().getState(), SearchCriteria.Op.IN);
        StateSearch.done();

        HostSearch = createSearchBuilder();
        HostSearch.and("host", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();

        LastHostSearch = createSearchBuilder();
        LastHostSearch.and("lastHost", LastHostSearch.entity().getLastHostId(), SearchCriteria.Op.EQ);
        LastHostSearch.and("state", LastHostSearch.entity().getState(), SearchCriteria.Op.EQ);
        LastHostSearch.done();

        HostUpSearch = createSearchBuilder();
        HostUpSearch.and("host", HostUpSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostUpSearch.and("states", HostUpSearch.entity().getState(), SearchCriteria.Op.NIN);
        HostUpSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("states", StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("host", StateChangeSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("update", StateChangeSearch.entity().getUpdated(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();

        _updateTimeAttr = _allAttributes.get("updateTime");
        assert _updateTimeAttr != null : "Couldn't get this updateTime attribute";
    }

    @Override
    public void update(final long id, final int activeSession, final Date updateTime, final byte[] sessionDetails) {
        final ConsoleProxyVO ub = createForUpdate();
        ub.setActiveSession(activeSession);
        ub.setLastUpdateTime(updateTime);
        ub.setSessionDetails(sessionDetails);

        update(id, ub);
    }

    @Override
    public List<ConsoleProxyVO> getProxyListInStates(final long dataCenterId, final State... states) {
        final SearchCriteria<ConsoleProxyVO> sc = DataCenterStatusSearch.create();
        sc.setParameters("states", (Object[]) states);
        sc.setParameters("dc", dataCenterId);
        return listBy(sc);
    }

    @Override
    public List<ConsoleProxyVO> getProxyListInStates(final State... states) {
        final SearchCriteria<ConsoleProxyVO> sc = StateSearch.create();
        sc.setParameters("states", (Object[]) states);
        return listBy(sc);
    }

    @Override
    public List<ConsoleProxyVO> listByHostId(final long hostId) {
        final SearchCriteria<ConsoleProxyVO> sc = HostSearch.create();
        sc.setParameters("host", hostId);
        return listBy(sc);
    }

    @Override
    public List<ConsoleProxyVO> listByLastHostId(final long hostId) {
        final SearchCriteria<ConsoleProxyVO> sc = LastHostSearch.create();
        sc.setParameters("lastHost", hostId);
        sc.setParameters("state", State.Stopped);
        return listBy(sc);
    }

    @Override
    public List<ConsoleProxyVO> listUpByHostId(final long hostId) {
        final SearchCriteria<ConsoleProxyVO> sc = HostUpSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("states", new Object[]{State.Destroyed, State.Stopped, State.Expunging});
        return listBy(sc);
    }

    @Override
    public List<ConsoleProxyLoadInfo> getDatacenterProxyLoadMatrix() {
        return getDatacenterLoadMatrix(DATACENTER_PROXY_MATRIX);
    }

    @Override
    public List<ConsoleProxyLoadInfo> getDatacenterVMLoadMatrix() {
        return getDatacenterLoadMatrix(DATACENTER_VM_MATRIX);
    }

    @Override
    public List<ConsoleProxyLoadInfo> getDatacenterSessionLoadMatrix() {
        return getDatacenterLoadMatrix(DATACENTER_ACTIVE_SESSION_MATRIX);
    }

    @Override
    public List<Pair<Long, Integer>> getDatacenterStoragePoolHostInfo(final long dcId, final boolean countAllPoolTypes) {
        final ArrayList<Pair<Long, Integer>> l = new ArrayList<>();

        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            if (countAllPoolTypes) {
                pstmt = txn.prepareAutoCloseStatement(STORAGE_POOL_HOST_INFO);
            } else {
                pstmt = txn.prepareAutoCloseStatement(SHARED_STORAGE_POOL_HOST_INFO);
            }
            pstmt.setLong(1, dcId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                l.add(new Pair<>(rs.getLong(1), rs.getInt(2)));
            }
        } catch (final SQLException e) {
            s_logger.debug("Caught SQLException: ", e);
        }
        return l;
    }

    @Override
    public List<Pair<Long, Integer>> getProxyLoadMatrix() {
        final ArrayList<Pair<Long, Integer>> l = new ArrayList<>();

        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            pstmt = txn.prepareAutoCloseStatement(PROXY_ASSIGNMENT_MATRIX);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                l.add(new Pair<>(rs.getLong(1), rs.getInt(2)));
            }
        } catch (final SQLException e) {
            s_logger.debug("Caught SQLException: ", e);
        }
        return l;
    }

    @Override
    public int getProxyStaticLoad(final long proxyVmId) {
        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            pstmt = txn.prepareAutoCloseStatement(GET_PROXY_LOAD);
            pstmt.setLong(1, proxyVmId);

            final ResultSet rs = pstmt.executeQuery();
            if (rs != null && rs.first()) {
                return rs.getInt(1);
            }
        } catch (final SQLException e) {
            s_logger.debug("Caught SQLException: ", e);
        }
        return 0;
    }

    @Override
    public int getProxyActiveLoad(final long proxyVmId) {
        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            pstmt = txn.prepareAutoCloseStatement(GET_PROXY_ACTIVE_LOAD);
            pstmt.setLong(1, proxyVmId);

            final ResultSet rs = pstmt.executeQuery();
            if (rs != null && rs.first()) {
                return rs.getInt(1);
            }
        } catch (final SQLException e) {
            s_logger.debug("Caught SQLException: ", e);
        }
        return 0;
    }

    @Override
    public List<Long> getRunningProxyListByMsid(final long msid) {
        final List<Long> l = new ArrayList<>();
        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            pstmt =
                    txn.prepareAutoCloseStatement("SELECT c.id FROM console_proxy c, vm_instance v, host h "
                            + "WHERE c.id=v.id AND v.state='Running' AND v.host_id=h.id AND h.mgmt_server_id=?");

            pstmt.setLong(1, msid);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                l.add(rs.getLong(1));
            }
        } catch (final SQLException e) {
            s_logger.debug("Caught SQLException: ", e);
        }
        return l;
    }

    private List<ConsoleProxyLoadInfo> getDatacenterLoadMatrix(final String sql) {
        final ArrayList<ConsoleProxyLoadInfo> l = new ArrayList<>();

        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final ConsoleProxyLoadInfo info = new ConsoleProxyLoadInfo();
                info.setId(rs.getLong(1));
                info.setName(rs.getString(2));
                info.setCount(rs.getInt(3));
                l.add(info);
            }
        } catch (final SQLException e) {
            s_logger.debug("Exception: ", e);
        }
        return l;
    }

    @Override
    public boolean remove(final Long id) {
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            txn.start();
            final ConsoleProxyVO proxy = createForUpdate();
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
}
