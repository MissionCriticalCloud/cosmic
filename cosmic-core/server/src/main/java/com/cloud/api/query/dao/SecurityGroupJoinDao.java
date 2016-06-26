package com.cloud.api.query.dao;

import com.cloud.api.query.vo.SecurityGroupJoinVO;
import com.cloud.network.security.SecurityGroup;
import com.cloud.user.Account;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.SecurityGroupResponse;

import java.util.List;

public interface SecurityGroupJoinDao extends GenericDao<SecurityGroupJoinVO, Long> {

    SecurityGroupResponse newSecurityGroupResponse(SecurityGroupJoinVO vsg, Account caller);

    SecurityGroupResponse setSecurityGroupResponse(SecurityGroupResponse vsgData, SecurityGroupJoinVO vsg);

    List<SecurityGroupJoinVO> newSecurityGroupView(SecurityGroup sg);

    List<SecurityGroupJoinVO> searchByIds(Long... ids);
}
