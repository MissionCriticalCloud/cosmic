package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.host.DetailVO;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component(value = "EngineHostDetailsDao")
public class HostDetailsDaoImpl extends GenericDaoBase<DetailVO, Long> implements HostDetailsDao {
    protected final SearchBuilder<DetailVO> HostSearch;
    protected final SearchBuilder<DetailVO> DetailSearch;

    public HostDetailsDaoImpl() {
        HostSearch = createSearchBuilder();
        HostSearch.and("hostId", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();

        DetailSearch = createSearchBuilder();
        DetailSearch.and("hostId", DetailSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        DetailSearch.and("name", DetailSearch.entity().getName(), SearchCriteria.Op.EQ);
        DetailSearch.done();
    }

    @Override
    public Map<String, String> findDetails(final long hostId) {
        final SearchCriteria<DetailVO> sc = HostSearch.create();
        sc.setParameters("hostId", hostId);

        final List<DetailVO> results = search(sc, null);
        final Map<String, String> details = new HashMap<>(results.size());
        for (final DetailVO result : results) {
            if ("password".equals(result.getName())) {
                details.put(result.getName(), DBEncryptionUtil.decrypt(result.getValue()));
            } else {
                details.put(result.getName(), result.getValue());
            }
        }
        return details;
    }

    @Override
    public void persist(final long hostId, final Map<String, String> details) {
        final String InsertOrUpdateSql = "INSERT INTO `cloud`.`host_details` (host_id, name, value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE value=?";

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        for (final Map.Entry<String, String> detail : details.entrySet()) {
            String value = detail.getValue();
            if ("password".equals(detail.getKey())) {
                value = DBEncryptionUtil.encrypt(value);
            }
            try {
                final PreparedStatement pstmt = txn.prepareAutoCloseStatement(InsertOrUpdateSql);
                pstmt.setLong(1, hostId);
                pstmt.setString(2, detail.getKey());
                pstmt.setString(3, value);
                pstmt.setString(4, value);
                pstmt.executeUpdate();
            } catch (final SQLException e) {
                throw new CloudRuntimeException("Unable to persist the host_details key: " + detail.getKey() + " for host id: " + hostId, e);
            }
        }
        txn.commit();
    }

    @Override
    public DetailVO findDetail(final long hostId, final String name) {
        final SearchCriteria<DetailVO> sc = DetailSearch.create();
        sc.setParameters("hostId", hostId);
        sc.setParameters("name", name);

        final DetailVO detail = findOneIncludingRemovedBy(sc);
        if ("password".equals(name) && detail != null) {
            detail.setValue(DBEncryptionUtil.decrypt(detail.getValue()));
        }
        return detail;
    }

    @Override
    public void deleteDetails(final long hostId) {
        final SearchCriteria sc = HostSearch.create();
        sc.setParameters("hostId", hostId);

        final List<DetailVO> results = search(sc, null);
        for (final DetailVO result : results) {
            remove(result.getId());
        }
    }
}
