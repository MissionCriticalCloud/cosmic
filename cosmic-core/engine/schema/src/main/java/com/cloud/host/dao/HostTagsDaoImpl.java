package com.cloud.host.dao;

import com.cloud.host.HostTagVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.TransactionLegacy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class HostTagsDaoImpl extends GenericDaoBase<HostTagVO, Long> implements HostTagsDao {
    protected final SearchBuilder<HostTagVO> HostSearch;
    protected final GenericSearchBuilder<HostTagVO, String> DistinctImplictTagsSearch;

    public HostTagsDaoImpl() {
        HostSearch = createSearchBuilder();
        HostSearch.and("hostId", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.done();

        DistinctImplictTagsSearch = createSearchBuilder(String.class);
        DistinctImplictTagsSearch.select(null, Func.DISTINCT, DistinctImplictTagsSearch.entity().getTag());
        DistinctImplictTagsSearch.and("hostIds", DistinctImplictTagsSearch.entity().getHostId(), SearchCriteria.Op.IN);
        DistinctImplictTagsSearch.and("implicitTags", DistinctImplictTagsSearch.entity().getTag(), SearchCriteria.Op.IN);
        DistinctImplictTagsSearch.done();
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

    @Override
    public List<String> getDistinctImplicitHostTags(final List<Long> hostIds, final String[] implicitHostTags) {
        final SearchCriteria<String> sc = DistinctImplictTagsSearch.create();
        sc.setParameters("hostIds", hostIds.toArray(new Object[hostIds.size()]));
        sc.setParameters("implicitTags", (Object[]) implicitHostTags);
        return customSearch(sc, null);
    }

    @Override
    public void deleteTags(final long hostId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<HostTagVO> sc = HostSearch.create();
        sc.setParameters("hostId", hostId);
        expunge(sc);
        txn.commit();
    }
}
