package com.cloud.network.vpc.dao;

import com.cloud.exception.UnsupportedServiceException;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.dao.NetworkServiceMapVO;
import com.cloud.network.vpc.VpcServiceMapVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class VpcServiceMapDaoImpl extends GenericDaoBase<VpcServiceMapVO, Long> implements VpcServiceMapDao {
    final SearchBuilder<VpcServiceMapVO> AllFieldsSearch;
    final SearchBuilder<VpcServiceMapVO> MultipleServicesSearch;
    final GenericSearchBuilder<VpcServiceMapVO, String> DistinctProvidersSearch;

    protected VpcServiceMapDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("service", AllFieldsSearch.entity().getService(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("provider", AllFieldsSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        MultipleServicesSearch = createSearchBuilder();
        MultipleServicesSearch.and("vpcId", MultipleServicesSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.and("service", MultipleServicesSearch.entity().getService(), SearchCriteria.Op.IN);
        MultipleServicesSearch.and("provider", MultipleServicesSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.done();

        DistinctProvidersSearch = createSearchBuilder(String.class);
        DistinctProvidersSearch.and("vpcId", DistinctProvidersSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.and("provider", DistinctProvidersSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        DistinctProvidersSearch.selectFields(DistinctProvidersSearch.entity().getProvider());
        DistinctProvidersSearch.done();
    }

    @Override
    public boolean areServicesSupportedInVpc(final long vpcId, final Service... services) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canProviderSupportServiceInVpc(final long vpcId, final Service service, final Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<NetworkServiceMapVO> getServicesInVpc(final long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProviderForServiceInVpc(final long vpcId, final Service service) {
        final SearchCriteria<VpcServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        sc.setParameters("service", service.getName());
        final VpcServiceMapVO ntwkSvc = findOneBy(sc);
        if (ntwkSvc == null) {
            throw new UnsupportedServiceException("Service " + service.getName() + " is not supported in the vpc id=" + vpcId);
        }

        return ntwkSvc.getProvider();
    }

    @Override
    public void deleteByVpcId(final long vpcId) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getDistinctProviders(final long vpcId) {
        final SearchCriteria<String> sc = DistinctProvidersSearch.create();
        sc.setParameters("vpcId", vpcId);
        final List<String> results = customSearch(sc, null);
        return results;
    }

    @Override
    public String isProviderForVpc(final long vpcId, final Provider provider) {
        // TODO Auto-generated method stub
        return null;
    }
}
