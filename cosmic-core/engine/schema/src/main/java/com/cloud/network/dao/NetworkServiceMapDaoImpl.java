package com.cloud.network.dao;

import com.cloud.exception.UnsupportedServiceException;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkServiceMapDaoImpl extends GenericDaoBase<NetworkServiceMapVO, Long> implements NetworkServiceMapDao {
    final SearchBuilder<NetworkServiceMapVO> AllFieldsSearch;
    final SearchBuilder<NetworkServiceMapVO> MultipleServicesSearch;
    final GenericSearchBuilder<NetworkServiceMapVO, String> DistinctProvidersSearch;

    protected NetworkServiceMapDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("service", AllFieldsSearch.entity().getService(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("provider", AllFieldsSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        MultipleServicesSearch = createSearchBuilder();
        MultipleServicesSearch.and("networkId", MultipleServicesSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.and("service", MultipleServicesSearch.entity().getService(), SearchCriteria.Op.IN);
        MultipleServicesSearch.and("provider", MultipleServicesSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.done();

        DistinctProvidersSearch = createSearchBuilder(String.class);
        DistinctProvidersSearch.and("networkId", DistinctProvidersSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.and("provider", DistinctProvidersSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.and("service", DistinctProvidersSearch.entity().getService(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.selectFields(DistinctProvidersSearch.entity().getProvider());
        DistinctProvidersSearch.done();
    }

    @Override
    public boolean areServicesSupportedInNetwork(final long networkId, final Service... services) {
        final SearchCriteria<NetworkServiceMapVO> sc = MultipleServicesSearch.create();
        sc.setParameters("networkId", networkId);

        if (services != null) {
            final String[] servicesStr = new String[services.length];

            int i = 0;
            for (final Service service : services) {
                servicesStr[i] = service.getName();
                i++;
            }

            sc.setParameters("service", (Object[]) servicesStr);
        }

        final List<NetworkServiceMapVO> networkServices = listBy(sc);

        if (services != null) {
            if (networkServices.size() == services.length) {
                return true;
            }
        } else if (!networkServices.isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canProviderSupportServiceInNetwork(final long networkId, final Service service, final Provider provider) {
        final SearchCriteria<NetworkServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("service", service.getName());
        sc.setParameters("provider", provider.getName());
        if (findOneBy(sc) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<NetworkServiceMapVO> getServicesInNetwork(final long networkId) {
        final SearchCriteria<NetworkServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        return listBy(sc);
    }

    @Override
    public String getProviderForServiceInNetwork(final long networkId, final Service service) {
        final SearchCriteria<NetworkServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("service", service.getName());
        final NetworkServiceMapVO ntwkSvc = findOneBy(sc);
        if (ntwkSvc == null) {
            throw new UnsupportedServiceException("Service " + service.getName() + " is not supported in the network id=" + networkId);
        }

        return ntwkSvc.getProvider();
    }

    @Override
    public void deleteByNetworkId(final long networkId) {
        final SearchCriteria<NetworkServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        remove(sc);
    }

    @Override
    public List<String> getDistinctProviders(final long networkId) {
        final SearchCriteria<String> sc = DistinctProvidersSearch.create();
        sc.setParameters("networkId", networkId);
        final List<String> results = customSearch(sc, null);
        return results;
    }

    @Override
    public String isProviderForNetwork(final long networkId, final Provider provider) {
        final SearchCriteria<String> sc = DistinctProvidersSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("provider", provider.getName());
        final List<String> results = customSearch(sc, null);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    @Override
    public List<String> getProvidersForServiceInNetwork(final long networkId, final Service service) {
        final SearchCriteria<String> sc = DistinctProvidersSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("service", service.getName());
        return customSearch(sc, null);
    }

    protected List<String> getServicesForProviderInNetwork(final long networkId, final Provider provider) {
        final List<String> services = new ArrayList<>();
        final SearchCriteria<NetworkServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("provider", provider.getName());
        final List<NetworkServiceMapVO> map = listBy(sc);
        for (final NetworkServiceMapVO instance : map) {
            services.add(instance.getService());
        }

        return services;
    }
}
