package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.engine.datacenter.entity.api.db.DcDetailVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component(value = "EngineDcDetailsDao")
public class DcDetailsDaoImpl extends GenericDaoBase<DcDetailVO, Long> implements DcDetailsDao {
    protected final SearchBuilder<DcDetailVO> DcSearch;
    protected final SearchBuilder<DcDetailVO> DetailSearch;

    protected DcDetailsDaoImpl() {
        DcSearch = createSearchBuilder();
        DcSearch.and("dcId", DcSearch.entity().getDcId(), SearchCriteria.Op.EQ);
        DcSearch.done();

        DetailSearch = createSearchBuilder();
        DetailSearch.and("dcId", DetailSearch.entity().getDcId(), SearchCriteria.Op.EQ);
        DetailSearch.and("name", DetailSearch.entity().getName(), SearchCriteria.Op.EQ);
        DetailSearch.done();
    }

    @Override
    public Map<String, String> findDetails(final long dcId) {
        final SearchCriteria<DcDetailVO> sc = DcSearch.create();
        sc.setParameters("dcId", dcId);

        final List<DcDetailVO> results = search(sc, null);
        final Map<String, String> details = new HashMap<>(results.size());
        for (final DcDetailVO result : results) {
            details.put(result.getName(), result.getValue());
        }
        return details;
    }

    @Override
    public void persist(final long dcId, final Map<String, String> details) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<DcDetailVO> sc = DcSearch.create();
        sc.setParameters("dcId", dcId);
        expunge(sc);

        for (final Map.Entry<String, String> detail : details.entrySet()) {
            final DcDetailVO vo = new DcDetailVO(dcId, detail.getKey(), detail.getValue());
            persist(vo);
        }
        txn.commit();
    }

    @Override
    public DcDetailVO findDetail(final long dcId, final String name) {
        final SearchCriteria<DcDetailVO> sc = DetailSearch.create();
        sc.setParameters("dcId", dcId);
        sc.setParameters("name", name);

        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public void deleteDetails(final long dcId) {
        final SearchCriteria sc = DcSearch.create();
        sc.setParameters("dcId", dcId);

        final List<DcDetailVO> results = search(sc, null);
        for (final DcDetailVO result : results) {
            remove(result.getId());
        }
    }
}
