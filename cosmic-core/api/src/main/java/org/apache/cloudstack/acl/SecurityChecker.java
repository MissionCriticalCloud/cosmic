package org.apache.cloudstack.acl;

import com.cloud.dc.DataCenter;
import com.cloud.domain.Domain;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.component.Adapter;

/**
 * SecurityChecker checks the ownership and access control to objects within
 */
public interface SecurityChecker extends Adapter {

    /**
     * Checks if the account owns the object.
     *
     * @param caller account to check against.
     * @param object object that the account is trying to access.
     * @return true if access allowed. false if this adapter cannot authenticate ownership.
     * @throws PermissionDeniedException if this adapter is suppose to authenticate ownership and the check failed.
     */
    boolean checkAccess(Account caller, Domain domain) throws PermissionDeniedException;

    /**
     * Checks if the user belongs to an account that owns the object.
     *
     * @param user   user to check against.
     * @param object object that the account is trying to access.
     * @return true if access allowed. false if this adapter cannot authenticate ownership.
     * @throws PermissionDeniedException if this adapter is suppose to authenticate ownership and the check failed.
     */
    boolean checkAccess(User user, Domain domain) throws PermissionDeniedException;

    /**
     * Checks if the account can access the object.
     *
     * @param caller     account to check against.
     * @param entity     object that the account is trying to access.
     * @param accessType TODO
     * @return true if access allowed. false if this adapter cannot provide
     * permission.
     * @throws PermissionDeniedException if this adapter is suppose to authenticate ownership and the
     *                                   check failed.
     */
    boolean checkAccess(Account caller, ControlledEntity entity, AccessType accessType)
            throws PermissionDeniedException;

    /**
     * Checks if the account can access the object.
     *
     * @param caller     account to check against.
     * @param entity     object that the account is trying to access.
     * @param accessType TODO
     * @param action     name of the API
     * @return true if access allowed. false if this adapter cannot provide
     * permission.
     * @throws PermissionDeniedException if this adapter is suppose to authenticate ownership and the
     *                                   check failed.
     */
    boolean checkAccess(Account caller, ControlledEntity entity, AccessType accessType, String action) throws PermissionDeniedException;

    /**
     * Checks if the account can access multiple objects.
     *
     * @param caller     account to check against.
     * @param entities   objects that the account is trying to access.
     * @param accessType TODO
     * @param action     name of the API
     * @return true if access allowed. false if this adapter cannot provide
     * permission.
     * @throws PermissionDeniedException if this adapter is suppose to authenticate ownership and the
     *                                   check failed.
     */
    boolean checkAccess(Account caller, AccessType accessType, String action, ControlledEntity... entities)
            throws PermissionDeniedException;

    /**
     * Checks if the user belongs to an account that can access the object.
     *
     * @param user   user to check against.
     * @param entity object that the account is trying to access.
     * @return true if access allowed. false if this adapter cannot authenticate ownership.
     * @throws PermissionDeniedException if this adapter is suppose to authenticate ownership and the check failed.
     */
    boolean checkAccess(User user, ControlledEntity entity) throws PermissionDeniedException;

    boolean checkAccess(Account account, DataCenter zone) throws PermissionDeniedException;

    public boolean checkAccess(Account account, ServiceOffering so) throws PermissionDeniedException;

    boolean checkAccess(Account account, DiskOffering dof) throws PermissionDeniedException;

    public enum AccessType {
        ModifyProject,
        OperateEntry,
        UseEntry,
        ListEntry
    }
}
