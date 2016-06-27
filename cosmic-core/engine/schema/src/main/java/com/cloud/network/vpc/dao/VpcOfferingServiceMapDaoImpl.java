package com.cloud.network.vpc.dao;

import com.cloud.network.Network.Service;
import com.cloud.network.vpc.VpcOfferingServiceMapVO;
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
public class VpcOfferingServiceMapDaoImpl extends GenericDaoBase<VpcOfferingServiceMapVO, Long> implements VpcOfferingServiceMapDao {
    final SearchBuilder<VpcOfferingServiceMapVO> AllFieldsSearch;
    final SearchBuilder<VpcOfferingServiceMapVO> MultipleServicesSearch;
    final GenericSearchBuilder<VpcOfferingServiceMapVO, String> ServicesSearch;

    protected VpcOfferingServiceMapDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("vpcOffId", AllFieldsSearch.entity().getVpcOfferingId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("service", AllFieldsSearch.entity().getService(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("provider", AllFieldsSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        MultipleServicesSearch = createSearchBuilder();
        MultipleServicesSearch.and("vpcOffId", MultipleServicesSearch.entity().getVpcOfferingId(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.and("service", MultipleServicesSearch.entity().getService(), SearchCriteria.Op.IN);
        MultipleServicesSearch.and("provider", MultipleServicesSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        MultipleServicesSearch.done();

        ServicesSearch = createSearchBuilder(String.class);
        ServicesSearch.and("offeringId", ServicesSearch.entity().getVpcOfferingId(), SearchCriteria.Op.EQ);
        ServicesSearch.select(null, Func.DISTINCT, ServicesSearch.entity().getService());
        ServicesSearch.done();
    }

    @Override
    public List<VpcOfferingServiceMapVO> listByVpcOffId(final long vpcOffId) {
        final SearchCriteria<VpcOfferingServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcOffId", vpcOffId);
        return listBy(sc);
    }

    @Override
    public boolean areServicesSupportedByNetworkOffering(final long networkOfferingId, final Service... services) {
        final SearchCriteria<VpcOfferingServiceMapVO> sc = MultipleServicesSearch.create();
        sc.setParameters("vpcOffId", networkOfferingId);

        if (services != null) {
            final String[] servicesStr = new String[services.length];

            int i = 0;
            for (final Service service : services) {
                servicesStr[i] = service.getName();
                i++;
            }

            sc.setParameters("service", (Object[]) servicesStr);
        }

        final List<VpcOfferingServiceMapVO> offeringServices = listBy(sc);

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
    public List<String> listServicesForVpcOffering(final long offId) {
        final SearchCriteria<String> sc = ServicesSearch.create();
        sc.setParameters("offeringId", offId);
        return customSearch(sc, null);
    }

    @Override
    public VpcOfferingServiceMapVO findByServiceProviderAndOfferingId(final String service, final String provider, final long vpcOfferingId) {
        final SearchCriteria<VpcOfferingServiceMapVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcOffId", vpcOfferingId);
        sc.setParameters("service", service);
        sc.setParameters("provider", provider);

        return findOneBy(sc);
    }
}
