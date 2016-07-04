package org.apache.cloudstack.storage.image.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.datastore.db.ImageStoreDao;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ImageStoreDaoImpl extends GenericDaoBase<ImageStoreVO, Long> implements ImageStoreDao {
    private SearchBuilder<ImageStoreVO> nameSearch;
    private SearchBuilder<ImageStoreVO> providerSearch;
    private SearchBuilder<ImageStoreVO> regionSearch;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        nameSearch = createSearchBuilder();
        nameSearch.and("name", nameSearch.entity().getName(), SearchCriteria.Op.EQ);
        nameSearch.and("role", nameSearch.entity().getRole(), SearchCriteria.Op.EQ);
        nameSearch.done();

        providerSearch = createSearchBuilder();
        providerSearch.and("providerName", providerSearch.entity().getProviderName(), SearchCriteria.Op.EQ);
        providerSearch.and("role", providerSearch.entity().getRole(), SearchCriteria.Op.EQ);
        providerSearch.done();

        regionSearch = createSearchBuilder();
        regionSearch.and("scope", regionSearch.entity().getScope(), SearchCriteria.Op.EQ);
        regionSearch.and("role", regionSearch.entity().getRole(), SearchCriteria.Op.EQ);
        regionSearch.done();

        return true;
    }

    @Override
    public ImageStoreVO findByName(final String name) {
        final SearchCriteria<ImageStoreVO> sc = nameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    @Override
    public List<ImageStoreVO> findByProvider(final String provider) {
        final SearchCriteria<ImageStoreVO> sc = providerSearch.create();
        sc.setParameters("providerName", provider);
        sc.setParameters("role", DataStoreRole.Image);
        return listBy(sc);
    }

    @Override
    public List<ImageStoreVO> findByScope(final ZoneScope scope) {
        final SearchCriteria<ImageStoreVO> sc = createSearchCriteria();
        sc.addAnd("role", SearchCriteria.Op.EQ, DataStoreRole.Image);
        if (scope.getScopeId() != null) {
            final SearchCriteria<ImageStoreVO> scc = createSearchCriteria();
            scc.addOr("scope", SearchCriteria.Op.EQ, ScopeType.REGION);
            scc.addOr("dcId", SearchCriteria.Op.EQ, scope.getScopeId());
            sc.addAnd("scope", SearchCriteria.Op.SC, scc);
        }
        // we should return all image stores if cross-zone scope is passed
        // (scopeId = null)
        return listBy(sc);
    }

    @Override
    public List<ImageStoreVO> findRegionImageStores() {
        final SearchCriteria<ImageStoreVO> sc = regionSearch.create();
        sc.setParameters("scope", ScopeType.REGION);
        sc.setParameters("role", DataStoreRole.Image);
        return listBy(sc);
    }

    @Override
    public List<ImageStoreVO> findImageCacheByScope(final ZoneScope scope) {
        final SearchCriteria<ImageStoreVO> sc = createSearchCriteria();
        sc.addAnd("role", SearchCriteria.Op.EQ, DataStoreRole.ImageCache);
        if (scope.getScopeId() != null) {
            sc.addAnd("scope", SearchCriteria.Op.EQ, ScopeType.ZONE);
            sc.addAnd("dcId", SearchCriteria.Op.EQ, scope.getScopeId());
        }
        return listBy(sc);
    }

    @Override
    public List<ImageStoreVO> listImageStores() {
        final SearchCriteria<ImageStoreVO> sc = createSearchCriteria();
        sc.addAnd("role", SearchCriteria.Op.EQ, DataStoreRole.Image);
        return listBy(sc);
    }

    @Override
    public List<ImageStoreVO> listImageCacheStores() {
        final SearchCriteria<ImageStoreVO> sc = createSearchCriteria();
        sc.addAnd("role", SearchCriteria.Op.EQ, DataStoreRole.ImageCache);
        return listBy(sc);
    }
}
