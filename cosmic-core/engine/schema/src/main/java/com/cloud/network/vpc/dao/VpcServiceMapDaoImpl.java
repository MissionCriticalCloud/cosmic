package com.cloud.network.vpc.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.dao.NetworkServiceMapVO;
import com.cloud.network.vpc.VpcServiceMapVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class VpcServiceMapDaoImpl extends GenericDaoBase<VpcServiceMapVO, Long> implements VpcServiceMapDao {

    private final GenericSearchBuilder<VpcServiceMapVO, String> _distinctProvidersSearch;

    protected VpcServiceMapDaoImpl() {
        _distinctProvidersSearch = createSearchBuilder(String.class);
        _distinctProvidersSearch.and("vpcId", _distinctProvidersSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        _distinctProvidersSearch.and("provider", _distinctProvidersSearch.entity().getProvider(), SearchCriteria.Op.EQ);
        _distinctProvidersSearch.selectFields(_distinctProvidersSearch.entity().getProvider());
        _distinctProvidersSearch.done();
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteByVpcId(final long vpcId) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<String> getDistinctProviders(final long vpcId) {
        final SearchCriteria<String> sc = _distinctProvidersSearch.create();
        sc.setParameters("vpcId", vpcId);

        return customSearch(sc, null);
    }

    @Override
    public String isProviderForVpc(final long vpcId, final Provider provider) {
        // TODO Auto-generated method stub
        return null;
    }
}
