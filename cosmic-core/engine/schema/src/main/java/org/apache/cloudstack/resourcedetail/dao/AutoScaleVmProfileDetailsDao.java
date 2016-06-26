package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.AutoScaleVmProfileDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface AutoScaleVmProfileDetailsDao extends GenericDao<AutoScaleVmProfileDetailVO, Long>, ResourceDetailsDao<AutoScaleVmProfileDetailVO> {

}
