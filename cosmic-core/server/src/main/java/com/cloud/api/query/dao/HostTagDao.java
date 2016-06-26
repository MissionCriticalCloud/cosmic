package com.cloud.api.query.dao;

import com.cloud.api.query.vo.HostTagVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.HostTagResponse;

import java.util.List;

public interface HostTagDao extends GenericDao<HostTagVO, Long> {
    HostTagResponse newHostTagResponse(HostTagVO hostTag);

    List<HostTagVO> searchByIds(Long... hostTagIds);
}
