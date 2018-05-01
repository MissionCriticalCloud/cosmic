package com.cloud.vm;

import com.cloud.domain.Domain;
import com.cloud.legacymodel.domain.PartOf;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.OwnedBy;
import com.cloud.legacymodel.user.User;
import com.cloud.utils.Journal;

/**
 */
public interface ReservationContext extends PartOf, OwnedBy {
    /**
     * @return the user making the call.
     */
    User getCaller();

    /**
     * @return the account
     */
    Account getAccount();

    /**
     * @return the domain.
     */
    Domain getDomain();

    /**
     * @return the journal
     */
    Journal getJournal();

    /**
     * @return the reservation id.
     */
    String getReservationId();
}
