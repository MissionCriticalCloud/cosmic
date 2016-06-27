package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMComputeTagVO;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VMComputeTagDaoImpl extends GenericDaoBase<VMComputeTagVO, Long> implements VMComputeTagDao {

    protected SearchBuilder<VMComputeTagVO> VmIdSearch;

    public VMComputeTagDaoImpl() {
    }

    @PostConstruct
    public void init() {
        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmId", VmIdSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        VmIdSearch.done();
    }

    @Override
    public void persist(final long vmId, final List<String> computeTags) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        final SearchCriteria<VMComputeTagVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);
        expunge(sc);

        for (String tag : computeTags) {
            if (tag != null) {
                tag = tag.trim();
                if (tag.length() > 0) {
                    final VMComputeTagVO vo = new VMComputeTagVO(vmId, tag);
                    persist(vo);
                }
            }
        }
        txn.commit();
    }

    @Override
    public List<String> getComputeTags(final long vmId) {

        final SearchCriteria<VMComputeTagVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);

        final List<VMComputeTagVO> results = search(sc, null);
        final List<String> computeTags = new ArrayList<>(results.size());
        for (final VMComputeTagVO result : results) {
            computeTags.add(result.getComputeTag());
        }

        return computeTags;
    }
}
