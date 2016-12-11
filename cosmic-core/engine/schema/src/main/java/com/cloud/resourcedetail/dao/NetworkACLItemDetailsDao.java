package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.NetworkACLItemDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface NetworkACLItemDetailsDao extends GenericDao<NetworkACLItemDetailVO, Long>, ResourceDetailsDao<NetworkACLItemDetailVO> {

}
