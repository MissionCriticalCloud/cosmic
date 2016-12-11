package com.cloud.api.query.dao;

import com.cloud.api.query.vo.HostTagVO;
import com.cloud.api.response.HostTagResponse;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface HostTagDao extends GenericDao<HostTagVO, Long> {
    HostTagResponse newHostTagResponse(HostTagVO hostTag);

    List<HostTagVO> searchByIds(Long... hostTagIds);
}
