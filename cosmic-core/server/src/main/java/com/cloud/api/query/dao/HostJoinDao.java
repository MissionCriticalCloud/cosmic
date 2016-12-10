package com.cloud.api.query.dao;

import com.cloud.api.query.vo.HostJoinVO;
import com.cloud.api.response.HostForMigrationResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.host.Host;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.ApiConstants.HostDetails;

import java.util.EnumSet;
import java.util.List;

public interface HostJoinDao extends GenericDao<HostJoinVO, Long> {

    HostResponse newHostResponse(HostJoinVO host, EnumSet<HostDetails> details);

    HostResponse setHostResponse(HostResponse response, HostJoinVO host);

    HostForMigrationResponse newHostForMigrationResponse(HostJoinVO host, EnumSet<HostDetails> details);

    HostForMigrationResponse setHostForMigrationResponse(HostForMigrationResponse response, HostJoinVO host);

    List<HostJoinVO> newHostView(Host group);

    List<HostJoinVO> searchByIds(Long... ids);
}
