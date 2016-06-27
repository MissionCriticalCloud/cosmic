package com.cloud.api.query.dao;

import com.cloud.api.query.vo.DataCenterJoinVO;
import com.cloud.dc.DataCenter;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.ZoneResponse;

public interface DataCenterJoinDao extends GenericDao<DataCenterJoinVO, Long> {

    ZoneResponse newDataCenterResponse(ResponseView view, DataCenterJoinVO dof, Boolean showCapacities);

    DataCenterJoinVO newDataCenterView(DataCenter dof);
}
