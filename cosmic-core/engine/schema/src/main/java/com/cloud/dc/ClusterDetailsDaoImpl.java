package com.cloud.dc;

import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.ConfigKey.Scope;
import org.apache.cloudstack.framework.config.ScopedConfigStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterDetailsDaoImpl extends GenericDaoBase<ClusterDetailsVO, Long> implements ClusterDetailsDao, ScopedConfigStorage {
    protected final SearchBuilder<ClusterDetailsVO> ClusterSearch;
    protected final SearchBuilder<ClusterDetailsVO> DetailSearch;

    protected ClusterDetailsDaoImpl() {
        ClusterSearch = createSearchBuilder();
        ClusterSearch.and("clusterId", ClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        ClusterSearch.done();

        DetailSearch = createSearchBuilder();
        DetailSearch.and("clusterId", DetailSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        DetailSearch.and("name", DetailSearch.entity().getName(), SearchCriteria.Op.EQ);
        DetailSearch.done();
    }

    @Override
    public Map<String, String> findDetails(final long clusterId) {
        final SearchCriteria<ClusterDetailsVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);

        final List<ClusterDetailsVO> results = search(sc, null);
        final Map<String, String> details = new HashMap<>(results.size());
        for (final ClusterDetailsVO result : results) {
            if ("password".equals(result.getName())) {
                details.put(result.getName(), DBEncryptionUtil.decrypt(result.getValue()));
            } else {
                details.put(result.getName(), result.getValue());
            }
        }
        return details;
    }

    @Override
    public void persist(final long clusterId, final Map<String, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<ClusterDetailsVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        expunge(sc);

        for (final Map.Entry<String, String> detail : details.entrySet()) {
            String value = detail.getValue();
            if ("password".equals(detail.getKey())) {
                value = DBEncryptionUtil.encrypt(value);
            }
            final ClusterDetailsVO vo = new ClusterDetailsVO(clusterId, detail.getKey(), value);
            persist(vo);
        }
        txn.commit();
    }

    @Override
    public void persist(final long clusterId, final String name, final String value) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<ClusterDetailsVO> sc = DetailSearch.create();
        sc.setParameters("clusterId", clusterId);
        sc.setParameters("name", name);
        expunge(sc);

        final ClusterDetailsVO vo = new ClusterDetailsVO(clusterId, name, value);
        persist(vo);
        txn.commit();
    }

    @Override
    public ClusterDetailsVO findDetail(final long clusterId, String name) {
        final SearchCriteria<ClusterDetailsVO> sc = DetailSearch.create();
        // This is temporary fix to support list/update configuration api for cpu and memory overprovisioning ratios
        if (name.equalsIgnoreCase("cpu.overprovisioning.factor")) {
            name = "cpuOvercommitRatio";
        }
        if (name.equalsIgnoreCase("mem.overprovisioning.factor")) {
            name = "memoryOvercommitRatio";
        }
        sc.setParameters("clusterId", clusterId);
        sc.setParameters("name", name);

        final ClusterDetailsVO detail = findOneIncludingRemovedBy(sc);
        if ("password".equals(name) && detail != null) {
            detail.setValue(DBEncryptionUtil.decrypt(detail.getValue()));
        }
        return detail;
    }

    @Override
    public void deleteDetails(final long clusterId) {
        final SearchCriteria<ClusterDetailsVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);

        final List<ClusterDetailsVO> results = search(sc, null);
        for (final ClusterDetailsVO result : results) {
            remove(result.getId());
        }
    }

    @Override
    public String getVmwareDcName(final Long clusterId) {
        String dcName = null;
        final String url = findDetail(clusterId, "url").getValue();
        final String[] tokens = url.split("/"); // Cluster URL format is 'http://vcenter/dc/cluster'
        if (tokens != null && tokens.length > 3) {
            dcName = tokens[3];
        }
        return dcName;
    }

    @Override
    public Scope getScope() {
        return ConfigKey.Scope.Cluster;
    }

    @Override
    public String getConfigValue(final long id, final ConfigKey<?> key) {
        final ClusterDetailsVO vo = findDetail(id, key.key());
        return vo == null ? null : vo.getValue();
    }
}
