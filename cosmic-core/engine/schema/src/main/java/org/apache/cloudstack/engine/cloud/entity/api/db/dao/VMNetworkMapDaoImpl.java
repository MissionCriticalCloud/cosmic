package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.network.dao.NetworkDao;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMNetworkMapVO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VMNetworkMapDaoImpl extends GenericDaoBase<VMNetworkMapVO, Long> implements VMNetworkMapDao {

    protected SearchBuilder<VMNetworkMapVO> VmIdSearch;
    protected SearchBuilder<VMNetworkMapVO> VmNetworkSearch;

    @Inject
    protected NetworkDao _networkDao;

    public VMNetworkMapDaoImpl() {
    }

    @PostConstruct
    public void init() {
        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmId", VmIdSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        VmIdSearch.done();

        VmNetworkSearch = createSearchBuilder();
        VmNetworkSearch.and("vmId", VmNetworkSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        VmNetworkSearch.and("networkId", VmNetworkSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        VmNetworkSearch.done();
    }

    @Override
    public void persist(final long vmId, final List<Long> networks) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        final SearchCriteria<VMNetworkMapVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);
        expunge(sc);

        for (final Long networkId : networks) {
            final VMNetworkMapVO vo = new VMNetworkMapVO(vmId, networkId);
            persist(vo);
        }

        txn.commit();
    }

    @Override
    public List<Long> getNetworks(final long vmId) {

        final SearchCriteria<VMNetworkMapVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);

        final List<VMNetworkMapVO> results = search(sc, null);
        final List<Long> networks = new ArrayList<>(results.size());
        for (final VMNetworkMapVO result : results) {
            networks.add(result.getNetworkId());
        }

        return networks;
    }

    @Override
    public VMNetworkMapVO findByVmAndNetworkId(final long vmId, final long networkId) {

        final SearchCriteria<VMNetworkMapVO> sc = VmNetworkSearch.create();
        sc.setParameters("vmId", vmId);
        sc.setParameters("networkId", networkId);
        final VMNetworkMapVO network = findOneBy(sc);

        return network;
    }
}
