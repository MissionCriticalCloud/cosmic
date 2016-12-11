package com.cloud.vm.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.NicDetailVO;

public interface NicDetailsDao extends GenericDao<NicDetailVO, Long>, ResourceDetailsDao<NicDetailVO> {
}
