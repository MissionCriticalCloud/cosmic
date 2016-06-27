package com.cloud.storage.dao;

import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.utils.db.GenericDaoBase;

import org.springframework.stereotype.Component;

@Component
public class GuestOSCategoryDaoImpl extends GenericDaoBase<GuestOSCategoryVO, Long> implements GuestOSCategoryDao {

    protected GuestOSCategoryDaoImpl() {

    }
}
