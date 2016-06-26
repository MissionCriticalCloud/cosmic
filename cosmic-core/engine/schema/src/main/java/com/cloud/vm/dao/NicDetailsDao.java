package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.NicDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface NicDetailsDao extends GenericDao<NicDetailVO, Long>, ResourceDetailsDao<NicDetailVO> {
}
