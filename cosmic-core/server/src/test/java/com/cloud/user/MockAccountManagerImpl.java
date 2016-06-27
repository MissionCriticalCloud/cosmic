package com.cloud.user;

import com.cloud.api.query.vo.ControlledViewEntity;
import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.command.admin.account.UpdateAccountCmd;
import org.apache.cloudstack.api.command.admin.user.DeleteUserCmd;
import org.apache.cloudstack.api.command.admin.user.RegisterCmd;
import org.apache.cloudstack.api.command.admin.user.UpdateUserCmd;

import javax.naming.ConfigurationException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MockAccountManagerImpl extends ManagerBase implements Manager, AccountManager {

    @Override
    public boolean disableAccount(final long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteAccount(final AccountVO account, final long callerUserId, final Account caller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Long checkAccessAndSpecifyAuthority(final Account caller, final Long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account createAccount(final String accountName, final short accountType, final Long domainId, final String networkDomain, final Map<String, String> details, final
    String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void logoutUser(final long userId) {
        // TODO Auto-generated method stub
    }

    @Override
    public UserAccount authenticateUser(final String username, final String password, final Long domainId, final InetAddress loginIpAddress, final Map<String, Object[]>
            requestParameters) {
        return null;
    }

    @Override
    public Pair<User, Account> findUserByApiKey(final String apiKey) {
        return null;
    }

    @Override
    public boolean enableAccount(final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void buildACLSearchBuilder(final SearchBuilder<? extends ControlledEntity> sb, final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts,
                                      final ListProjectResourcesCriteria listProjectResourcesCriteria) {
        // TODO Auto-generated method stub

    }

    @Override
    public void buildACLViewSearchBuilder(final SearchBuilder<? extends ControlledViewEntity> sb, final Long domainId,
                                          final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria listProjectResourcesCriteria) {
        // TODO Auto-generated method stub
    }

    @Override
    public void buildACLSearchCriteria(final SearchCriteria<? extends ControlledEntity> sc, final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts,
                                       final ListProjectResourcesCriteria listProjectResourcesCriteria) {
        // TODO Auto-generated method stub

    }

    @Override
    public void buildACLSearchParameters(final Account caller, final Long id, final String accountName, final Long projectId, final List<Long> permittedAccounts, final
    Ternary<Long, Boolean,
            ListProjectResourcesCriteria> domainIdRecursiveListProject, final boolean listAll, final boolean forProjectInvitation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void buildACLViewSearchCriteria(final SearchCriteria<? extends ControlledViewEntity> sc, final Long domainId,
                                           final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria listProjectResourcesCriteria) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean deleteUserAccount(final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Account updateAccount(final UpdateAccountCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account disableAccount(final String accountName, final Long domainId, final Long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account enableAccount(final String accountName, final Long domainId, final Long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteUser(final DeleteUserCmd deleteUserCmd) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UserAccount updateUser(final UpdateUserCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount disableUser(final long userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount enableUser(final long userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account lockAccount(final String accountName, final Long domainId, final Long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> listAclGroupsByAccount(final Long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UserAccount createUserAccount(final String userName, final String password, final String firstName, final String lastName, final String email, final String timezone,
                                         final String accountName,
                                         final short accountType, final Long domainId, final String networkDomain, final Map<String, String> details, final String accountUUID,
                                         final String userUUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount createUserAccount(final String userName, final String password, final String firstName, final String lastName, final String email, final String timezone,
                                         final String accountName, final short accountType,
                                         final Long domainId, final String networkDomain, final Map<String, String> details, final String accountUUID, final String userUUID,
                                         final User.Source source) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount lockUser(final long userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getSystemAccount() {
        return new AccountVO();
    }

    @Override
    public User getSystemUser() {
        return new UserVO();
    }

    @Override
    public User createUser(final String userName, final String password, final String firstName,
                           final String lastName, final String email, final String timeZone, final String accountName,
                           final Long domainId, final String userUUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User createUser(final String userName, final String password, final String firstName, final String lastName, final String email, final String timeZone, final String
            accountName, final Long domainId,
                           final String userUUID, final User.Source source) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAdmin(final Long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Account finalizeOwner(final Account caller, final String accountName, final Long domainId, final Long projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getActiveAccountByName(final String accountName, final Long domainId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount getActiveUserAccount(final String username, final Long domainId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount updateUser(final Long userId, final String firstName, final String lastName, final String email, final String userName, final String password, final
    String apiKey, final String secretKey,
                                  final String timeZone) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getActiveAccountById(final long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getAccount(final long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User getActiveUser(final long userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User getUserIncludingRemoved(final long userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRootAdmin(final Long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDomainAdmin(final Long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNormalUser(final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public User getActiveUserByRegistrationToken(final String registrationToken) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void markUserRegistered(final long userId) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] createApiKeyAndSecretKey(final RegisterCmd cmd) {
        return null;
    }

    @Override
    public String[] createApiKeyAndSecretKey(final long userId) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.user.AccountService#getUserByApiKey(java.lang.String)
     */
    @Override
    public UserAccount getUserByApiKey(final String apiKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RoleType getRoleType(final Account account) {
        return null;
    }

    @Override
    public void checkAccess(final Account account, final Domain domain) throws PermissionDeniedException {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkAccess(final Account account, final AccessType accessType, final boolean sameOwner, final ControlledEntity... entities) throws PermissionDeniedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void checkAccess(final Account account, final ServiceOffering so) throws PermissionDeniedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void checkAccess(final Account account, final DiskOffering dof) throws PermissionDeniedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void checkAccess(final Account account, final AccessType accessType, final boolean sameOwner, final String apiName,
                            final ControlledEntity... entities) throws PermissionDeniedException {
        // TODO Auto-generated method stub
    }

    @Override
    public Long finalyzeAccountId(final String accountName, final Long domainId, final Long projectId, final boolean enabledOnly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserAccount getUserAccountById(final Long userId) {
        // TODO Auto-generated method stub
        return null;
    }
}
