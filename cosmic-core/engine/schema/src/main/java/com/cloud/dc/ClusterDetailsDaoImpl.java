package com.cloud.dc;

import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.ConfigKey.Scope;
import com.cloud.framework.config.ScopedConfigStorage;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterDetailsDaoImpl extends GenericDaoBase<ClusterDetailsVO, Long> implements ClusterDetailsDao, ScopedConfigStorage {

    private final SearchBuilder<ClusterDetailsVO> _clusterSearch;
    private final SearchBuilder<ClusterDetailsVO> _detailSearch;

    protected ClusterDetailsDaoImpl() {
        _clusterSearch = createSearchBuilder();
        _clusterSearch.and("clusterId", _clusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        _clusterSearch.done();

        _detailSearch = createSearchBuilder();
        _detailSearch.and("clusterId", _detailSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        _detailSearch.and("name", _detailSearch.entity().getName(), SearchCriteria.Op.EQ);
        _detailSearch.done();
    }

    @Override
    public Map<String, String> findDetails(final long clusterId) {
        final SearchCriteria<ClusterDetailsVO> sc = _clusterSearch.create();
        sc.setParameters("clusterId", clusterId);

        final List<ClusterDetailsVO> results = search(sc, null);
        final Map<String, String> details = new HashMap<>(results.size());

        results.forEach(result -> {
            final String value = "password".equals(result.getName())
                    ? DBEncryptionUtil.decrypt(result.getValue())
                    : result.getValue();

            details.put(result.getName(), value);
        });

        return details;
    }

    @Override
    public void persist(final long clusterId, final Map<String, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<ClusterDetailsVO> sc = _clusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        expunge(sc);

        details.entrySet().forEach(detail -> {
            final String value = "password".equals(detail.getKey())
                    ? DBEncryptionUtil.encrypt(detail.getValue())
                    : detail.getValue();

            final ClusterDetailsVO vo = new ClusterDetailsVO(clusterId, detail.getKey(), value);
            persist(vo);
        });
        txn.commit();
    }

    @Override
    public void persist(final long clusterId, final String name, final String value) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<ClusterDetailsVO> sc = _detailSearch.create();
        sc.setParameters("clusterId", clusterId);
        sc.setParameters("name", name);
        expunge(sc);

        final ClusterDetailsVO vo = new ClusterDetailsVO(clusterId, name, value);
        persist(vo);
        txn.commit();
    }

    @Override
    public ClusterDetailsVO findDetail(final long clusterId, final String name) {
        final String correctedName = correctName(name);

        final SearchCriteria<ClusterDetailsVO> sc = _detailSearch.create();
        sc.setParameters("clusterId", clusterId);
        sc.setParameters("name", correctedName);

        ClusterDetailsVO detail = findOneIncludingRemovedBy(sc);
        if (detail != null && "password".equals(correctedName)) {
            detail.setValue(DBEncryptionUtil.decrypt(detail.getValue()));
        }
        if ((detail == null) && ("cpuOvercommitRatio".equals(correctedName) || "memoryOvercommitRatio".equals(correctedName))) {
            this.persist(clusterId, correctedName, "1");
            detail = findOneIncludingRemovedBy(sc);
        }
        return detail;
    }

    /*
     * This is a temporary fix to support listing/updating configuration API for CPU and memory overprovisioning ratios
     */
    private String correctName(final String name) {
        String correctedName = name;
        if ("cpu.overprovisioning.factor".equalsIgnoreCase(name)) {
            correctedName = "cpuOvercommitRatio";
        } else if ("mem.overprovisioning.factor".equalsIgnoreCase(name)) {
            correctedName = "memoryOvercommitRatio";
        }
        return correctedName;
    }

    @Override
    public void deleteDetails(final long clusterId) {
        final SearchCriteria<ClusterDetailsVO> sc = _clusterSearch.create();
        sc.setParameters("clusterId", clusterId);

        final List<ClusterDetailsVO> results = search(sc, null);
        results.forEach(result -> remove(result.getId()));
    }

    @Override
    public Scope getScope() {
        return ConfigKey.Scope.Cluster;
    }

    @Override
    public String getConfigValue(final long id, final ConfigKey<?> key) {
        final ClusterDetailsVO vo = findDetail(id, key.key());

        return (vo != null)
                ? vo.getValue()
                : null;
    }
}
