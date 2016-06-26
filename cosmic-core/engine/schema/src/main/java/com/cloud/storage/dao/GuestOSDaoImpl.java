package com.cloud.storage.dao;

import com.cloud.storage.GuestOSVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import org.springframework.stereotype.Component;

@Component
public class GuestOSDaoImpl extends GenericDaoBase<GuestOSVO, Long> implements GuestOSDao {

    protected final SearchBuilder<GuestOSVO> Search;

    protected GuestOSDaoImpl() {
        Search = createSearchBuilder();
        Search.and("display_name", Search.entity().getDisplayName(), SearchCriteria.Op.EQ);
        Search.done();
    }

    @Override
    public GuestOSVO listByDisplayName(final String displayName) {
        final SearchCriteria<GuestOSVO> sc = Search.create();
        sc.setParameters("display_name", displayName);
        return findOneBy(sc);
    }
}
