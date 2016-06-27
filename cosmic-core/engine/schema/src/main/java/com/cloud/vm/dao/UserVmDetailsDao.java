package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.UserVmDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface UserVmDetailsDao extends GenericDao<UserVmDetailVO, Long>, ResourceDetailsDao<UserVmDetailVO> {
}
