package com.cloud.user.dao;

import com.cloud.user.UserVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB
public class UserDaoImpl extends GenericDaoBase<UserVO, Long> implements UserDao {
    protected SearchBuilder<UserVO> UsernamePasswordSearch;
    protected SearchBuilder<UserVO> UsernameSearch;
    protected SearchBuilder<UserVO> UsernameLikeSearch;
    protected SearchBuilder<UserVO> UserIdSearch;
    protected SearchBuilder<UserVO> AccountIdSearch;
    protected SearchBuilder<UserVO> SecretKeySearch;
    protected SearchBuilder<UserVO> RegistrationTokenSearch;

    protected UserDaoImpl() {
        UsernameSearch = createSearchBuilder();
        UsernameSearch.and("username", UsernameSearch.entity().getUsername(), SearchCriteria.Op.EQ);
        UsernameSearch.done();

        UsernameLikeSearch = createSearchBuilder();
        UsernameLikeSearch.and("username", UsernameLikeSearch.entity().getUsername(), SearchCriteria.Op.LIKE);
        UsernameLikeSearch.done();

        AccountIdSearch = createSearchBuilder();
        AccountIdSearch.and("account", AccountIdSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdSearch.done();

        UsernamePasswordSearch = createSearchBuilder();
        UsernamePasswordSearch.and("username", UsernamePasswordSearch.entity().getUsername(), SearchCriteria.Op.EQ);
        UsernamePasswordSearch.and("password", UsernamePasswordSearch.entity().getPassword(), SearchCriteria.Op.EQ);
        UsernamePasswordSearch.done();

        UserIdSearch = createSearchBuilder();
        UserIdSearch.and("id", UserIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        UserIdSearch.done();

        SecretKeySearch = createSearchBuilder();
        SecretKeySearch.and("secretKey", SecretKeySearch.entity().getSecretKey(), SearchCriteria.Op.EQ);
        SecretKeySearch.done();

        RegistrationTokenSearch = createSearchBuilder();
        RegistrationTokenSearch.and("registrationToken", RegistrationTokenSearch.entity().getRegistrationToken(), SearchCriteria.Op.EQ);
        RegistrationTokenSearch.done();
    }

    @Override
    public UserVO getUser(final String username, final String password) {
        final SearchCriteria<UserVO> sc = UsernamePasswordSearch.create();
        sc.setParameters("username", username);
        sc.setParameters("password", password);
        return findOneBy(sc);
    }

    @Override
    public UserVO getUser(final String username) {
        final SearchCriteria<UserVO> sc = UsernameSearch.create();
        sc.setParameters("username", username);
        return findOneBy(sc);
    }

    @Override
    public UserVO getUser(final long userId) {
        final SearchCriteria<UserVO> sc = UserIdSearch.create();
        sc.setParameters("id", userId);
        return findOneBy(sc);
    }

    @Override
    public List<UserVO> findUsersLike(final String username) {
        final SearchCriteria<UserVO> sc = UsernameLikeSearch.create();
        sc.setParameters("username", "%" + username + "%");
        return listBy(sc);
    }

    @Override
    public List<UserVO> listByAccount(final long accountId) {
        final SearchCriteria<UserVO> sc = AccountIdSearch.create();
        sc.setParameters("account", accountId);
        return listBy(sc, null);
    }

    @Override
    public UserVO findUserBySecretKey(final String secretKey) {
        final SearchCriteria<UserVO> sc = SecretKeySearch.create();
        sc.setParameters("secretKey", secretKey);
        return findOneBy(sc);
    }

    @Override
    public UserVO findUserByRegistrationToken(final String registrationToken) {
        final SearchCriteria<UserVO> sc = RegistrationTokenSearch.create();
        sc.setParameters("registrationToken", registrationToken);
        return findOneBy(sc);
    }

    @Override
    public List<UserVO> findUsersByName(final String username) {
        final SearchCriteria<UserVO> sc = UsernameSearch.create();
        sc.setParameters("username", username);
        return listBy(sc);
    }
}
