package com.cloud.secstorage;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class CommandExecLogDaoImpl extends GenericDaoBase<CommandExecLogVO, Long> implements CommandExecLogDao {

    protected final SearchBuilder<CommandExecLogVO> ExpungeSearch;

    public CommandExecLogDaoImpl() {
        ExpungeSearch = createSearchBuilder();
        ExpungeSearch.and("created", ExpungeSearch.entity().getCreated(), Op.LT);
        ExpungeSearch.done();
    }

    @Override
    public void expungeExpiredRecords(final Date cutTime) {
        final SearchCriteria<CommandExecLogVO> sc = ExpungeSearch.create();
        sc.setParameters("created", cutTime);
        expunge(sc);
    }
}
