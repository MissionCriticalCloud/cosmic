package com.cloud.storage.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.storage.VolumeDetailVO;
import com.cloud.utils.db.GenericDao;

public interface VolumeDetailsDao extends GenericDao<VolumeDetailVO, Long>, ResourceDetailsDao<VolumeDetailVO> {

}
