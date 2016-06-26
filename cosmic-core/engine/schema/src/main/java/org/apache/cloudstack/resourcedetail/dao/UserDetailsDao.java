package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;
import org.apache.cloudstack.resourcedetail.UserDetailVO;

public interface UserDetailsDao extends GenericDao<UserDetailVO, Long>, ResourceDetailsDao<UserDetailVO> {

}
