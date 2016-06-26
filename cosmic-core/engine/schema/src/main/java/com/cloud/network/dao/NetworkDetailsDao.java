package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface NetworkDetailsDao extends GenericDao<NetworkDetailVO, Long>, ResourceDetailsDao<NetworkDetailVO> {

}
