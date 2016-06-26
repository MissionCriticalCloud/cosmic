package com.cloud.projects;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.projects.ProjectAccount.Role;
import com.cloud.user.Account;
import com.cloud.utils.component.ManagerBase;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

public class MockProjectManagerImpl extends ManagerBase implements ProjectManager {

    @Override
    public Project createProject(final String name, final String displayText, final String accountName, final Long domainId) throws ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteProject(final long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Project getProject(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectAccount assignAccountToProject(final Project project, final long accountId, final Role accountRole) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getProjectOwner(final long projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean unassignAccountFromProject(final long projectId, final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Project findByProjectAccountId(final long projectAccountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Project findByNameAndDomainId(final String name, final long domainId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Project updateProject(final long id, final String displayText, final String newOwnerName) throws ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addAccountToProject(final long projectId, final String accountName, final String email) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteAccountFromProject(final long projectId, final String accountName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateInvitation(final long projectId, final String accountName, final String token, final boolean accept) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Project activateProject(final long projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Project suspendProject(final long projectId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Project enableProject(final long projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteProjectInvitation(final long invitationId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Project findByProjectAccountIdIncludingRemoved(final long projectAccountId) {
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canAccessProjectAccount(final Account caller, final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canModifyProjectAccount(final Account caller, final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteAccountFromProject(final long projectId, final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Long> listPermittedProjectAccounts(final long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean projectInviteRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean allowUserToCreateProject() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteProject(final Account caller, final long callerUserId, final ProjectVO project) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getInvitationTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }
}
