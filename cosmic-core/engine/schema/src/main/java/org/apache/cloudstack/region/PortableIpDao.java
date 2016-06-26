package org.apache.cloudstack.region;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PortableIpDao extends GenericDao<PortableIpVO, Long> {

    List<PortableIpVO> listByRegionId(int regionId);

    List<PortableIpVO> listByRangeId(long rangeId);

    List<PortableIpVO> listByRangeIdAndState(long rangeId, PortableIp.State state);

    List<PortableIpVO> listByRegionIdAndState(int regionId, PortableIp.State state);

    PortableIpVO findByIpAddress(String ipAddress);

    void unassignIpAddress(long ipAddressId);
}
