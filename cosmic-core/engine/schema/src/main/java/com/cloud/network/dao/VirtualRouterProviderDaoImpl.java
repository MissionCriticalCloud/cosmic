package com.cloud.network.dao;

import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.network.element.VirtualRouterProviderVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class VirtualRouterProviderDaoImpl extends GenericDaoBase<VirtualRouterProviderVO, Long> implements VirtualRouterProviderDao {
    final SearchBuilder<VirtualRouterProviderVO> AllFieldsSearch;

    public VirtualRouterProviderDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("nsp_id", AllFieldsSearch.entity().getNspId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("uuid", AllFieldsSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("type", AllFieldsSearch.entity().getType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("enabled", AllFieldsSearch.entity().isEnabled(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public VirtualRouterProviderVO findByNspIdAndType(final long nspId, final Type type) {
        final SearchCriteria<VirtualRouterProviderVO> sc = AllFieldsSearch.create();
        sc.setParameters("nsp_id", nspId);
        sc.setParameters("type", type);
        return findOneBy(sc);
    }

    @Override
    public List<VirtualRouterProviderVO> listByEnabledAndType(final boolean enabled, final Type type) {
        final SearchCriteria<VirtualRouterProviderVO> sc = AllFieldsSearch.create();
        sc.setParameters("enabled", enabled);
        sc.setParameters("type", type);
        return listBy(sc);
    }

    @Override
    public VirtualRouterProviderVO findByIdAndEnabledAndType(final long id, final boolean enabled, final Type type) {
        final SearchCriteria<VirtualRouterProviderVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", id);
        sc.setParameters("enabled", enabled);
        sc.setParameters("type", type);
        return findOneBy(sc);
    }

    @Override
    public List<VirtualRouterProviderVO> listByType(final Type type) {
        final SearchCriteria<VirtualRouterProviderVO> sc = AllFieldsSearch.create();
        sc.setParameters("type", type);
        return listBy(sc);
    }
}
