package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMRootDiskTagVO;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VMRootDiskTagDaoImpl extends GenericDaoBase<VMRootDiskTagVO, Long> implements VMRootDiskTagDao {

    protected SearchBuilder<VMRootDiskTagVO> VmIdSearch;

    public VMRootDiskTagDaoImpl() {
    }

    @PostConstruct
    public void init() {
        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmId", VmIdSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        VmIdSearch.done();
    }

    @Override
    public void persist(final long vmId, final List<String> rootDiskTags) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        final SearchCriteria<VMRootDiskTagVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);
        expunge(sc);

        for (String tag : rootDiskTags) {
            if (tag != null) {
                tag = tag.trim();
                if (tag.length() > 0) {
                    final VMRootDiskTagVO vo = new VMRootDiskTagVO(vmId, tag);
                    persist(vo);
                }
            }
        }
        txn.commit();
    }

    @Override
    public List<String> getRootDiskTags(final long vmId) {
        final SearchCriteria<VMRootDiskTagVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);

        final List<VMRootDiskTagVO> results = search(sc, null);
        final List<String> computeTags = new ArrayList<>(results.size());
        for (final VMRootDiskTagVO result : results) {
            computeTags.add(result.getRootDiskTag());
        }
        return computeTags;
    }
}
