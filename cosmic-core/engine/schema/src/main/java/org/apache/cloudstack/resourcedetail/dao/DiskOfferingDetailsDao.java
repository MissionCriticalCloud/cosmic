package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.DiskOfferingDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface DiskOfferingDetailsDao extends GenericDao<DiskOfferingDetailVO, Long>, ResourceDetailsDao<DiskOfferingDetailVO> {

}
