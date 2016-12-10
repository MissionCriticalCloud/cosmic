package com.cloud.network.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface NetworkDetailsDao extends GenericDao<NetworkDetailVO, Long>, ResourceDetailsDao<NetworkDetailVO> {

}
