package org.apache.cloudstack.region;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PortableIpDaoImpl extends GenericDaoBase<PortableIpVO, Long> implements PortableIpDao {

    private final SearchBuilder<PortableIpVO> listByRegionIDSearch;
    private final SearchBuilder<PortableIpVO> listByRangeIDSearch;
    private final SearchBuilder<PortableIpVO> listByRangeIDAndStateSearch;
    private final SearchBuilder<PortableIpVO> listByRegionIDAndStateSearch;
    private final SearchBuilder<PortableIpVO> findByIpAddressSearch;

    public PortableIpDaoImpl() {
        listByRegionIDSearch = createSearchBuilder();
        listByRegionIDSearch.and("regionId", listByRegionIDSearch.entity().getRegionId(), SearchCriteria.Op.EQ);
        listByRegionIDSearch.done();

        listByRangeIDSearch = createSearchBuilder();
        listByRangeIDSearch.and("rangeId", listByRangeIDSearch.entity().getRangeId(), SearchCriteria.Op.EQ);
        listByRangeIDSearch.done();

        listByRangeIDAndStateSearch = createSearchBuilder();
        listByRangeIDAndStateSearch.and("rangeId", listByRangeIDAndStateSearch.entity().getRangeId(), SearchCriteria.Op.EQ);
        listByRangeIDAndStateSearch.and("state", listByRangeIDAndStateSearch.entity().getState(), SearchCriteria.Op.EQ);
        listByRangeIDAndStateSearch.done();

        listByRegionIDAndStateSearch = createSearchBuilder();
        listByRegionIDAndStateSearch.and("regionId", listByRegionIDAndStateSearch.entity().getRegionId(), SearchCriteria.Op.EQ);
        listByRegionIDAndStateSearch.and("state", listByRegionIDAndStateSearch.entity().getState(), SearchCriteria.Op.EQ);
        listByRegionIDAndStateSearch.done();

        findByIpAddressSearch = createSearchBuilder();
        findByIpAddressSearch.and("address", findByIpAddressSearch.entity().getAddress(), SearchCriteria.Op.EQ);
        findByIpAddressSearch.done();
    }

    @Override
    public List<PortableIpVO> listByRegionId(final int regionIdId) {
        final SearchCriteria<PortableIpVO> sc = listByRegionIDSearch.create();
        sc.setParameters("regionId", regionIdId);
        return listBy(sc);
    }

    @Override
    public List<PortableIpVO> listByRangeId(final long rangeId) {
        final SearchCriteria<PortableIpVO> sc = listByRangeIDSearch.create();
        sc.setParameters("rangeId", rangeId);
        return listBy(sc);
    }

    @Override
    public List<PortableIpVO> listByRangeIdAndState(final long rangeId, final PortableIp.State state) {
        final SearchCriteria<PortableIpVO> sc = listByRangeIDAndStateSearch.create();
        sc.setParameters("rangeId", rangeId);
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public List<PortableIpVO> listByRegionIdAndState(final int regionId, final PortableIp.State state) {
        final SearchCriteria<PortableIpVO> sc = listByRegionIDAndStateSearch.create();
        sc.setParameters("regionId", regionId);
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public PortableIpVO findByIpAddress(final String ipAddress) {
        final SearchCriteria<PortableIpVO> sc = findByIpAddressSearch.create();
        sc.setParameters("address", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public void unassignIpAddress(final long ipAddressId) {
        final PortableIpVO address = createForUpdate();
        address.setAllocatedToAccountId(null);
        address.setAllocatedInDomainId(null);
        address.setAllocatedTime(null);
        address.setState(PortableIp.State.Free);
        address.setAssociatedWithNetworkId(null);
        address.setAssociatedDataCenterId(null);
        address.setAssociatedWithVpcId(null);
        address.setPhysicalNetworkId(null);
        update(ipAddressId, address);
    }
}
