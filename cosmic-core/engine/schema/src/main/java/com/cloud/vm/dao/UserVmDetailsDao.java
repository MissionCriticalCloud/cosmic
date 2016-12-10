package com.cloud.vm.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.UserVmDetailVO;

public interface UserVmDetailsDao extends GenericDao<UserVmDetailVO, Long>, ResourceDetailsDao<UserVmDetailVO> {
}
