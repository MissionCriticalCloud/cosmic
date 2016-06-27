package com.cloud.user;

import com.cloud.domain.Domain;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.command.admin.user.RegisterCmd;

import java.util.Map;

public interface AccountService {

    /**
     * Creates a new user and account, stores the password as is so encrypted passwords are recommended.
     *
     * @param userName      TODO
     * @param password      TODO
     * @param firstName     TODO
     * @param lastName      TODO
     * @param email         TODO
     * @param timezone      TODO
     * @param accountName   TODO
     * @param accountType   TODO
     * @param domainId      TODO
     * @param networkDomain TODO
     * @return the user if created successfully, null otherwise
     */
    UserAccount createUserAccount(String userName, String password, String firstName, String lastName, String email, String timezone, String accountName,
                                  short accountType, Long domainId, String networkDomain, Map<String, String> details, String accountUUID, String userUUID);

    UserAccount createUserAccount(String userName, String password, String firstName, String lastName, String email, String timezone, String accountName, short accountType, Long
            domainId, String networkDomain,
                                  Map<String, String> details, String accountUUID, String userUUID, User.Source source);

    /**
     * Locks a user by userId. A locked user cannot access the API, but will still have running VMs/IP addresses
     * allocated/etc.
     *
     * @param userId
     * @return UserAccount object
     */
    UserAccount lockUser(long userId);

    Account getSystemAccount();

    User getSystemUser();

    User createUser(String userName, String password, String firstName, String lastName, String email, String timeZone, String accountName, Long domainId, String userUUID);

    User createUser(String userName, String password, String firstName, String lastName, String email, String timeZone, String accountName, Long domainId, String userUUID,
                    User.Source source);

    boolean isAdmin(Long accountId);

    Account finalizeOwner(Account caller, String accountName, Long domainId, Long projectId);

    Account getActiveAccountByName(String accountName, Long domainId);

    UserAccount getActiveUserAccount(String username, Long domainId);

    UserAccount updateUser(Long userId, String firstName, String lastName, String email, String userName, String password, String apiKey, String secretKey, String timeZone);

    Account getActiveAccountById(long accountId);

    Account getAccount(long accountId);

    User getActiveUser(long userId);

    User getUserIncludingRemoved(long userId);

    boolean isRootAdmin(Long accountId);

    boolean isDomainAdmin(Long accountId);

    boolean isNormalUser(long accountId);

    User getActiveUserByRegistrationToken(String registrationToken);

    void markUserRegistered(long userId);

    public String[] createApiKeyAndSecretKey(RegisterCmd cmd);

    public String[] createApiKeyAndSecretKey(final long userId);

    UserAccount getUserByApiKey(String apiKey);

    RoleType getRoleType(Account account);

    void checkAccess(Account account, Domain domain) throws PermissionDeniedException;

    void checkAccess(Account account, AccessType accessType, boolean sameOwner, ControlledEntity... entities) throws PermissionDeniedException;

    void checkAccess(Account account, ServiceOffering so) throws PermissionDeniedException;

    void checkAccess(Account account, DiskOffering dof) throws PermissionDeniedException;

    void checkAccess(Account account, AccessType accessType, boolean sameOwner, String apiName,
                     ControlledEntity... entities) throws PermissionDeniedException;

    Long finalyzeAccountId(String accountName, Long domainId, Long projectId, boolean enabledOnly);

    /**
     * returns the user account object for a given user id
     *
     * @param userId user id
     * @return useraccount object if it exists else null
     */
    UserAccount getUserAccountById(Long userId);
}
