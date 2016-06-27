package org.apache.cloudstack.resourcedetail.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.FirewallRuleDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface FirewallRuleDetailsDao extends GenericDao<FirewallRuleDetailVO, Long>, ResourceDetailsDao<FirewallRuleDetailVO> {

}
