package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.NetworkACLVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkACLDaoImpl extends GenericDaoBase<NetworkACLVO, Long> implements NetworkACLDao {

    protected NetworkACLDaoImpl() {
    }
}
