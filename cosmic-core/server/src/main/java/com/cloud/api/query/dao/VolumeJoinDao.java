package com.cloud.api.query.dao;

import com.cloud.api.query.vo.VolumeJoinVO;
import com.cloud.storage.Volume;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.VolumeResponse;

import java.util.List;

public interface VolumeJoinDao extends GenericDao<VolumeJoinVO, Long> {

    VolumeResponse newVolumeResponse(ResponseView view, VolumeJoinVO vol);

    VolumeResponse setVolumeResponse(ResponseView view, VolumeResponse volData, VolumeJoinVO vol);

    List<VolumeJoinVO> newVolumeView(Volume vol);

    List<VolumeJoinVO> searchByIds(Long... ids);
}
