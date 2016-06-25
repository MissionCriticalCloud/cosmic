//

//

package com.cloud.network.dao;

import com.cloud.network.NiciraNvpDeviceVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NiciraNvpDaoImpl extends GenericDaoBase<NiciraNvpDeviceVO, Long> implements NiciraNvpDao {

    protected final SearchBuilder<NiciraNvpDeviceVO> physicalNetworkIdSearch;

    public NiciraNvpDaoImpl() {
        physicalNetworkIdSearch = createSearchBuilder();
        physicalNetworkIdSearch.and("physicalNetworkId", physicalNetworkIdSearch.entity().getPhysicalNetworkId(), Op.EQ);
        physicalNetworkIdSearch.done();
    }

    @Override
    public List<NiciraNvpDeviceVO> listByPhysicalNetwork(final long physicalNetworkId) {
        final SearchCriteria<NiciraNvpDeviceVO> sc = physicalNetworkIdSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return search(sc, null);
    }
}
