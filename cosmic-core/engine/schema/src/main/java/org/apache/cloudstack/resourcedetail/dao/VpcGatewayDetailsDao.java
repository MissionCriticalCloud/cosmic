package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;
import org.apache.cloudstack.resourcedetail.VpcGatewayDetailVO;

public interface VpcGatewayDetailsDao extends GenericDao<VpcGatewayDetailVO, Long>, ResourceDetailsDao<VpcGatewayDetailVO> {

}
