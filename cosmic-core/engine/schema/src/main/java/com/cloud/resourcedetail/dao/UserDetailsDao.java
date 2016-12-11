package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.resourcedetail.UserDetailVO;
import com.cloud.utils.db.GenericDao;

public interface UserDetailsDao extends GenericDao<UserDetailVO, Long>, ResourceDetailsDao<UserDetailVO> {

}
