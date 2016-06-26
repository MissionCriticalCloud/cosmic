package com.cloud.network.vpc;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/**
 * @author daan
 */
public interface NetworkACLItemCidrsDao extends GenericDao<NetworkACLItemCidrsVO, Long> {

    void persist(long networkACLItemId, List<String> cidrs);

    void updateCidrs(long networkACLItemId, List<String> cidrs);

    List<String> getCidrs(long networkACLItemId);

    @DB
    List<NetworkACLItemCidrsVO> listByNetworkACLItemId(long networkACLItemId);
}
