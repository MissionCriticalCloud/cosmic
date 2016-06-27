package com.cloud.dc.dao;

import com.cloud.dc.PodVlanMapVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PodVlanMapDaoImpl extends GenericDaoBase<PodVlanMapVO, Long> implements PodVlanMapDao {

    protected SearchBuilder<PodVlanMapVO> PodSearch;
    protected SearchBuilder<PodVlanMapVO> VlanSearch;
    protected SearchBuilder<PodVlanMapVO> PodVlanSearch;

    public PodVlanMapDaoImpl() {
        PodSearch = createSearchBuilder();
        PodSearch.and("podId", PodSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        PodSearch.done();

        VlanSearch = createSearchBuilder();
        VlanSearch.and("vlanDbId", VlanSearch.entity().getVlanDbId(), SearchCriteria.Op.EQ);
        VlanSearch.done();

        PodVlanSearch = createSearchBuilder();
        PodVlanSearch.and("podId", PodVlanSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        PodVlanSearch.and("vlanDbId", PodVlanSearch.entity().getVlanDbId(), SearchCriteria.Op.EQ);
        PodVlanSearch.done();
    }

    @Override
    public List<PodVlanMapVO> listPodVlanMapsByPod(final long podId) {
        final SearchCriteria<PodVlanMapVO> sc = PodSearch.create();
        sc.setParameters("podId", podId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public PodVlanMapVO listPodVlanMapsByVlan(final long vlanDbId) {
        final SearchCriteria<PodVlanMapVO> sc = VlanSearch.create();
        sc.setParameters("vlanDbId", vlanDbId);
        return findOneBy(sc);
    }

    @Override
    public PodVlanMapVO findPodVlanMap(final long podId, final long vlanDbId) {
        final SearchCriteria<PodVlanMapVO> sc = PodVlanSearch.create();
        sc.setParameters("podId", podId);
        sc.setParameters("vlanDbId", vlanDbId);
        return findOneIncludingRemovedBy(sc);
    }
}
