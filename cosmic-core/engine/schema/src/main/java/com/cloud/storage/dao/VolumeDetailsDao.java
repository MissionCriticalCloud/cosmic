package com.cloud.storage.dao;

import com.cloud.storage.VolumeDetailVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface VolumeDetailsDao extends GenericDao<VolumeDetailVO, Long>, ResourceDetailsDao<VolumeDetailVO> {

}
