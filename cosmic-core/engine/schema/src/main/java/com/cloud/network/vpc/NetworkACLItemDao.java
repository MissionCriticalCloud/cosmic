package com.cloud.network.vpc;

import com.cloud.utils.db.GenericDao;

import java.util.List;

/*
 * Data Access Object for network_acl_item table
 */
public interface NetworkACLItemDao extends GenericDao<NetworkACLItemVO, Long> {

    boolean setStateToAdd(NetworkACLItemVO rule);

    boolean revoke(NetworkACLItemVO rule);

    List<NetworkACLItemVO> listByACL(Long aclId);

    int getMaxNumberByACL(long aclId);

    NetworkACLItemVO findByAclAndNumber(long aclId, int number);

    void loadCidrs(NetworkACLItemVO item);
}
