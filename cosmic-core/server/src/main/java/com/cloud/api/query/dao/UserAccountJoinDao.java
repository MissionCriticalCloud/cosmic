package com.cloud.api.query.dao;

import com.cloud.api.query.vo.UserAccountJoinVO;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.UserResponse;

import java.util.List;

public interface UserAccountJoinDao extends GenericDao<UserAccountJoinVO, Long> {

    UserResponse newUserResponse(UserAccountJoinVO usr);

    UserAccountJoinVO newUserView(User usr);

    UserAccountJoinVO newUserView(UserAccount usr);

    List<UserAccountJoinVO> searchByAccountId(Long accountId);
}
