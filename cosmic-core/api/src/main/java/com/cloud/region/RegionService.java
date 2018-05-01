package com.cloud.region;

import com.cloud.api.command.admin.account.DeleteAccountCmd;
import com.cloud.api.command.admin.account.DisableAccountCmd;
import com.cloud.api.command.admin.account.EnableAccountCmd;
import com.cloud.api.command.admin.account.UpdateAccountCmd;
import com.cloud.api.command.admin.domain.DeleteDomainCmd;
import com.cloud.api.command.admin.domain.UpdateDomainCmd;
import com.cloud.api.command.admin.user.DeleteUserCmd;
import com.cloud.api.command.admin.user.DisableUserCmd;
import com.cloud.api.command.admin.user.EnableUserCmd;
import com.cloud.api.command.admin.user.UpdateUserCmd;
import com.cloud.api.command.user.region.ListRegionsCmd;
import com.cloud.domain.Domain;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.UserAccount;

import java.util.List;

public interface RegionService {
    /**
     * Adds a Region to the local Region
     *
     * @param id
     * @param name
     * @param endPoint
     * @return Return added Region object
     */
    public Region addRegion(int id, String name, String endPoint);

    /**
     * Update details of the Region with specified Id
     *
     * @param id
     * @param name
     * @param endPoint
     * @return Return updated Region object
     */
    public Region updateRegion(int id, String name, String endPoint);

    /**
     * @param id
     * @return True if region is successfully removed
     */
    public boolean removeRegion(int id);

    /**
     * List all Regions or by Id/Name
     *
     * @param id
     * @param name
     * @return List of Regions
     */
    public List<? extends Region> listRegions(ListRegionsCmd cmd);

    /**
     * Deletes a user by userId
     * isPopagate flag is set to true if sent from peer Region
     *
     * @param cmd
     * @return true if delete was successful, false otherwise
     */
    boolean deleteUserAccount(DeleteAccountCmd cmd);

    /**
     * Updates an account
     * isPopagate falg is set to true if sent from peer Region
     *
     * @param cmd - the parameter containing accountId or account nameand domainId
     * @return updated account object
     */
    Account updateAccount(UpdateAccountCmd cmd);

    /**
     * Disables an account by accountName and domainId or accountId
     *
     * @param cmd
     * @return
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    Account disableAccount(DisableAccountCmd cmd) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * Enables an account by accountId
     *
     * @param cmd
     * @return
     */
    Account enableAccount(EnableAccountCmd cmd);

    /**
     * Deletes user by Id
     *
     * @param deleteUserCmd
     * @return true if delete was successful, false otherwise
     */
    boolean deleteUser(DeleteUserCmd deleteUserCmd);

    /**
     * update an existing domain
     *
     * @param cmd - the command containing domainId and new domainName
     * @return Domain object if the command succeeded
     */
    public Domain updateDomain(UpdateDomainCmd updateDomainCmd);

    /**
     * Deletes domain
     *
     * @param cmd
     * @return true if delete was successful, false otherwise
     */
    public boolean deleteDomain(DeleteDomainCmd cmd);

    /**
     * Update a user by userId
     *
     * @param userId
     * @return UserAccount object
     */
    public UserAccount updateUser(UpdateUserCmd updateUserCmd);

    /**
     * Disables a user by userId
     *
     * @param cmd
     * @return UserAccount object
     */
    public UserAccount disableUser(DisableUserCmd cmd);

    /**
     * Enables a user
     *
     * @param cmd
     * @return UserAccount object
     */
    public UserAccount enableUser(EnableUserCmd cmd);
}
