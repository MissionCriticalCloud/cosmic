package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.NetworkACLListDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface NetworkACLListDetailsDao extends GenericDao<NetworkACLListDetailVO, Long>, ResourceDetailsDao<NetworkACLListDetailVO> {

}
