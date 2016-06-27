package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;
import org.apache.cloudstack.resourcedetail.UserIpAddressDetailVO;

public interface UserIpAddressDetailsDao extends GenericDao<UserIpAddressDetailVO, Long>, ResourceDetailsDao<UserIpAddressDetailVO> {

}
