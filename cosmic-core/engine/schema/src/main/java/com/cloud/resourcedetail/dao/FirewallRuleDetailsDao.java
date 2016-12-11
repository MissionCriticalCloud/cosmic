package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.FirewallRuleDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface FirewallRuleDetailsDao extends GenericDao<FirewallRuleDetailVO, Long>, ResourceDetailsDao<FirewallRuleDetailVO> {

}
