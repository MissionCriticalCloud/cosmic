package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.resourcedetail.UserIpAddressDetailVO;
import com.cloud.utils.db.GenericDao;

public interface UserIpAddressDetailsDao extends GenericDao<UserIpAddressDetailVO, Long>, ResourceDetailsDao<UserIpAddressDetailVO> {

}
