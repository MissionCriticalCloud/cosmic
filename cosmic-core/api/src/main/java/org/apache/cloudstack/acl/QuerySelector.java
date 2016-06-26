package org.apache.cloudstack.acl;

import com.cloud.user.Account;
import com.cloud.utils.component.Adapter;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;

import java.util.List;

/**
 * QueryChecker returns granted access at domain, account or resource level.
 */
public interface QuerySelector extends Adapter {

    /**
     * List granted domains for the caller, given a specific action.
     *
     * @param caller account to check against.
     * @param action action
     * @return list of domain Ids granted to the caller account.
     */
    List<Long> getAuthorizedDomains(Account caller, String action, AccessType accessType);

    /**
     * List granted accounts for the caller, given a specific action.
     *
     * @param caller account to check against.
     * @param action action.
     * @return list of domain Ids granted to the caller account.
     */
    List<Long> getAuthorizedAccounts(Account caller, String action, AccessType accessType);

    /**
     * List granted resources for the caller, given a specific action.
     *
     * @param caller account to check against.
     * @param action action.
     * @return list of domain Ids granted to the caller account.
     */
    List<Long> getAuthorizedResources(Account caller, String action, AccessType accessType);

    /**
     * Check if this account is associated with a policy with scope of ALL
     *
     * @param caller account to check
     * @param action action.
     * @return true if this account is attached with a policy for the given action of ALL scope.
     */
    boolean isGrantedAll(Account caller, String action, AccessType accessType);

    /**
     * List of ACL group the given account belongs to
     *
     * @param accountId account id.
     * @return ACL group names
     */
    List<String> listAclGroupsByAccount(long accountId);
}
