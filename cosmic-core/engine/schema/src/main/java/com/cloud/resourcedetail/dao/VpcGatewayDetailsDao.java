package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.resourcedetail.VpcGatewayDetailVO;
import com.cloud.utils.db.GenericDao;

public interface VpcGatewayDetailsDao extends GenericDao<VpcGatewayDetailVO, Long>, ResourceDetailsDao<VpcGatewayDetailVO> {

}
