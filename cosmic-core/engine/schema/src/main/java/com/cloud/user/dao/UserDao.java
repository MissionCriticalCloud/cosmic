package com.cloud.user.dao;

import com.cloud.user.UserVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/*
 * Data Access Object for user table
 */
public interface UserDao extends GenericDao<UserVO, Long> {
    UserVO getUser(String username, String password);

    UserVO getUser(String username);

    UserVO getUser(long userId);

    List<UserVO> findUsersLike(String username);

    List<UserVO> listByAccount(long accountId);

    /**
     * Finds a user based on the secret key provided.
     *
     * @param secretKey
     * @return
     */
    UserVO findUserBySecretKey(String secretKey);

    /**
     * Finds a user based on the registration token provided.
     *
     * @param registrationToken
     * @return
     */
    UserVO findUserByRegistrationToken(String registrationToken);

    List<UserVO> findUsersByName(String username);
}
