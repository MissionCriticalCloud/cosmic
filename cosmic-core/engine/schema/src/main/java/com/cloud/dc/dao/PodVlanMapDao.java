package com.cloud.dc.dao;

import com.cloud.dc.PodVlanMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PodVlanMapDao extends GenericDao<PodVlanMapVO, Long> {

    public List<PodVlanMapVO> listPodVlanMapsByPod(long podId);

    public PodVlanMapVO listPodVlanMapsByVlan(long vlanDbId);

    public PodVlanMapVO findPodVlanMap(long podId, long vlanDbId);
}
