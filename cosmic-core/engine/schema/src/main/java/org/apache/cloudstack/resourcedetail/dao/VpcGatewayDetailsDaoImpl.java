package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.VpcGatewayDetailVO;

import org.springframework.stereotype.Component;

@Component
public class VpcGatewayDetailsDaoImpl extends ResourceDetailsDaoBase<VpcGatewayDetailVO> implements VpcGatewayDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new VpcGatewayDetailVO(resourceId, key, value, display));
    }
}
