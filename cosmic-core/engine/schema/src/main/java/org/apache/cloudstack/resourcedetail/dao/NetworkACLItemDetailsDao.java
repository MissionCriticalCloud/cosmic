package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.NetworkACLItemDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface NetworkACLItemDetailsDao extends GenericDao<NetworkACLItemDetailVO, Long>, ResourceDetailsDao<NetworkACLItemDetailVO> {

}
