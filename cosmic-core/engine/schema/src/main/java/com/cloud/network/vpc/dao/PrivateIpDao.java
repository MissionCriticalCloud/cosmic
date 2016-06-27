package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.PrivateIpVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PrivateIpDao extends GenericDao<PrivateIpVO, Long> {

    /**
     * @param dcId
     * @param networkId
     * @param requestedIp TODO
     * @return
     */
    PrivateIpVO allocateIpAddress(long dcId, long networkId, String requestedIp);

    /**
     * @param ipAddress
     * @param networkId
     */
    void releaseIpAddress(String ipAddress, long networkId);

    /**
     * @param networkId
     * @param ip4Address
     * @return
     */
    PrivateIpVO findByIpAndSourceNetworkId(long networkId, String ip4Address);

    /**
     * @param networkId
     * @return
     */
    List<PrivateIpVO> listByNetworkId(long networkId);

    /**
     * @param ntwkId
     * @return
     */
    int countAllocatedByNetworkId(long ntwkId);

    /**
     * @param networkId
     */
    void deleteByNetworkId(long networkId);

    int countByNetworkId(long ntwkId);

    /**
     * @param vpcId
     * @param ip4Address
     * @return
     */
    PrivateIpVO findByIpAndVpcId(long vpcId, String ip4Address);

    PrivateIpVO findByIpAndSourceNetworkIdAndVpcId(long networkId, String ip4Address, long vpcId);
}
