package com.cloud.user.dao;

import com.cloud.user.UserAccount;
import com.cloud.user.UserAccountVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class UserAccountDaoImpl extends GenericDaoBase<UserAccountVO, Long> implements UserAccountDao {

    protected final SearchBuilder<UserAccountVO> userAccountSearch;

    public UserAccountDaoImpl() {
        userAccountSearch = createSearchBuilder();
        userAccountSearch.and("apiKey", userAccountSearch.entity().getApiKey(), SearchCriteria.Op.EQ);
        userAccountSearch.done();
    }

    @Override
    public List<UserAccountVO> getAllUsersByNameAndEntity(final String username, final String entity) {
        if (username == null) {
            return null;
        }
        final SearchCriteria<UserAccountVO> sc = createSearchCriteria();
        sc.addAnd("username", SearchCriteria.Op.EQ, username);
        sc.addAnd("externalEntity", SearchCriteria.Op.EQ, entity);
        return listBy(sc);
    }

    @Override
    public UserAccount getUserAccount(final String username, final Long domainId) {
        if ((username == null) || (domainId == null)) {
            return null;
        }

        final SearchCriteria<UserAccountVO> sc = createSearchCriteria();
        sc.addAnd("username", SearchCriteria.Op.EQ, username);
        sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        return findOneBy(sc);
    }

    @Override
    public boolean validateUsernameInDomain(final String username, final Long domainId) {
        final UserAccount userAcct = getUserAccount(username, domainId);
        if (userAcct == null) {
            return true;
        }
        return false;
    }

    @Override
    public UserAccount getUserByApiKey(final String apiKey) {
        final SearchCriteria<UserAccountVO> sc = userAccountSearch.create();
        sc.setParameters("apiKey", apiKey);
        return findOneBy(sc);
    }
}
