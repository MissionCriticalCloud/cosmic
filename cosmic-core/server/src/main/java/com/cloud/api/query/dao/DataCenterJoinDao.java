package com.cloud.api.query.dao;

import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.query.vo.DataCenterJoinVO;
import com.cloud.api.response.ZoneResponse;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.utils.db.GenericDao;

public interface DataCenterJoinDao extends GenericDao<DataCenterJoinVO, Long> {

    ZoneResponse newDataCenterResponse(ResponseView view, DataCenterJoinVO dof, Boolean showCapacities);

    DataCenterJoinVO newDataCenterView(DataCenter dof);
}
