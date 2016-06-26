package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;
import org.apache.cloudstack.resourcedetail.VpcDetailVO;

public interface VpcDetailsDao extends GenericDao<VpcDetailVO, Long>, ResourceDetailsDao<VpcDetailVO> {

}
