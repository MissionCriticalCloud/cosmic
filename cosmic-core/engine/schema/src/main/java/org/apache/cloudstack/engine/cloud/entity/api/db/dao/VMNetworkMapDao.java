package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMNetworkMapVO;

import java.util.List;

public interface VMNetworkMapDao extends GenericDao<VMNetworkMapVO, Long> {

    void persist(long vmId, List<Long> networks);

    List<Long> getNetworks(long vmId);

    VMNetworkMapVO findByVmAndNetworkId(long vmId, long networkId);
}
