package com.cloud.vm;

import com.cloud.dao.EntityManager;
import com.cloud.domain.Domain;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.Journal;

public class ReservationContextImpl implements ReservationContext {
    static EntityManager s_entityMgr;
    User _caller;
    Account _account;
    Domain _domain;
    Journal _journal;
    String _reservationId;

    public ReservationContextImpl(final String reservationId, final Journal journal, final User caller) {
        this(reservationId, journal, caller, null, null);
    }

    public ReservationContextImpl(final String reservationId, final Journal journal, final User caller, final Account account, final Domain domain) {
        _caller = caller;
        _account = account;
        _domain = domain;
        _journal = journal;
        _reservationId = reservationId;
    }

    public ReservationContextImpl(final String reservationId, final Journal journal, final User caller, final Account account) {
        this(reservationId, journal, caller, account, null);
    }

    static public void init(final EntityManager entityMgr) {
        s_entityMgr = entityMgr;
    }

    @Override
    public long getDomainId() {
        return 0;
    }

    @Override
    public long getAccountId() {
        return _caller.getAccountId();
    }

    @Override
    public User getCaller() {
        return _caller;
    }

    @Override
    public Account getAccount() {
        if (_account == null) {
            _account = s_entityMgr.findById(Account.class, _caller.getId());
        }
        return _account;
    }

    @Override
    public Domain getDomain() {
        if (_domain == null) {
            getAccount();
            _domain = s_entityMgr.findById(Domain.class, _account.getDomainId());
        }
        return _domain;
    }

    @Override
    public Journal getJournal() {
        return _journal;
    }

    @Override
    public String getReservationId() {
        return _reservationId;
    }
}
