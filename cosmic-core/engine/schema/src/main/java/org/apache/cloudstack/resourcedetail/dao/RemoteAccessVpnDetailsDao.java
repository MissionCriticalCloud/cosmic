package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.RemoteAccessVpnDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface RemoteAccessVpnDetailsDao extends GenericDao<RemoteAccessVpnDetailVO, Long>, ResourceDetailsDao<RemoteAccessVpnDetailVO> {

}
