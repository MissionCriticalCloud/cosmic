package com.cloud.user.dao;

import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface AccountDao extends GenericDao<AccountVO, Long> {
    Pair<User, Account> findUserAccountByApiKey(String apiKey);

    List<AccountVO> findAccountsLike(String accountName);

    List<AccountVO> findActiveAccounts(Long maxAccountId, Filter filter);

    List<AccountVO> findRecentlyDeletedAccounts(Long maxAccountId, Date earliestRemovedDate, Filter filter);

    List<AccountVO> findNewAccounts(Long minAccountId, Filter filter);

    List<AccountVO> findCleanupsForRemovedAccounts(Long domainId);

    List<AccountVO> findActiveAccountsForDomain(Long domain);

    void markForCleanup(long accountId);

    List<AccountVO> listAccounts(String accountName, Long domainId, Filter filter);

    List<AccountVO> findCleanupsForDisabledAccounts();

    //return account only in enabled state
    Account findEnabledAccount(String accountName, Long domainId);

    Account findEnabledNonProjectAccount(String accountName, Long domainId);

    //returns account even when it's removed
    Account findAccountIncludingRemoved(String accountName, Long domainId);

    Account findNonProjectAccountIncludingRemoved(String accountName, Long domainId);

    //returns only non-removed account
    Account findActiveAccount(String accountName, Long domainId);

    Account findActiveNonProjectAccount(String accountName, Long domainId);

    List<Long> getAccountIdsForDomains(List<Long> ids);

    /*
    @Desc:   Retrieves the DomainId for a given Account Id
    @Input:  id : Id of the Account
    @Output: DomainId matching for the given Account Id. Returns -1
             in case of no match;
     */
    long getDomainIdForGivenAccountId(long id);
}
