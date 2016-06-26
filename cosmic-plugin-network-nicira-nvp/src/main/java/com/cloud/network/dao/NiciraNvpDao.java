//

//

package com.cloud.network.dao;

import com.cloud.network.NiciraNvpDeviceVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface NiciraNvpDao extends GenericDao<NiciraNvpDeviceVO, Long> {
    /**
     * list all the nicira nvp devices added in to this physical network
     *
     * @param physicalNetworkId physical Network Id
     * @return list of NiciraNvpDeviceVO for this physical network.
     */
    List<NiciraNvpDeviceVO> listByPhysicalNetwork(long physicalNetworkId);
}
