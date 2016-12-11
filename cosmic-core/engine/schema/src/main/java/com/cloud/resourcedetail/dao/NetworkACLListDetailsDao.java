package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.NetworkACLListDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface NetworkACLListDetailsDao extends GenericDao<NetworkACLListDetailVO, Long>, ResourceDetailsDao<NetworkACLListDetailVO> {

}
