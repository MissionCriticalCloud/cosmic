package com.cloud.domain.dao;

import com.cloud.domain.DomainVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Set;

public interface DomainDao extends GenericDao<DomainVO, Long> {
    public DomainVO create(DomainVO domain);

    public DomainVO findDomainByPath(String domainPath);

    public boolean isChildDomain(Long parentId, Long childId);

    DomainVO findImmediateChildForParent(Long parentId);

    List<DomainVO> findImmediateChildrenForParent(Long parentId);

    List<DomainVO> findAllChildren(String path, Long parentId);

    List<DomainVO> findInactiveDomains();

    Set<Long> getDomainParentIds(long domainId);

    List<Long> getDomainChildrenIds(String path);
}
