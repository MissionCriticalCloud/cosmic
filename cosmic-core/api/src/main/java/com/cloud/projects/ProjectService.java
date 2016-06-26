package com.cloud.projects;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.projects.ProjectAccount.Role;
import com.cloud.user.Account;

public interface ProjectService {
    /**
     * Creates a new project
     *
     * @param name        - project name
     * @param displayText - project display text
     * @param accountName - account name of the project owner
     * @param domainId    - domainid of the project owner
     * @return the project if created successfully, null otherwise
     * @throws ResourceAllocationException
     */
    Project createProject(String name, String displayText, String accountName, Long domainId) throws ResourceAllocationException;

    /**
     * Deletes a project
     *
     * @param id - project id
     * @return true if the project was deleted successfully, false otherwise
     */
    boolean deleteProject(long id);

    /**
     * Gets a project by id
     *
     * @param id - project id
     * @return project object
     */
    Project getProject(long id);

    ProjectAccount assignAccountToProject(Project project, long accountId, Role accountRole);

    Account getProjectOwner(long projectId);

    boolean unassignAccountFromProject(long projectId, long accountId);

    Project findByProjectAccountId(long projectAccountId);

    Project findByNameAndDomainId(String name, long domainId);

    Project updateProject(long id, String displayText, String newOwnerName) throws ResourceAllocationException;

    boolean addAccountToProject(long projectId, String accountName, String email);

    boolean deleteAccountFromProject(long projectId, String accountName);

    boolean updateInvitation(long projectId, String accountName, String token, boolean accept);

    Project activateProject(long projectId);

    Project suspendProject(long projectId) throws ConcurrentOperationException, ResourceUnavailableException;

    Project enableProject(long projectId);

    boolean deleteProjectInvitation(long invitationId);

    Project findByProjectAccountIdIncludingRemoved(long projectAccountId);
}
