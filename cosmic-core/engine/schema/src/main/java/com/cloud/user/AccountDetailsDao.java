package com.cloud.user;

import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface AccountDetailsDao extends GenericDao<AccountDetailVO, Long> {
    Map<String, String> findDetails(long accountId);

    void persist(long accountId, Map<String, String> details);

    AccountDetailVO findDetail(long accountId, String name);

    void deleteDetails(long accountId);

    /*
     * For these existing entries, they will get updated. For these new entries,
     * they will get created
     */
    void update(long accountId, Map<String, String> details);
}
