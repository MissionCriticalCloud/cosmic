package com.cloud.network;

import com.cloud.dc.HostPodVO;
import com.cloud.dc.StorageNetworkIpAddressVO;
import com.cloud.dc.StorageNetworkIpRange;
import com.cloud.dc.StorageNetworkIpRangeVO;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.StorageNetworkIpAddressDao;
import com.cloud.dc.dao.StorageNetworkIpRangeDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionCallbackWithException;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.SecondaryStorageVmDao;
import org.apache.cloudstack.api.command.admin.network.CreateStorageNetworkIpRangeCmd;
import org.apache.cloudstack.api.command.admin.network.DeleteStorageNetworkIpRangeCmd;
import org.apache.cloudstack.api.command.admin.network.ListStorageNetworkIpRangeCmd;
import org.apache.cloudstack.api.command.admin.network.UpdateStorageNetworkIpRangeCmd;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageNetworkManagerImpl extends ManagerBase implements StorageNetworkManager, StorageNetworkService {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageNetworkManagerImpl.class);

    @Inject
    StorageNetworkIpAddressDao _sNwIpDao;
    @Inject
    StorageNetworkIpRangeDao _sNwIpRangeDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    HostPodDao _podDao;
    @Inject
    SecondaryStorageVmDao _ssvmDao;

    @Override
    @DB
    public StorageNetworkIpRange createIpRange(final CreateStorageNetworkIpRangeCmd cmd) throws SQLException {
        final Long podId = cmd.getPodId();
        final String startIp = cmd.getStartIp();
        String endIp = cmd.getEndIp();
        final Integer vlan = cmd.getVlan();
        final String netmask = cmd.getNetmask();

        if (endIp == null) {
            endIp = startIp;
        }

        if (!NetUtils.isValidNetmask(netmask)) {
            throw new CloudRuntimeException("Invalid netmask:" + netmask);
        }

        final HostPodVO pod = _podDao.findById(podId);
        if (pod == null) {
            throw new CloudRuntimeException("Cannot find pod " + podId);
        }
        final Long zoneId = pod.getDataCenterId();

        final List<NetworkVO> nws = _networkDao.listByZoneAndTrafficType(zoneId, TrafficType.Storage);
        if (nws.size() == 0) {
            throw new CloudRuntimeException("Cannot find storage network in zone " + zoneId);
        }
        if (nws.size() > 1) {
            throw new CloudRuntimeException("Find more than one storage network in zone " + zoneId + "," + nws.size() + " found");
        }
        final NetworkVO nw = nws.get(0);

        checkOverlapPrivateIpRange(podId, startIp, endIp);
        checkOverlapStorageIpRange(podId, startIp, endIp);

        final StorageNetworkIpRangeVO range = null;

        final String endIpFinal = endIp;
        return Transaction.execute(new TransactionCallbackWithException<StorageNetworkIpRangeVO, SQLException>() {
            @Override
            public StorageNetworkIpRangeVO doInTransaction(final TransactionStatus status) throws SQLException {
                final StorageNetworkIpRangeVO range = new StorageNetworkIpRangeVO(zoneId, podId, nw.getId(), startIp, endIpFinal, vlan, netmask, cmd.getGateWay());
                _sNwIpRangeDao.persist(range);
                try {
                    createStorageIpEntires(TransactionLegacy.currentTxn(), range.getId(), startIp, endIpFinal, zoneId);
                } catch (final SQLException e) {
                    final StringBuilder err = new StringBuilder();
                    err.append("Create storage network range failed.");
                    err.append("startIp=" + startIp);
                    err.append("endIp=" + endIpFinal);
                    err.append("netmask=" + netmask);
                    err.append("zoneId=" + zoneId);
                    s_logger.debug(err.toString(), e);
                    throw e;
                }

                return range;
            }
        });
    }

    @Override
    @DB
    public void deleteIpRange(final DeleteStorageNetworkIpRangeCmd cmd) {
        final long rangeId = cmd.getId();
        final StorageNetworkIpRangeVO range = _sNwIpRangeDao.findById(rangeId);
        if (range == null) {
            throw new CloudRuntimeException("Can not find storage network ip range " + rangeId);
        }

        if (_sNwIpDao.countInUseIpByRangeId(rangeId) > 0) {
            throw new CloudRuntimeException(getInUseIpAddress(rangeId));
        }

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                StorageNetworkIpRangeVO range = null;
                try {
                    range = _sNwIpRangeDao.acquireInLockTable(rangeId);
                    if (range == null) {
                        final String msg = "Unable to acquire lock on storage network ip range id=" + rangeId + ", delete failed";
                        s_logger.warn(msg);
                        throw new CloudRuntimeException(msg);
                    }
                    /*
                     * entries in op_dc_storage_network_ip_address will be deleted automatically due to
                     * fk_storage_ip_address__range_id constraint key
                     */
                    _sNwIpRangeDao.remove(rangeId);
                } finally {
                    if (range != null) {
                        _sNwIpRangeDao.releaseFromLockTable(rangeId);
                    }
                }
            }
        });
    }

    @Override
    public List<StorageNetworkIpRange> listIpRange(final ListStorageNetworkIpRangeCmd cmd) {
        final Long rangeId = cmd.getRangeId();
        final Long podId = cmd.getPodId();
        final Long zoneId = cmd.getZoneId();

        List result = null;
        if (rangeId != null) {
            result = _sNwIpRangeDao.listByRangeId(rangeId);
        } else if (podId != null) {
            result = _sNwIpRangeDao.listByPodId(podId);
        } else if (zoneId != null) {
            result = _sNwIpRangeDao.listByDataCenterId(zoneId);
        } else {
            result = _sNwIpRangeDao.listAll();
        }

        return result;
    }

    @Override
    @DB
    public StorageNetworkIpRange updateIpRange(final UpdateStorageNetworkIpRangeCmd cmd) {
        final Integer vlan = cmd.getVlan();
        final Long rangeId = cmd.getId();
        String startIp = cmd.getStartIp();
        String endIp = cmd.getEndIp();
        final String netmask = cmd.getNetmask();

        if (netmask != null && !NetUtils.isValidNetmask(netmask)) {
            throw new CloudRuntimeException("Invalid netmask:" + netmask);
        }

        if (_sNwIpDao.countInUseIpByRangeId(rangeId) > 0) {
            throw new CloudRuntimeException("Cannot update the range," + getInUseIpAddress(rangeId));
        }

        final StorageNetworkIpRangeVO range = _sNwIpRangeDao.findById(rangeId);
        if (range == null) {
            throw new CloudRuntimeException("Cannot find storage ip range " + rangeId);
        }

        if (startIp != null || endIp != null) {
            final long podId = range.getPodId();
            startIp = startIp == null ? range.getStartIp() : startIp;
            endIp = endIp == null ? range.getEndIp() : endIp;
            checkOverlapPrivateIpRange(podId, startIp, endIp);
            checkOverlapStorageIpRange(podId, startIp, endIp);
        }

        final String startIpFinal = startIp;
        final String endIpFinal = endIp;
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                StorageNetworkIpRangeVO range = null;
                try {
                    range = _sNwIpRangeDao.acquireInLockTable(rangeId);
                    if (range == null) {
                        throw new CloudRuntimeException("Cannot acquire lock on storage ip range " + rangeId);
                    }
                    final StorageNetworkIpRangeVO vo = _sNwIpRangeDao.createForUpdate();
                    if (vlan != null) {
                        vo.setVlan(vlan);
                    }
                    if (startIpFinal != null) {
                        vo.setStartIp(startIpFinal);
                    }
                    if (endIpFinal != null) {
                        vo.setEndIp(endIpFinal);
                    }
                    if (netmask != null) {
                        vo.setNetmask(netmask);
                    }
                    _sNwIpRangeDao.update(rangeId, vo);
                } finally {
                    if (range != null) {
                        _sNwIpRangeDao.releaseFromLockTable(range.getId());
                    }
                }
            }
        });

        return _sNwIpRangeDao.findById(rangeId);
    }

    private String getInUseIpAddress(final long rangeId) {
        final List<String> ips = _sNwIpDao.listInUseIpByRangeId(rangeId);
        final StringBuilder res = new StringBuilder();
        res.append("Below IP of range " + rangeId + " is still in use:");
        for (final String ip : ips) {
            res.append(ip).append(",");
        }
        return res.toString();
    }

    private void checkOverlapPrivateIpRange(final long podId, final String startIp, final String endIp) {
        final HostPodVO pod = _podDao.findById(podId);
        if (pod == null) {
            throw new CloudRuntimeException("Cannot find pod " + podId);
        }
        final String[] IpRange = pod.getDescription().split("-");
        if ((IpRange[0] == null || IpRange[1] == null) || (!NetUtils.isValidIp(IpRange[0]) || !NetUtils.isValidIp(IpRange[1]))) {
            return;
        }
        if (NetUtils.ipRangesOverlap(startIp, endIp, IpRange[0], IpRange[1])) {
            throw new InvalidParameterValueException("The Storage network Start IP and endIP address range overlap with private IP :" + IpRange[0] + ":" + IpRange[1]);
        }
    }

    private void checkOverlapStorageIpRange(final long podId, final String startIp, final String endIp) {
        final List<StorageNetworkIpRangeVO> curRanges = _sNwIpRangeDao.listByPodId(podId);
        for (final StorageNetworkIpRangeVO range : curRanges) {
            if (NetUtils.ipRangesOverlap(startIp, endIp, range.getStartIp(), range.getEndIp())) {
                throw new InvalidParameterValueException("The Storage network Start IP and endIP address range overlap with private IP :" + range.getStartIp() + " - " +
                        range.getEndIp());
            }
        }
    }

    private void createStorageIpEntires(final TransactionLegacy txn, final long rangeId, final String startIp, final String endIp, final long zoneId) throws SQLException {
        long startIPLong = NetUtils.ip2Long(startIp);
        final long endIPLong = NetUtils.ip2Long(endIp);
        final String insertSql =
                "INSERT INTO `cloud`.`op_dc_storage_network_ip_address` (range_id, ip_address, mac_address, taken) VALUES (?, ?, (select mac_address from `cloud`.`data_center` " +
                        "where id=?), ?)";
        final String updateSql = "UPDATE `cloud`.`data_center` set mac_address = mac_address+1 where id=?";
        final Connection conn = txn.getConnection();
        while (startIPLong <= endIPLong) {
            try (PreparedStatement stmt_insert = conn.prepareStatement(insertSql)) {
                stmt_insert.setLong(1, rangeId);
                stmt_insert.setString(2, NetUtils.long2Ip(startIPLong++));
                stmt_insert.setLong(3, zoneId);
                stmt_insert.setNull(4, java.sql.Types.DATE);
                stmt_insert.executeUpdate();
            }

            try (PreparedStatement stmt_update = txn.prepareStatement(updateSql)) {
                stmt_update.setLong(1, zoneId);
                stmt_update.executeUpdate();
            }
        }
    }

    @Override
    public StorageNetworkIpAddressVO acquireIpAddress(final long podId) {
        final List<StorageNetworkIpRangeVO> ranges = _sNwIpRangeDao.listByPodId(podId);
        for (StorageNetworkIpRangeVO r : ranges) {
            try {
                final Long rangeId = r.getId();
                r = _sNwIpRangeDao.acquireInLockTable(rangeId);
                if (r == null) {
                    final String msg = "Unable to acquire lock on storage network ip range id=" + rangeId + ", delete failed";
                    s_logger.warn(msg);
                    throw new CloudRuntimeException(msg);
                }

                final StorageNetworkIpAddressVO ip = _sNwIpDao.takeIpAddress(r.getId());
                if (ip != null) {
                    return ip;
                }
            } finally {
                if (r != null) {
                    _sNwIpRangeDao.releaseFromLockTable(r.getId());
                }
            }
        }

        return null;
    }

    @Override
    public void releaseIpAddress(final String ip) {
        _sNwIpDao.releaseIpAddress(ip);
    }

    @Override
    public boolean isStorageIpRangeAvailable(final long zoneId) {
        final QueryBuilder<StorageNetworkIpRangeVO> sc = QueryBuilder.create(StorageNetworkIpRangeVO.class);
        sc.and(sc.entity().getDataCenterId(), Op.EQ, zoneId);
        final List<StorageNetworkIpRangeVO> entries = sc.list();
        return entries.size() > 0;
    }

    @Override
    public List<SecondaryStorageVmVO> getSSVMWithNoStorageNetwork(final long zoneId) {
        final List<SecondaryStorageVmVO> ssvms =
                _ssvmDao.getSecStorageVmListInStates(null, zoneId, VirtualMachine.State.Starting, VirtualMachine.State.Running, VirtualMachine.State.Stopping);
        return ssvms;
    }

    @Override
    public boolean isAnyStorageIpInUseInZone(final long zoneId) {
        final List<StorageNetworkIpRangeVO> ranges = _sNwIpRangeDao.listByDataCenterId(zoneId);
        for (final StorageNetworkIpRangeVO r : ranges) {
            if (_sNwIpDao.countInUseIpByRangeId(r.getId()) > 0) {
                return true;
            }
        }
        return false;
    }
}
