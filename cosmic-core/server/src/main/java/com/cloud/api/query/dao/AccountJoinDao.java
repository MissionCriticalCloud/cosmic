package com.cloud.api.query.dao;

import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.query.vo.AccountJoinVO;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.ResourceLimitAndCountResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.utils.db.GenericDao;

public interface AccountJoinDao extends GenericDao<AccountJoinVO, Long> {

    AccountResponse newAccountResponse(ResponseView view, AccountJoinVO vol);

    AccountJoinVO newAccountView(Account vol);

    void setResourceLimits(AccountJoinVO account, boolean accountIsAdmin, ResourceLimitAndCountResponse response);
}
