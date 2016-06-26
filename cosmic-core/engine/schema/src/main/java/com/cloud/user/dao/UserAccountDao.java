package com.cloud.user.dao;

import com.cloud.user.UserAccount;
import com.cloud.user.UserAccountVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface UserAccountDao extends GenericDao<UserAccountVO, Long> {
    List<UserAccountVO> getAllUsersByNameAndEntity(String username, String entity);

    UserAccount getUserAccount(String username, Long domainId);

    boolean validateUsernameInDomain(String username, Long domainId);

    UserAccount getUserByApiKey(String apiKey);
}
