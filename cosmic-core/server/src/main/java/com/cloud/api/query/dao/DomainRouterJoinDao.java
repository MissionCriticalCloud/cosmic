package com.cloud.api.query.dao;

import com.cloud.api.query.vo.DomainRouterJoinVO;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.user.Account;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DomainRouterJoinDao extends GenericDao<DomainRouterJoinVO, Long> {

    DomainRouterResponse newDomainRouterResponse(DomainRouterJoinVO uvo, Account caller);

    DomainRouterResponse setDomainRouterResponse(DomainRouterResponse userVmData, DomainRouterJoinVO uvo);

    List<DomainRouterJoinVO> newDomainRouterView(VirtualRouter vr);

    List<DomainRouterJoinVO> searchByIds(Long... ids);
}
