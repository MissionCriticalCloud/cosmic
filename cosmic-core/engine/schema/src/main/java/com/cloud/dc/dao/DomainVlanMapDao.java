package com.cloud.dc.dao;

import com.cloud.dc.DomainVlanMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DomainVlanMapDao extends GenericDao<DomainVlanMapVO, Long> {
    public List<DomainVlanMapVO> listDomainVlanMapsByDomain(long domainId);

    public List<DomainVlanMapVO> listDomainVlanMapsByVlan(long vlanDbId);

    public DomainVlanMapVO findDomainVlanMap(long domainId, long vlanDbId);
}
