package com.cloud.user.dao;

import com.cloud.user.Account;
import com.cloud.user.Account.State;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import com.cloud.utils.Pair;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountDaoImpl extends GenericDaoBase<AccountVO, Long> implements AccountDao {
    private static final Logger s_logger = LoggerFactory.getLogger(AccountDaoImpl.class);
    private static final String FIND_USER_ACCOUNT_BY_API_KEY = "SELECT u.id, u.username, u.account_id, u.secret_key, u.state, "
            + "a.id, a.account_name, a.type, a.domain_id, a.state " + "FROM `cloud`.`user` u, `cloud`.`account` a "
            + "WHERE u.account_id = a.id AND u.api_key = ? and u.removed IS NULL";

    protected final SearchBuilder<AccountVO> AllFieldsSearch;
    protected final SearchBuilder<AccountVO> AccountTypeSearch;
    protected final SearchBuilder<AccountVO> DomainAccountsSearch;
    protected final SearchBuilder<AccountVO> CleanupForRemovedAccountsSearch;
    protected final SearchBuilder<AccountVO> CleanupForDisabledAccountsSearch;
    protected final SearchBuilder<AccountVO> NonProjectAccountSearch;
    protected final GenericSearchBuilder<AccountVO, Long> AccountIdsSearch;

    public AccountDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("accountName", AllFieldsSearch.entity().getAccountName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("domainId", AllFieldsSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("type", AllFieldsSearch.entity().getType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        AccountTypeSearch = createSearchBuilder();
        AccountTypeSearch.and("domainId", AccountTypeSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        AccountTypeSearch.and("type", AccountTypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        AccountTypeSearch.done();

        DomainAccountsSearch = createSearchBuilder();
        DomainAccountsSearch.and("domainId", DomainAccountsSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        DomainAccountsSearch.and("removed", DomainAccountsSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        DomainAccountsSearch.done();

        CleanupForRemovedAccountsSearch = createSearchBuilder();
        CleanupForRemovedAccountsSearch.and("cleanup", CleanupForRemovedAccountsSearch.entity().getNeedsCleanup(), SearchCriteria.Op.EQ);
        CleanupForRemovedAccountsSearch.and("removed", CleanupForRemovedAccountsSearch.entity().getRemoved(), SearchCriteria.Op.NNULL);
        CleanupForRemovedAccountsSearch.and("domainid", CleanupForRemovedAccountsSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        CleanupForRemovedAccountsSearch.done();

        CleanupForDisabledAccountsSearch = createSearchBuilder();
        CleanupForDisabledAccountsSearch.and("cleanup", CleanupForDisabledAccountsSearch.entity().getNeedsCleanup(), SearchCriteria.Op.EQ);
        CleanupForDisabledAccountsSearch.and("removed", CleanupForDisabledAccountsSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        CleanupForDisabledAccountsSearch.and("state", CleanupForDisabledAccountsSearch.entity().getState(), SearchCriteria.Op.EQ);
        CleanupForDisabledAccountsSearch.done();

        NonProjectAccountSearch = createSearchBuilder();
        NonProjectAccountSearch.and("accountName", NonProjectAccountSearch.entity().getAccountName(), SearchCriteria.Op.EQ);
        NonProjectAccountSearch.and("domainId", NonProjectAccountSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        NonProjectAccountSearch.and("state", NonProjectAccountSearch.entity().getState(), SearchCriteria.Op.EQ);
        NonProjectAccountSearch.and("type", NonProjectAccountSearch.entity().getType(), SearchCriteria.Op.NEQ);
        NonProjectAccountSearch.done();

        AccountIdsSearch = createSearchBuilder(Long.class);
        AccountIdsSearch.selectFields(AccountIdsSearch.entity().getId());
        AccountIdsSearch.and("ids", AccountIdsSearch.entity().getDomainId(), Op.IN);
        AccountIdsSearch.done();
    }

    @Override
    public Pair<User, Account> findUserAccountByApiKey(final String apiKey) {
        final PreparedStatement pstmt;
        Pair<User, Account> userAcctPair = null;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            final String sql = FIND_USER_ACCOUNT_BY_API_KEY;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, apiKey);
            final ResultSet rs = pstmt.executeQuery();
            // TODO:  make sure we don't have more than 1 result?  ApiKey had better be unique
            if (rs.next()) {
                final User u = new UserVO(rs.getLong(1));
                u.setUsername(rs.getString(2));
                u.setAccountId(rs.getLong(3));
                u.setSecretKey(DBEncryptionUtil.decrypt(rs.getString(4)));
                u.setState(State.valueOf(rs.getString(5)));

                final AccountVO a = new AccountVO(rs.getLong(6));
                a.setAccountName(rs.getString(7));
                a.setType(rs.getShort(8));
                a.setDomainId(rs.getLong(9));
                a.setState(State.valueOf(rs.getString(10)));

                userAcctPair = new Pair<>(u, a);
            }
        } catch (final Exception e) {
            s_logger.warn("Exception finding user/acct by api key: " + apiKey, e);
        }
        return userAcctPair;
    }

    @Override
    public List<AccountVO> findAccountsLike(final String accountName) {
        final SearchCriteria<AccountVO> sc = createSearchCriteria();
        sc.addAnd("accountName", SearchCriteria.Op.LIKE, "%" + accountName + "%");
        return listBy(sc);
    }

    @Override
    public List<AccountVO> findActiveAccounts(final Long maxAccountId, final Filter filter) {
        if (maxAccountId == null) {
            return null;
        }

        final SearchCriteria<AccountVO> sc = createSearchCriteria();
        sc.addAnd("id", SearchCriteria.Op.LTEQ, maxAccountId);

        return listBy(sc, filter);
    }

    @Override
    public List<AccountVO> findRecentlyDeletedAccounts(final Long maxAccountId, final Date earliestRemovedDate, final Filter filter) {
        if (earliestRemovedDate == null) {
            return null;
        }
        final SearchCriteria<AccountVO> sc = createSearchCriteria();
        if (maxAccountId != null) {
            sc.addAnd("id", SearchCriteria.Op.LTEQ, maxAccountId);
        }
        sc.addAnd("removed", SearchCriteria.Op.NNULL);
        sc.addAnd("removed", SearchCriteria.Op.GTEQ, earliestRemovedDate);

        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<AccountVO> findNewAccounts(final Long minAccountId, final Filter filter) {
        if (minAccountId == null) {
            return null;
        }

        final SearchCriteria<AccountVO> sc = createSearchCriteria();
        sc.addAnd("id", SearchCriteria.Op.GT, minAccountId);

        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<AccountVO> findCleanupsForRemovedAccounts(final Long domainId) {
        final SearchCriteria<AccountVO> sc = CleanupForRemovedAccountsSearch.create();
        sc.setParameters("cleanup", true);

        if (domainId != null) {
            sc.setParameters("domainid", domainId);
        }

        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<AccountVO> findActiveAccountsForDomain(final Long domain) {
        final SearchCriteria<AccountVO> sc = DomainAccountsSearch.create();
        sc.setParameters("domainId", domain);
        return listBy(sc);
    }

    @Override
    public void markForCleanup(final long accountId) {
        final AccountVO account = findByIdIncludingRemoved(accountId);
        if (!account.getNeedsCleanup()) {
            account.setNeedsCleanup(true);
            if (!update(accountId, account)) {
                s_logger.warn("Failed to mark account id=" + accountId + " for cleanup");
            }
        }
    }

    @Override
    public List<AccountVO> listAccounts(final String accountName, final Long domainId, final Filter filter) {
        final SearchCriteria<AccountVO> sc = AllFieldsSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<AccountVO> findCleanupsForDisabledAccounts() {
        final SearchCriteria<AccountVO> sc = CleanupForDisabledAccountsSearch.create();
        sc.setParameters("cleanup", true);
        sc.setParameters("state", State.disabled);

        return listBy(sc);
    }

    @Override
    public Account findEnabledAccount(final String accountName, final Long domainId) {
        final SearchCriteria<AccountVO> sc = AllFieldsSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        sc.setParameters("state", State.enabled);
        return findOneBy(sc);
    }

    @Override
    public Account findEnabledNonProjectAccount(final String accountName, final Long domainId) {
        final SearchCriteria<AccountVO> sc = NonProjectAccountSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        sc.setParameters("state", State.enabled);
        sc.setParameters("type", Account.ACCOUNT_TYPE_PROJECT);
        return findOneBy(sc);
    }

    @Override
    public Account findAccountIncludingRemoved(final String accountName, final Long domainId) {
        final SearchCriteria<AccountVO> sc = AllFieldsSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public Account findNonProjectAccountIncludingRemoved(final String accountName, final Long domainId) {
        final SearchCriteria<AccountVO> sc = NonProjectAccountSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        sc.setParameters("type", Account.ACCOUNT_TYPE_PROJECT);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public Account findActiveAccount(final String accountName, final Long domainId) {
        final SearchCriteria<AccountVO> sc = AllFieldsSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        return findOneBy(sc);
    }

    @Override
    public Account findActiveNonProjectAccount(final String accountName, final Long domainId) {
        final SearchCriteria<AccountVO> sc = NonProjectAccountSearch.create("accountName", accountName);
        sc.setParameters("domainId", domainId);
        sc.setParameters("type", Account.ACCOUNT_TYPE_PROJECT);
        return findOneBy(sc);
    }

    @Override
    public List<Long> getAccountIdsForDomains(final List<Long> domainIds) {
        final SearchCriteria<Long> sc = AccountIdsSearch.create();
        sc.setParameters("ids", domainIds.toArray(new Object[domainIds.size()]));
        return customSearchIncludingRemoved(sc, null);
    }

    @Override
    public long getDomainIdForGivenAccountId(final long id) {
        long domain_id = -1;
        try {
            final AccountVO account_vo = findById(id);
            domain_id = account_vo.getDomainId();
        } catch (final Exception e) {
            s_logger.warn("getDomainIdForGivenAccountId: Exception :" + e.getMessage());
        } finally {
            return domain_id;
        }
    }
}
