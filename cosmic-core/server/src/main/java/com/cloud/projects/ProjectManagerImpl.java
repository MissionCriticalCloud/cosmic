package com.cloud.projects;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.dao.ProjectAccountJoinDao;
import com.cloud.api.query.dao.ProjectInvitationJoinDao;
import com.cloud.api.query.dao.ProjectJoinDao;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.projects.Project.State;
import com.cloud.projects.ProjectAccount.Role;
import com.cloud.projects.dao.ProjectAccountDao;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.projects.dao.ProjectInvitationDao;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.DomainManager;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.User;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionCallbackWithExceptionNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectManagerImpl extends ManagerBase implements ProjectManager {
    public static final Logger s_logger = LoggerFactory.getLogger(ProjectManagerImpl.class);
    @Inject
    protected ResourceTagDao _resourceTagDao;
    protected boolean _invitationRequired = false;
    protected long _invitationTimeOut = 86400000;
    protected boolean _allowUserToCreateProject = true;
    protected ScheduledExecutorService _executor;
    protected int _projectCleanupExpInvInterval = 60; //Interval defining how often project invitation cleanup thread is running
    @Inject
    AccountManager _accountMgr;
    @Inject
    DomainManager _domainMgr;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    ResourceLimitService _resourceLimitMgr;
    private EmailInvite _emailInvite;
    @Inject
    private DomainDao _domainDao;
    @Inject
    private ProjectDao _projectDao;
    @Inject
    private ProjectJoinDao _projectJoinDao;
    @Inject
    private ProjectAccountDao _projectAccountDao;
    @Inject
    private ProjectAccountJoinDao _projectAccountJoinDao;
    @Inject
    private AccountDao _accountDao;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private ProjectInvitationDao _projectInvitationDao;
    @Inject
    private ProjectInvitationJoinDao _projectInvitationJoinDao;

    public static String generateToken(final int length) {
        final String charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final Random rand = new Random(System.currentTimeMillis());
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            final int pos = rand.nextInt(charset.length());
            sb.append(charset.charAt(pos));
        }
        return sb.toString();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        final Map<String, String> configs = _configDao.getConfiguration(params);
        _invitationRequired = Boolean.valueOf(configs.get(Config.ProjectInviteRequired.key()));

        final String value = configs.get(Config.ProjectInvitationExpirationTime.key());
        _invitationTimeOut = Long.parseLong(value != null ? value : "86400") * 1000;
        _allowUserToCreateProject = Boolean.valueOf(configs.get(Config.AllowUserToCreateProject.key()));

        // set up the email system for project invitations

        final String smtpHost = configs.get("project.smtp.host");
        final int smtpPort = NumbersUtil.parseInt(configs.get("project.smtp.port"), 25);
        final String useAuthStr = configs.get("project.smtp.useAuth");
        final boolean useAuth = ((useAuthStr == null) ? false : Boolean.parseBoolean(useAuthStr));
        final String smtpUsername = configs.get("project.smtp.username");
        final String smtpPassword = configs.get("project.smtp.password");
        final String emailSender = configs.get("project.email.sender");
        final String smtpDebugStr = configs.get("project.smtp.debug");
        boolean smtpDebug = false;
        if (smtpDebugStr != null) {
            smtpDebug = Boolean.parseBoolean(smtpDebugStr);
        }

        _emailInvite = new EmailInvite(smtpHost, smtpPort, useAuth, smtpUsername, smtpPassword, emailSender, smtpDebug);
        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Project-ExpireInvitations"));

        return true;
    }

    @Override
    public boolean start() {
        _executor.scheduleWithFixedDelay(new ExpiredInvitationsCleanup(), _projectCleanupExpInvInterval, _projectCleanupExpInvInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_CREATE, eventDescription = "creating project", create = true)
    @DB
    public Project createProject(final String name, final String displayText, final String accountName, final Long domainId) throws ResourceAllocationException {
        final Account caller = CallContext.current().getCallingAccount();
        Account owner = caller;

        //check if the user authorized to create the project
        if (_accountMgr.isNormalUser(caller.getId()) && !_allowUserToCreateProject) {
            throw new PermissionDeniedException("Regular user is not permitted to create a project");
        }

        //Verify request parameters
        if ((accountName != null && domainId == null) || (domainId != null && accountName == null)) {
            throw new InvalidParameterValueException("Account name and domain id must be specified together");
        }

        if (accountName != null) {
            owner = _accountMgr.finalizeOwner(caller, accountName, domainId, null);
        }

        //don't allow 2 projects with the same name inside the same domain
        if (_projectDao.findByNameAndDomain(name, owner.getDomainId()) != null) {
            throw new InvalidParameterValueException("Project with name " + name + " already exists in domain id=" + owner.getDomainId());
        }

        //do resource limit check
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.project);

        final Account ownerFinal = owner;
        return Transaction.execute(new TransactionCallback<Project>() {
            @Override
            public Project doInTransaction(final TransactionStatus status) {

                //Create an account associated with the project
                final StringBuilder acctNm = new StringBuilder("PrjAcct-");
                acctNm.append(name).append("-").append(ownerFinal.getDomainId());

                final Account projectAccount = _accountMgr.createAccount(acctNm.toString(), Account.ACCOUNT_TYPE_PROJECT, domainId, null, null, UUID.randomUUID().toString());

                final Project project = _projectDao.persist(new ProjectVO(name, displayText, ownerFinal.getDomainId(), projectAccount.getId()));

                //assign owner to the project
                assignAccountToProject(project, ownerFinal.getId(), ProjectAccount.Role.Admin);

                if (project != null) {
                    CallContext.current().setEventDetails("Project id=" + project.getId());
                    CallContext.current().putContextParameter(Project.class, project.getUuid());
                }

                //Increment resource count
                _resourceLimitMgr.incrementResourceCount(ownerFinal.getId(), ResourceType.project);

                return project;
            }
        });
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_DELETE, eventDescription = "deleting project", async = true)
    public boolean deleteProject(final long projectId) {
        final CallContext ctx = CallContext.current();

        final ProjectVO project = getProject(projectId);
        //verify input parameters
        if (project == null) {
            throw new InvalidParameterValueException("Unable to find project by id " + projectId);
        }

        _accountMgr.checkAccess(ctx.getCallingAccount(), AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        return deleteProject(ctx.getCallingAccount(), ctx.getCallingUserId(), project);
    }

    @DB
    private boolean cleanupProject(final Project project, final AccountVO caller, final Long callerUserId) {
        boolean result = true;
        //Delete project's account
        final AccountVO account = _accountDao.findById(project.getProjectAccountId());
        s_logger.debug("Deleting projects " + project + " internal account id=" + account.getId() + " as a part of project cleanup...");

        result = result && _accountMgr.deleteAccount(account, callerUserId, caller);

        if (result) {
            //Unassign all users from the project
            result = Transaction.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(final TransactionStatus status) {
                    boolean result = true;
                    s_logger.debug("Unassigning all accounts from project " + project + " as a part of project cleanup...");
                    final List<? extends ProjectAccount> projectAccounts = _projectAccountDao.listByProjectId(project.getId());
                    for (final ProjectAccount projectAccount : projectAccounts) {
                        result = result && unassignAccountFromProject(projectAccount.getProjectId(), projectAccount.getAccountId());
                    }

                    s_logger.debug("Removing all invitations for the project " + project + " as a part of project cleanup...");
                    _projectInvitationDao.cleanupInvitations(project.getId());

                    return result;
                }
            });
            if (result) {
                s_logger.debug("Accounts are unassign successfully from project " + project + " as a part of project cleanup...");
            }
        } else {
            s_logger.warn("Failed to cleanup project's internal account");
        }

        return result;
    }

    @Override
    public ProjectVO getProject(final long projectId) {
        return _projectDao.findById(projectId);
    }

    @Override
    public ProjectAccount assignAccountToProject(final Project project, final long accountId, final ProjectAccount.Role accountRole) {
        return _projectAccountDao.persist(new ProjectAccountVO(project, accountId, accountRole));
    }

    @Override
    public Account getProjectOwner(final long projectId) {
        final ProjectAccount prAcct = _projectAccountDao.getProjectOwner(projectId);
        if (prAcct != null) {
            return _accountMgr.getAccount(prAcct.getAccountId());
        }

        return null;
    }

    @Override
    public boolean unassignAccountFromProject(final long projectId, final long accountId) {
        final ProjectAccountVO projectAccount = _projectAccountDao.findByProjectIdAccountId(projectId, accountId);
        if (projectAccount == null) {
            s_logger.debug("Account id=" + accountId + " is not assigned to project id=" + projectId + " so no need to unassign");
            return true;
        }

        if (_projectAccountDao.remove(projectAccount.getId())) {
            return true;
        } else {
            s_logger.warn("Failed to unassign account id=" + accountId + " from the project id=" + projectId);
            return false;
        }
    }

    @Override
    public ProjectVO findByProjectAccountId(final long projectAccountId) {
        return _projectDao.findByProjectAccountId(projectAccountId);
    }

    @Override
    public Project findByNameAndDomainId(final String name, final long domainId) {
        return _projectDao.findByNameAndDomain(name, domainId);
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_UPDATE, eventDescription = "updating project", async = true)
    public Project updateProject(final long projectId, final String displayText, final String newOwnerName) throws ResourceAllocationException {
        final Account caller = CallContext.current().getCallingAccount();

        //check that the project exists
        final ProjectVO project = getProject(projectId);

        if (project == null) {
            throw new InvalidParameterValueException("Unable to find the project id=" + projectId);
        }

        //verify permissions
        _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        Transaction.execute(new TransactionCallbackWithExceptionNoReturn<ResourceAllocationException>() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) throws ResourceAllocationException {
                if (displayText != null) {
                    project.setDisplayText(displayText);
                    _projectDao.update(projectId, project);
                }

                if (newOwnerName != null) {
                    //check that the new owner exists
                    final Account futureOwnerAccount = _accountMgr.getActiveAccountByName(newOwnerName, project.getDomainId());
                    if (futureOwnerAccount == null) {
                        throw new InvalidParameterValueException("Unable to find account name=" + newOwnerName + " in domain id=" + project.getDomainId());
                    }
                    final Account currentOwnerAccount = getProjectOwner(projectId);
                    if (currentOwnerAccount.getId() != futureOwnerAccount.getId()) {
                        final ProjectAccountVO futureOwner = _projectAccountDao.findByProjectIdAccountId(projectId, futureOwnerAccount.getAccountId());
                        if (futureOwner == null) {
                            throw new InvalidParameterValueException("Account " + newOwnerName +
                                    " doesn't belong to the project. Add it to the project first and then change the project's ownership");
                        }

                        //do resource limit check
                        _resourceLimitMgr.checkResourceLimit(_accountMgr.getAccount(futureOwnerAccount.getId()), ResourceType.project);

                        //unset the role for the old owner
                        final ProjectAccountVO currentOwner = _projectAccountDao.findByProjectIdAccountId(projectId, currentOwnerAccount.getId());
                        currentOwner.setAccountRole(Role.Regular);
                        _projectAccountDao.update(currentOwner.getId(), currentOwner);
                        _resourceLimitMgr.decrementResourceCount(currentOwnerAccount.getId(), ResourceType.project);

                        //set new owner
                        futureOwner.setAccountRole(Role.Admin);
                        _projectAccountDao.update(futureOwner.getId(), futureOwner);
                        _resourceLimitMgr.incrementResourceCount(futureOwnerAccount.getId(), ResourceType.project);
                    } else {
                        s_logger.trace("Future owner " + newOwnerName + "is already the owner of the project id=" + projectId);
                    }
                }
            }
        });

        return _projectDao.findById(projectId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_ACCOUNT_ADD, eventDescription = "adding account to project", async = true)
    public boolean addAccountToProject(final long projectId, final String accountName, final String email) {
        final Account caller = CallContext.current().getCallingAccount();

        //check that the project exists
        final Project project = getProject(projectId);

        if (project == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find project with specified id");
            ex.addProxyObject(String.valueOf(projectId), "projectId");
            throw ex;
        }

        //User can be added to Active project only
        if (project.getState() != Project.State.Active) {
            final InvalidParameterValueException ex =
                    new InvalidParameterValueException("Can't add account to the specified project id in state=" + project.getState() + " as it's no longer active");
            ex.addProxyObject(project.getUuid(), "projectId");
            throw ex;
        }

        //check that account-to-add exists
        Account account = null;
        if (accountName != null) {
            account = _accountMgr.getActiveAccountByName(accountName, project.getDomainId());
            if (account == null) {
                final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find account name=" + accountName + " in specified domain id");
                final DomainVO domain = ApiDBUtils.findDomainById(project.getDomainId());
                String domainUuid = String.valueOf(project.getDomainId());
                if (domain != null) {
                    domainUuid = domain.getUuid();
                }
                ex.addProxyObject(domainUuid, "domainId");
                throw ex;
            }

            //verify permissions - only project owner can assign
            _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

            //Check if the account already added to the project
            final ProjectAccount projectAccount = _projectAccountDao.findByProjectIdAccountId(projectId, account.getId());
            if (projectAccount != null) {
                s_logger.debug("Account " + accountName + " already added to the project id=" + projectId);
                return true;
            }
        }

        if (_invitationRequired) {
            return inviteAccountToProject(project, account, email);
        } else {
            if (account == null) {
                throw new InvalidParameterValueException("Account information is required for assigning account to the project");
            }
            if (assignAccountToProject(project, account.getId(), ProjectAccount.Role.Regular) != null) {
                return true;
            } else {
                s_logger.warn("Failed to add account " + accountName + " to project id=" + projectId);
                return false;
            }
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_ACCOUNT_REMOVE, eventDescription = "removing account from project", async = true)
    public boolean deleteAccountFromProject(final long projectId, final String accountName) {
        final Account caller = CallContext.current().getCallingAccount();

        //check that the project exists
        final Project project = getProject(projectId);

        if (project == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find project with specified id");
            ex.addProxyObject(String.valueOf(projectId), "projectId");
            throw ex;
        }

        //check that account-to-remove exists
        final Account account = _accountMgr.getActiveAccountByName(accountName, project.getDomainId());
        if (account == null) {
            final InvalidParameterValueException ex =
                    new InvalidParameterValueException("Unable to find account name=" + accountName + " in domain id=" + project.getDomainId());
            final DomainVO domain = ApiDBUtils.findDomainById(project.getDomainId());
            String domainUuid = String.valueOf(project.getDomainId());
            if (domain != null) {
                domainUuid = domain.getUuid();
            }
            ex.addProxyObject(domainUuid, "domainId");
            throw ex;
        }

        //verify permissions
        _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        //Check if the account exists in the project
        final ProjectAccount projectAccount = _projectAccountDao.findByProjectIdAccountId(projectId, account.getId());
        if (projectAccount == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Account " + accountName + " is not assigned to the project with specified id");
            // Use the projectVO object and not the projectAccount object to inject the projectId.
            ex.addProxyObject(project.getUuid(), "projectId");
            throw ex;
        }

        //can't remove the owner of the project
        if (projectAccount.getAccountRole() == Role.Admin) {
            final InvalidParameterValueException ex =
                    new InvalidParameterValueException("Unable to delete account " + accountName +
                            " from the project with specified id as the account is the owner of the project");
            ex.addProxyObject(project.getUuid(), "projectId");
            throw ex;
        }

        return deleteAccountFromProject(projectId, account.getId());
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_INVITATION_UPDATE, eventDescription = "updating project invitation", async = true)
    public boolean updateInvitation(final long projectId, String accountName, final String token, final boolean accept) {
        final Account caller = CallContext.current().getCallingAccount();
        Long accountId = null;
        boolean result = true;

        //if accountname and token are null, default accountname to caller's account name
        if (accountName == null && token == null) {
            accountName = caller.getAccountName();
        }

        //check that the project exists
        final Project project = getProject(projectId);

        if (project == null) {
            throw new InvalidParameterValueException("Unable to find the project id=" + projectId);
        }

        if (accountName != null) {
            //check that account-to-remove exists
            final Account account = _accountMgr.getActiveAccountByName(accountName, project.getDomainId());
            if (account == null) {
                throw new InvalidParameterValueException("Unable to find account name=" + accountName + " in domain id=" + project.getDomainId());
            }

            //verify permissions
            _accountMgr.checkAccess(caller, null, true, account);

            accountId = account.getId();
        } else {
            accountId = caller.getId();
        }

        //check that invitation exists
        ProjectInvitationVO invite = null;
        if (token == null) {
            invite = _projectInvitationDao.findByAccountIdProjectId(accountId, projectId, ProjectInvitation.State.Pending);
        } else {
            invite = _projectInvitationDao.findPendingByTokenAndProjectId(token, projectId, ProjectInvitation.State.Pending);
        }

        if (invite != null) {
            if (!_projectInvitationDao.isActive(invite.getId(), _invitationTimeOut) && accept) {
                expireInvitation(invite);
                throw new InvalidParameterValueException("Invitation is expired for account id=" + accountName + " to the project id=" + projectId);
            } else {

                final ProjectInvitationVO inviteFinal = invite;
                final Long accountIdFinal = accountId;
                final String accountNameFinal = accountName;
                result = Transaction.execute(new TransactionCallback<Boolean>() {
                    @Override
                    public Boolean doInTransaction(final TransactionStatus status) {
                        boolean result = true;

                        final ProjectInvitation.State newState = accept ? ProjectInvitation.State.Completed : ProjectInvitation.State.Declined;

                        //update invitation
                        s_logger.debug("Marking invitation " + inviteFinal + " with state " + newState);
                        inviteFinal.setState(newState);
                        result = _projectInvitationDao.update(inviteFinal.getId(), inviteFinal);

                        if (result && accept) {
                            //check if account already exists for the project (was added before invitation got accepted)
                            final ProjectAccount projectAccount = _projectAccountDao.findByProjectIdAccountId(projectId, accountIdFinal);
                            if (projectAccount != null) {
                                s_logger.debug("Account " + accountNameFinal + " already added to the project id=" + projectId);
                            } else {
                                assignAccountToProject(project, accountIdFinal, ProjectAccount.Role.Regular);
                            }
                        } else {
                            s_logger.warn("Failed to update project invitation " + inviteFinal + " with state " + newState);
                        }

                        return result;
                    }
                });
            }
        } else {
            throw new InvalidParameterValueException("Unable to find invitation for account name=" + accountName + " to the project id=" + projectId);
        }

        return result;
    }

    private boolean expireInvitation(final ProjectInvitationVO invite) {
        s_logger.debug("Expiring invitation id=" + invite.getId());
        invite.setState(ProjectInvitation.State.Expired);
        return _projectInvitationDao.update(invite.getId(), invite);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_ACTIVATE, eventDescription = "activating project")
    @DB
    public Project activateProject(final long projectId) {
        final Account caller = CallContext.current().getCallingAccount();

        //check that the project exists
        final ProjectVO project = getProject(projectId);

        if (project == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find project with specified id");
            ex.addProxyObject(String.valueOf(projectId), "projectId");
            throw ex;
        }

        //verify permissions
        _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        //allow project activation only when it's in Suspended state
        final Project.State currentState = project.getState();

        if (currentState == State.Active) {
            s_logger.debug("The project id=" + projectId + " is already active, no need to activate it again");
            return project;
        }

        if (currentState != State.Suspended) {
            throw new InvalidParameterValueException("Can't activate the project in " + currentState + " state");
        }

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                project.setState(Project.State.Active);
                _projectDao.update(projectId, project);

                _accountMgr.enableAccount(project.getProjectAccountId());
            }
        });

        return _projectDao.findById(projectId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_SUSPEND, eventDescription = "suspending project", async = true)
    public Project suspendProject(final long projectId) throws ConcurrentOperationException, ResourceUnavailableException {
        final Account caller = CallContext.current().getCallingAccount();

        final ProjectVO project = getProject(projectId);
        //verify input parameters
        if (project == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find project with specified id");
            ex.addProxyObject(String.valueOf(projectId), "projectId");
            throw ex;
        }

        _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        if (suspendProject(project)) {
            s_logger.debug("Successfully suspended project id=" + projectId);
            return _projectDao.findById(projectId);
        } else {
            final CloudRuntimeException ex = new CloudRuntimeException("Failed to suspend project with specified id");
            ex.addProxyObject(project.getUuid(), "projectId");
            throw ex;
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_CREATE, eventDescription = "creating project", async = true)
    @DB
    public Project enableProject(final long projectId) {
        final Account caller = CallContext.current().getCallingAccount();

        final ProjectVO project = getProject(projectId);
        //verify input parameters
        if (project == null) {
            throw new InvalidParameterValueException("Unable to find project by id " + projectId);
        }

        _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        //at this point enabling project doesn't require anything, so just update the state
        project.setState(State.Active);
        _projectDao.update(projectId, project);

        return project;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_PROJECT_INVITATION_REMOVE, eventDescription = "removing project invitation", async = true)
    public boolean deleteProjectInvitation(final long id) {
        final Account caller = CallContext.current().getCallingAccount();

        final ProjectInvitation invitation = _projectInvitationDao.findById(id);
        if (invitation == null) {
            throw new InvalidParameterValueException("Unable to find project invitation by id " + id);
        }

        //check that the project exists
        final Project project = getProject(invitation.getProjectId());

        //check permissions - only project owner can remove the invitations
        _accountMgr.checkAccess(caller, AccessType.ModifyProject, true, _accountMgr.getAccount(project.getProjectAccountId()));

        if (_projectInvitationDao.remove(id)) {
            s_logger.debug("Project Invitation id=" + id + " is removed");
            return true;
        } else {
            s_logger.debug("Failed to remove project invitation id=" + id);
            return false;
        }
    }

    @Override
    public ProjectVO findByProjectAccountIdIncludingRemoved(final long projectAccountId) {
        return _projectDao.findByProjectAccountIdIncludingRemoved(projectAccountId);
    }

    private boolean suspendProject(final ProjectVO project) throws ConcurrentOperationException, ResourceUnavailableException {

        s_logger.debug("Marking project " + project + " with state " + State.Suspended + " as a part of project suspend...");
        project.setState(State.Suspended);
        final boolean updateResult = _projectDao.update(project.getId(), project);

        if (updateResult) {
            final long projectAccountId = project.getProjectAccountId();
            if (!_accountMgr.disableAccount(projectAccountId)) {
                s_logger.warn("Failed to suspend all project's " + project + " resources; the resources will be suspended later by background thread");
            }
        } else {
            throw new CloudRuntimeException("Failed to mark the project " + project + " with state " + State.Suspended);
        }
        return true;
    }

    @Override
    public boolean canAccessProjectAccount(final Account caller, final long accountId) {
        //ROOT admin always can access the project
        if (_accountMgr.isRootAdmin(caller.getId())) {
            return true;
        } else if (_accountMgr.isDomainAdmin(caller.getId())) {
            final Account owner = _accountMgr.getAccount(accountId);
            _accountMgr.checkAccess(caller, _domainDao.findById(owner.getDomainId()));
            return true;
        }

        return _projectAccountDao.canAccessProjectAccount(caller.getId(), accountId);
    }

    @Override
    public boolean canModifyProjectAccount(final Account caller, final long accountId) {
        //ROOT admin always can access the project
        if (_accountMgr.isRootAdmin(caller.getId())) {
            return true;
        } else if (_accountMgr.isDomainAdmin(caller.getId())) {
            final Account owner = _accountMgr.getAccount(accountId);
            _accountMgr.checkAccess(caller, _domainDao.findById(owner.getDomainId()));
            return true;
        }
        return _projectAccountDao.canModifyProjectAccount(caller.getId(), accountId);
    }

    @Override
    @DB
    public boolean deleteAccountFromProject(final long projectId, final long accountId) {
        return Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                boolean success = true;

                //remove account
                final ProjectAccountVO projectAccount = _projectAccountDao.findByProjectIdAccountId(projectId, accountId);
                success = _projectAccountDao.remove(projectAccount.getId());

                //remove all invitations for account
                if (success) {
                    s_logger.debug("Removed account " + accountId + " from project " + projectId + " , cleaning up old invitations for account/project...");
                    final ProjectInvitation invite = _projectInvitationDao.findByAccountIdProjectId(accountId, projectId);
                    if (invite != null) {
                        success = success && _projectInvitationDao.remove(invite.getId());
                    }
                }

                return success;
            }
        });
    }

    @Override
    public List<Long> listPermittedProjectAccounts(final long accountId) {
        return _projectAccountDao.listPermittedAccountIds(accountId);
    }

    @Override
    public boolean projectInviteRequired() {
        return _invitationRequired;
    }

    @Override
    public boolean allowUserToCreateProject() {
        return _allowUserToCreateProject;
    }

    @DB
    @Override
    public boolean deleteProject(final Account caller, final long callerUserId, final ProjectVO project) {
        //mark project as inactive first, so you can't add resources to it
        final boolean updateResult = Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                s_logger.debug("Marking project id=" + project.getId() + " with state " + State.Disabled + " as a part of project delete...");
                project.setState(State.Disabled);
                final boolean updateResult = _projectDao.update(project.getId(), project);
                //owner can be already removed at this point, so adding the conditional check
                final Account projectOwner = getProjectOwner(project.getId());
                if (projectOwner != null) {
                    _resourceLimitMgr.decrementResourceCount(projectOwner.getId(), ResourceType.project);
                }

                return updateResult;
            }
        });

        if (updateResult) {
            //pass system caller when clenaup projects account
            if (!cleanupProject(project, _accountDao.findById(Account.ACCOUNT_ID_SYSTEM), User.UID_SYSTEM)) {
                s_logger.warn("Failed to cleanup project's id=" + project.getId() + " resources, not removing the project yet");
                return false;
            } else {
                return _projectDao.remove(project.getId());
            }
        } else {
            s_logger.warn("Failed to mark the project id=" + project.getId() + " with state " + State.Disabled);
            return false;
        }
    }

    @Override
    public long getInvitationTimeout() {
        return _invitationTimeOut;
    }

    private boolean inviteAccountToProject(final Project project, final Account account, final String email) {
        if (account != null) {
            if (createAccountInvitation(project, account.getId()) != null) {
                return true;
            } else {
                s_logger.warn("Failed to generate invitation for account " + account.getAccountName() + " to project id=" + project);
                return false;
            }
        }

        if (email != null) {
            //generate the token
            final String token = generateToken(10);
            if (generateTokenBasedInvitation(project, email, token) != null) {
                return true;
            } else {
                s_logger.warn("Failed to generate invitation for email " + email + " to project id=" + project);
                return false;
            }
        }

        return false;
    }

    public ProjectInvitation createAccountInvitation(final Project project, final Long accountId) {
        if (activeInviteExists(project, accountId, null)) {
            throw new InvalidParameterValueException("There is already a pending invitation for account id=" + accountId + " to the project id=" + project);
        }

        final ProjectInvitation invitation = _projectInvitationDao.persist(new ProjectInvitationVO(project.getId(), accountId, project.getDomainId(), null, null));

        return invitation;
    }

    @DB
    public boolean activeInviteExists(final Project project, final Long accountId, final String email) {
        return Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                //verify if the invitation was already generated
                ProjectInvitationVO invite = null;
                if (accountId != null) {
                    invite = _projectInvitationDao.findByAccountIdProjectId(accountId, project.getId());
                } else if (email != null) {
                    invite = _projectInvitationDao.findByEmailAndProjectId(email, project.getId());
                }

                if (invite != null) {
                    if (invite.getState() == ProjectInvitation.State.Completed ||
                            (invite.getState() == ProjectInvitation.State.Pending && _projectInvitationDao.isActive(invite.getId(), _invitationTimeOut))) {
                        return true;
                    } else {
                        if (invite.getState() == ProjectInvitation.State.Pending) {
                            expireInvitation(invite);
                        }
                        //remove the expired/declined invitation
                        if (accountId != null) {
                            s_logger.debug("Removing invitation in state " + invite.getState() + " for account id=" + accountId + " to project " + project);
                        } else if (email != null) {
                            s_logger.debug("Removing invitation in state " + invite.getState() + " for email " + email + " to project " + project);
                        }

                        _projectInvitationDao.expunge(invite.getId());
                    }
                }

                return false;
            }
        });
    }

    public ProjectInvitation generateTokenBasedInvitation(final Project project, final String email, final String token) {
        //verify if the invitation was already generated
        if (activeInviteExists(project, null, email)) {
            throw new InvalidParameterValueException("There is already a pending invitation for email " + email + " to the project id=" + project);
        }

        final ProjectInvitation projectInvitation = _projectInvitationDao.persist(new ProjectInvitationVO(project.getId(), null, project.getDomainId(), email, token));
        try {
            _emailInvite.sendInvite(token, email, project.getId());
        } catch (final Exception ex) {
            s_logger.warn("Failed to send project id=" + project + " invitation to the email " + email + "; removing the invitation record from the db", ex);
            _projectInvitationDao.remove(projectInvitation.getId());
            return null;
        }

        return projectInvitation;
    }

    class EmailInvite {
        private final String _smtpHost;
        private final String _smtpUsername;
        private final String _smtpPassword;
        private final String _emailSender;
        private final Session _smtpSession;
        private int _smtpPort = -1;
        private boolean _smtpUseAuth = false;

        public EmailInvite(final String smtpHost, final int smtpPort, final boolean smtpUseAuth, final String smtpUsername, final String smtpPassword, final String emailSender,
                           final boolean smtpDebug) {
            _smtpHost = smtpHost;
            _smtpPort = smtpPort;
            _smtpUseAuth = smtpUseAuth;
            _smtpUsername = smtpUsername;
            _smtpPassword = smtpPassword;
            _emailSender = emailSender;

            if (_smtpHost != null) {
                final Properties smtpProps = new Properties();
                smtpProps.put("mail.smtp.host", smtpHost);
                smtpProps.put("mail.smtp.port", smtpPort);
                smtpProps.put("mail.smtp.auth", "" + smtpUseAuth);
                if (smtpUsername != null) {
                    smtpProps.put("mail.smtp.user", smtpUsername);
                }

                smtpProps.put("mail.smtps.host", smtpHost);
                smtpProps.put("mail.smtps.port", smtpPort);
                smtpProps.put("mail.smtps.auth", "" + smtpUseAuth);
                if (smtpUsername != null) {
                    smtpProps.put("mail.smtps.user", smtpUsername);
                }

                if ((smtpUsername != null) && (smtpPassword != null)) {
                    _smtpSession = Session.getInstance(smtpProps, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(smtpUsername, smtpPassword);
                        }
                    });
                } else {
                    _smtpSession = Session.getInstance(smtpProps);
                }
                _smtpSession.setDebug(smtpDebug);
            } else {
                _smtpSession = null;
            }
        }

        public void sendInvite(final String token, final String email, final long projectId) throws MessagingException, UnsupportedEncodingException {
            if (_smtpSession != null) {
                InternetAddress address = null;
                if (email != null) {
                    try {
                        address = new InternetAddress(email, email);
                    } catch (final Exception ex) {
                        s_logger.error("Exception creating address for: " + email, ex);
                    }
                }

                final String content = "You've been invited to join the CloudStack project id=" + projectId + ". Please use token " + token + " to complete registration";

                final SMTPMessage msg = new SMTPMessage(_smtpSession);
                msg.setSender(new InternetAddress(_emailSender, _emailSender));
                msg.setFrom(new InternetAddress(_emailSender, _emailSender));
                msg.addRecipient(RecipientType.TO, address);
                msg.setSubject("You are invited to join the cloud stack project id=" + projectId);
                msg.setSentDate(new Date(DateUtil.currentGMTTime().getTime() >> 10));
                msg.setContent(content, "text/plain");
                msg.saveChanges();

                SMTPTransport smtpTrans = null;
                if (_smtpUseAuth) {
                    smtpTrans = new SMTPSSLTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                } else {
                    smtpTrans = new SMTPTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                }
                smtpTrans.connect();
                smtpTrans.sendMessage(msg, msg.getAllRecipients());
                smtpTrans.close();
            } else {
                throw new CloudRuntimeException("Unable to send email invitation; smtp ses");
            }
        }
    }

    public class ExpiredInvitationsCleanup extends ManagedContextRunnable {
        @Override
        protected void runInContext() {
            try {
                TimeZone.getDefault();
                final List<ProjectInvitationVO> invitationsToExpire = _projectInvitationDao.listInvitationsToExpire(_invitationTimeOut);
                if (!invitationsToExpire.isEmpty()) {
                    s_logger.debug("Found " + invitationsToExpire.size() + " projects to expire");
                    for (final ProjectInvitationVO invitationToExpire : invitationsToExpire) {
                        invitationToExpire.setState(ProjectInvitation.State.Expired);
                        _projectInvitationDao.update(invitationToExpire.getId(), invitationToExpire);
                        s_logger.trace("Expired project invitation id=" + invitationToExpire.getId());
                    }
                }
            } catch (final Exception ex) {
                s_logger.warn("Exception while running expired invitations cleanup", ex);
            }
        }
    }
}
