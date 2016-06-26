package com.cloud.offerings.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.offerings.NetworkOfferingServiceMapVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkOfferingServiceMapDaoImpl extends GenericDaoBase<NetworkOfferingServiceMapVO, Long> implements NetworkOfferingServiceMapDao {

    final SearchBuilder<NetworkOfferingServiceMapVO> AllFieldsSearch;
    final SearchBuilder<NetworkOfferingServiceMapVO> MultipleServicesSearch;
    final GenericSearchBuilder<NetworkOfferingServiceMapVO, String> ProvidersSearch;
    final GenericSearchBuilder<NetworkOfferingServiceMapVO, String> ServicesSearch;
    final GenericSearchBuilder<NetworkOfferingServiceMapVO, String> DistinctProvidersSearch;

    protected NetworkOfferingServiceMapDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("networkOfferingId", AllFieldsSearch.entity().getNetworkOfferingId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("service", AllFieldsSearch.entity().getService(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("provider", AllFieldsSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        MultipleServicesSearch = createSearchBuilder();
        MultipleServicesSearch.and("networkOfferingId", MultipleServicesSearch.entity().getNetworkOfferingId(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.and("service", MultipleServicesSearch.entity().getService(), SearchCriteria.Op.IN);
        MultipleServicesSearch.and("provider", MultipleServicesSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.done();

        ProvidersSearch = createSearchBuilder(String.class);
        ProvidersSearch.and("networkOfferingId", ProvidersSearch.entity().getNetworkOfferingId(), SearchCriteria.Op.EQ);
        ProvidersSearch.and("service", ProvidersSearch.entity().getService(), SearchCriteria.Op.EQ);
        ProvidersSearch.select(null, Func.DISTINCT, ProvidersSearch.entity().getProvider());
        ProvidersSearch.done();

        ServicesSearch = createSearchBuilder(String.class);
        ServicesSearch.and("networkOfferingId", ServicesSearch.entity().getNetworkOfferingId(), SearchCriteria.Op.EQ);
        ServicesSearch.select(null, Func.DISTINCT, ServicesSearch.entity().getService());
        ServicesSearch.done();

        DistinctProvidersSearch = createSearchBuilder(String.class);
        DistinctProvidersSearch.and("offId", DistinctProvidersSearch.entity().getNetworkOfferingId(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.and("provider", DistinctProvidersSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.selectFields(DistinctProvidersSearch.entity().getProvider());
        DistinctProvidersSearch.done();
    }

    @Override
    public boolean areServicesSupportedByNetworkOffering(final long networkOfferingId, final Service... services) {
        final SearchCriteria<NetworkOfferingServiceMapVO> sc = MultipleServicesSearch.create();
        sc.setParameters("networkOfferingId", networkOfferingId);

        if (services != null) {
            final String[] servicesStr = new String[services.length];

            int i = 0;
            for (final Service service : services) {
                servicesStr[i] = service.getName();
                i++;
            }

            sc.setParameters("service", (Object[]) servicesStr);
        }

        final List<NetworkOfferingServiceMapVO> offeringServices = listBy(sc);

        if (services != null) {
            if (offeringServices.size() == services.length) {
                return true;
            }
        } else if (!offeringServices.isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public List<NetworkOfferingServiceMapVO> listByNetworkOfferingId(final long networkOfferingId) {
        final SearchCriteria<NetworkOfferingServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkOfferingId", networkOfferingId);
        return listBy(sc);
    }

    @Override
    public void deleteByOfferingId(final long networkOfferingId) {
        final SearchCriteria<NetworkOfferingServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkOfferingId", networkOfferingId);
        remove(sc);
    }

    @Override
    public List<String> listProvidersForServiceForNetworkOffering(final long networkOfferingId, final Service service) {
        final SearchCriteria<String> sc = ProvidersSearch.create();

        sc.setParameters("networkOfferingId", networkOfferingId);
        sc.setParameters("service", service.getName());

        return customSearch(sc, null);
    }

    @Override
    public boolean isProviderForNetworkOffering(final long networkOfferingId, final Provider provider) {
        final SearchCriteria<NetworkOfferingServiceMapVO> sc = AllFieldsSearch.create();

        sc.setParameters("networkOfferingId", networkOfferingId);
        sc.setParameters("provider", provider.getName());

        if (findOneBy(sc) != null) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> listServicesForNetworkOffering(final long networkOfferingId) {
        final SearchCriteria<String> sc = ServicesSearch.create();
        sc.setParameters("networkOfferingId", networkOfferingId);
        return customSearch(sc, null);
    }

    @Override
    public List<String> getDistinctProviders(final long offId) {
        final SearchCriteria<String> sc = DistinctProvidersSearch.create();
        sc.setParameters("offId", offId);
        final List<String> results = customSearch(sc, null);
        return results;
    }

    @Override
    public NetworkOfferingServiceMapVO persist(final NetworkOfferingServiceMapVO entity) {
        final SearchCriteria<NetworkOfferingServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkOfferingId", entity.getNetworkOfferingId());
        sc.setParameters("service", entity.getService());
        sc.setParameters("provider", entity.getProvider());
        final NetworkOfferingServiceMapVO mappingInDb = findOneBy(sc);
        return mappingInDb != null ? mappingInDb : super.persist(entity);
    }
}
