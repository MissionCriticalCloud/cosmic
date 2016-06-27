package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.host.HostTagVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component(value = "EngineHostTagsDao")
public class HostTagsDaoImpl extends GenericDaoBase<HostTagVO, Long> implements HostTagsDao {
    protected final SearchBuilder<HostTagVO> HostSearch;

    protected HostTagsDaoImpl() {
        HostSearch = createSearchBuilder();
        HostSearch.and("hostId", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();
    }

    @Override
    public void persist(final long hostId, final List<String> hostTags) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        final SearchCriteria<HostTagVO> sc = HostSearch.create();
        sc.setParameters("hostId", hostId);
        expunge(sc);

        for (String tag : hostTags) {
            tag = tag.trim();
            if (tag.length() > 0) {
                final HostTagVO vo = new HostTagVO(hostId, tag);
                persist(vo);
            }
        }
        txn.commit();
    }

    @Override
    public List<String> gethostTags(final long hostId) {
        final SearchCriteria<HostTagVO> sc = HostSearch.create();
        sc.setParameters("hostId", hostId);

        final List<HostTagVO> results = search(sc, null);
        final List<String> hostTags = new ArrayList<>(results.size());
        for (final HostTagVO result : results) {
            hostTags.add(result.getTag());
        }

        return hostTags;
    }
}
