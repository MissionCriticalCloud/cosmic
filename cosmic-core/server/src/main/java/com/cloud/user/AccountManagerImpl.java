package com.cloud.user;

import com.cloud.acl.ControlledEntity;
import com.cloud.acl.QuerySelector;
import com.cloud.acl.RoleType;
import com.cloud.acl.SecurityChecker;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.affinity.AffinityGroup;
import com.cloud.affinity.dao.AffinityGroupDao;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.command.admin.account.UpdateAccountCmd;
import com.cloud.api.command.admin.user.DeleteUserCmd;
import com.cloud.api.command.admin.user.RegisterCmd;
import com.cloud.api.command.admin.user.UpdateUserCmd;
import com.cloud.api.query.vo.ControlledViewEntity;
import com.cloud.config.ApiServiceConfiguration;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.ResourceCountVO;
import com.cloud.configuration.ResourceLimit;
import com.cloud.configuration.dao.ResourceCountDao;
import com.cloud.configuration.dao.ResourceLimitDao;
import com.cloud.context.CallContext;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DataCenterVnetDao;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.event.ActionEvent;
import com.cloud.event.ActionEventUtils;
import com.cloud.event.ActionEvents;
import com.cloud.event.EventTypes;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.CloudAuthenticationException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.messagebus.MessageBus;
import com.cloud.framework.messagebus.PublishScope;
import com.cloud.managed.context.ManagedContextRunnable;
import com.cloud.network.IpAddress;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.VpnUserVO;
import com.cloud.network.dao.AccountGuestVlanMapDao;
import com.cloud.network.dao.AccountGuestVlanMapVO;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.RemoteAccessVpnDao;
import com.cloud.network.dao.RemoteAccessVpnVO;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpn.RemoteAccessVpnService;
import com.cloud.network.vpn.Site2SiteVpnManager;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.projects.Project;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.projects.ProjectInvitationVO;
import com.cloud.projects.ProjectManager;
import com.cloud.projects.ProjectVO;
import com.cloud.projects.dao.ProjectAccountDao;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.server.auth.UserAuthenticator;
import com.cloud.server.auth.UserAuthenticator.ActionOnFailedAuthentication;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeApiService;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.snapshot.SnapshotManager;
import com.cloud.template.TemplateManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account.State;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserAccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.InstanceGroupVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.ReservationContextImpl;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.InstanceGroupDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.VMSnapshotManager;
import com.cloud.vm.snapshot.VMSnapshotVO;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagerImpl extends ManagerBase implements AccountManager, Manager {
    public static final Logger s_logger = LoggerFactory.getLogger(AccountManagerImpl.class);
    private final ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("AccountChecker"));
    @Inject
    protected SnapshotDao _snapshotDao;
    @Inject
    protected VMTemplateDao _vmTemplateDao;
    @Inject
    protected IpAddressManager _ipAddrMgr;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    Site2SiteVpnManager _vpnMgr;
    @Inject
    VolumeApiService volumeService;
    List<QuerySelector> _querySelectors;
    @Inject
    MessageBus _messageBus;
    List<UserAuthenticator> _userPasswordEncoders;
    int _allowedLoginAttempts;
    UserVO _systemUser;
    AccountVO _systemAccount;
    List<SecurityChecker> _securityCheckers;
    int _cleanupInterval;
    @Inject
    private AccountDao _accountDao;
    @Inject
    private AccountManager _accountMgr;
    @Inject
    private ResourceCountDao _resourceCountDao;
    @Inject
    private UserDao _userDao;
    @Inject
    private InstanceGroupDao _vmGroupDao;
    @Inject
    private UserAccountDao _userAccountDao;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private UserVmDao _userVmDao;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private VMInstanceDao _vmDao;
    @Inject
    private NetworkOrchestrationService _networkMgr;
    @Inject
    private SnapshotManager _snapMgr;
    @Inject
    private VMSnapshotManager _vmSnapshotMgr;
    @Inject
    private VMSnapshotDao _vmSnapshotDao;
    @Inject
    private UserVmManager _vmMgr;
    @Inject
    private TemplateManager _tmpltMgr;
    @Inject
    private ConfigurationManager _configMgr;
    @Inject
    private VirtualMachineManager _itMgr;
    @Inject
    private RemoteAccessVpnDao _remoteAccessVpnDao;
    @Inject
    private RemoteAccessVpnService _remoteAccessVpnMgr;
    @Inject
    private VpnUserDao _vpnUser;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private DomainManager _domainMgr;
    @Inject
    private ProjectManager _projectMgr;
    @Inject
    private ProjectDao _projectDao;
    @Inject
    private AccountDetailsDao _accountDetailsDao;
    @Inject
    private DomainDao _domainDao;
    @Inject
    private ProjectAccountDao _projectAccountDao;
    @Inject
    private IPAddressDao _ipAddressDao;
    @Inject
    private VpcManager _vpcMgr;
    @Inject
    private AffinityGroupDao _affinityGroupDao;
    @Inject
    private AccountGuestVlanMapDao _accountGuestVlanMapDao;
    @Inject
    private DataCenterVnetDao _dataCenterVnetDao;
    @Inject
    private ResourceLimitService _resourceLimitMgr;
    @Inject
    private ResourceLimitDao _resourceLimitDao;
    @Inject
    private DedicatedResourceDao _dedicatedDao;
    private List<UserAuthenticator> _userAuthenticators;

    public List<UserAuthenticator> getUserAuthenticators() {
        return _userAuthenticators;
    }

    public void setUserAuthenticators(final List<UserAuthenticator> authenticators) {
        _userAuthenticators = authenticators;
    }

    public List<UserAuthenticator> getUserPasswordEncoders() {
        return _userPasswordEncoders;
    }

    public void setUserPasswordEncoders(final List<UserAuthenticator> encoders) {
        _userPasswordEncoders = encoders;
    }

    public List<SecurityChecker> getSecurityCheckers() {
        return _securityCheckers;
    }

    public void setSecurityCheckers(final List<SecurityChecker> securityCheckers) {
        _securityCheckers = securityCheckers;
    }

    public List<QuerySelector> getQuerySelectors() {
        return _querySelectors;
    }

    public void setQuerySelectors(final List<QuerySelector> querySelectors) {
        _querySelectors = querySelectors;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _systemAccount = _accountDao.findById(Account.ACCOUNT_ID_SYSTEM);
        if (_systemAccount == null) {
            throw new ConfigurationException("Unable to find the system account using " + Account.ACCOUNT_ID_SYSTEM);
        }

        _systemUser = _userDao.findById(User.UID_SYSTEM);
        if (_systemUser == null) {
            throw new ConfigurationException("Unable to find the system user using " + User.UID_SYSTEM);
        }

        final Map<String, String> configs = _configDao.getConfiguration(params);

        final String loginAttempts = configs.get(Config.IncorrectLoginAttemptsAllowed.key());
        _allowedLoginAttempts = NumbersUtil.parseInt(loginAttempts, 5);

        final String value = configs.get(Config.AccountCleanupInterval.key());
        _cleanupInterval = NumbersUtil.parseInt(value, 60 * 60 * 24); // 1 day.

        return true;
    }

    @Override
    public boolean start() {
        _executor.scheduleAtFixedRate(new AccountCleanupTask(), _cleanupInterval, _cleanupInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    @ActionEvents({
            @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_CREATE, eventDescription = "creating Account"),
            @ActionEvent(eventType = EventTypes.EVENT_USER_CREATE, eventDescription = "creating User")
    })
    public UserAccount createUserAccount(final String userName, final String password, final String firstName, final String lastName, final String email, final String timezone,
                                         final String accountName, final short accountType, final Long domainId, final String networkDomain, final Map<String, String> details,
                                         final String accountUUID, final String userUUID) {

        return createUserAccount(userName, password, firstName, lastName, email, timezone, accountName, accountType, domainId, networkDomain, details, accountUUID, userUUID,
                User.Source.UNKNOWN);
    }

    @Override
    @DB
    @ActionEvents({
            @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_CREATE, eventDescription = "creating Account"),
            @ActionEvent(eventType = EventTypes.EVENT_USER_CREATE, eventDescription = "creating User")
    })
    public UserAccount createUserAccount(final String userName, final String password, final String firstName, final String lastName, final String email,
                                         final String timezone, String accountName, final short accountType, Long domainId, final String networkDomain, final Map<String, String>
                                                 details,
                                         final String accountUUID, final String userUUID, final User.Source source) {

        if (accountName == null) {
            accountName = userName;
        }
        if (domainId == null) {
            domainId = Domain.ROOT_DOMAIN;
        }

        if (StringUtils.isEmpty(userName)) {
            throw new InvalidParameterValueException("Username is empty");
        }

        if (StringUtils.isEmpty(firstName)) {
            throw new InvalidParameterValueException("Firstname is empty");
        }

        if (StringUtils.isEmpty(lastName)) {
            throw new InvalidParameterValueException("Lastname is empty");
        }

        // Validate domain
        final Domain domain = _domainMgr.getDomain(domainId);
        if (domain == null) {
            throw new InvalidParameterValueException("The domain " + domainId + " does not exist; unable to create account");
        }

        // Check permissions
        checkAccess(CallContext.current().getCallingAccount(), domain);

        if (!_userAccountDao.validateUsernameInDomain(userName, domainId)) {
            throw new InvalidParameterValueException("The user " + userName + " already exists in domain " + domainId);
        }

        if (networkDomain != null && networkDomain.length() > 0) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters " +
                                "'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        final String accountNameFinal = accountName;
        final Long domainIdFinal = domainId;
        final String accountUUIDFinal = accountUUID;
        final Pair<Long, Account> pair = Transaction.execute(new TransactionCallback<Pair<Long, Account>>() {
            @Override
            public Pair<Long, Account> doInTransaction(final TransactionStatus status) {
                // create account
                String accountUUID = accountUUIDFinal;
                if (accountUUID == null) {
                    accountUUID = UUID.randomUUID().toString();
                }
                final AccountVO account = createAccount(accountNameFinal, accountType, domainIdFinal, networkDomain, details, accountUUID);
                final long accountId = account.getId();

                // create the first user for the account
                final UserVO user = createUser(accountId, userName, password, firstName, lastName, email, timezone, userUUID, source);

                if (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
                    // set registration token
                    final byte[] bytes = (domainIdFinal + accountNameFinal + userName + System.currentTimeMillis()).getBytes();
                    final String registrationToken = UUID.nameUUIDFromBytes(bytes).toString();
                    user.setRegistrationToken(registrationToken);
                }

                return new Pair<>(user.getId(), account);
            }
        });

        final long userId = pair.first();
        final Account account = pair.second();

        // create correct account and group association based on accountType
        if (accountType != Account.ACCOUNT_TYPE_PROJECT) {
            final Map<Long, Long> accountGroupMap = new HashMap<>();
            accountGroupMap.put(account.getId(), new Long(accountType + 1));
            _messageBus.publish(_name, MESSAGE_ADD_ACCOUNT_EVENT, PublishScope.LOCAL, accountGroupMap);
        }

        CallContext.current().putContextParameter(Account.class, account.getUuid());

        // check success
        return _userAccountDao.findById(userId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_LOCK, eventDescription = "locking User")
    public UserAccount lockUser(final long userId) {
        final Account caller = CallContext.current().getCallingAccount();

        // Check if user with id exists in the system
        final User user = _userDao.findById(userId);
        if (user == null || user.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find user by id");
        }

        final Account account = _accountDao.findById(user.getAccountId());
        if (account == null) {
            throw new InvalidParameterValueException("unable to find user account " + user.getAccountId());
        }

        // don't allow to lock user of the account of type Project
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find user by id");
        }

        // If the user is a System user, return an error. We do not allow this
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("user id : " + userId + " is a system user, locking is not allowed");
        }

        checkAccess(caller, AccessType.OperateEntry, true, account);

        // make sure the account is enabled too
        // if the user is either locked already or disabled already, don't change state...only lock currently enabled
        // users
        boolean success;
        if (user.getState().equals(State.locked)) {
            // already locked...no-op
            return _userAccountDao.findById(userId);
        } else if (user.getState().equals(State.enabled)) {
            success = doSetUserStatus(user.getId(), State.locked);

            boolean lockAccount = true;
            final List<UserVO> allUsersByAccount = _userDao.listByAccount(user.getAccountId());
            for (final UserVO oneUser : allUsersByAccount) {
                if (oneUser.getState().equals(State.enabled)) {
                    lockAccount = false;
                    break;
                }
            }

            if (lockAccount) {
                success = (success && lockAccount(user.getAccountId()));
            }
        } else {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Attempting to lock a non-enabled user, current state is " + user.getState() + " (userId: " + user.getId() + "), locking failed.");
            }
            success = false;
        }

        if (success) {

            CallContext.current().putContextParameter(User.class, user.getUuid());

            return _userAccountDao.findById(userId);
        } else {
            throw new CloudRuntimeException("Unable to lock user " + userId);
        }
    }

    @Override
    public AccountVO getSystemAccount() {
        if (_systemAccount == null) {
            _systemAccount = _accountDao.findById(Account.ACCOUNT_ID_SYSTEM);
        }
        return _systemAccount;
    }

    @Override
    public UserVO getSystemUser() {
        if (_systemUser == null) {
            _systemUser = _userDao.findById(User.UID_SYSTEM);
        }
        return _systemUser;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_CREATE, eventDescription = "creating User")
    public UserVO createUser(final String userName, final String password, final String firstName, final String lastName, final String email, final String timeZone, final String
            accountName, final Long domainId,
                             final String userUUID) {

        return createUser(userName, password, firstName, lastName, email, timeZone, accountName, domainId, userUUID, User.Source.UNKNOWN);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_CREATE, eventDescription = "creating User")
    public UserVO createUser(final String userName, final String password, final String firstName, final String lastName, final String email, final String timeZone, final String
            accountName, Long domainId,
                             final String userUUID, final User.Source source) {
        // default domain to ROOT if not specified
        if (domainId == null) {
            domainId = Domain.ROOT_DOMAIN;
        }

        final Domain domain = _domainMgr.getDomain(domainId);
        if (domain == null) {
            throw new CloudRuntimeException("The domain " + domainId + " does not exist; unable to create user");
        } else if (domain.getState().equals(Domain.State.Inactive)) {
            throw new CloudRuntimeException("The user cannot be created as domain " + domain.getName() + " is being deleted");
        }

        checkAccess(CallContext.current().getCallingAccount(), domain);

        final Account account = _accountDao.findEnabledAccount(accountName, domainId);
        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain id=" + domainId + " to create user");
        }

        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("Account id : " + account.getId() + " is a system account, can't add a user to it");
        }

        if (!_userAccountDao.validateUsernameInDomain(userName, domainId)) {
            throw new CloudRuntimeException("The user " + userName + " already exists in domain " + domainId);
        }
        final UserVO user;
        user = createUser(account.getId(), userName, password, firstName, lastName, email, timeZone, userUUID, source);
        return user;
    }

    @Override
    public boolean isAdmin(final Long accountId) {
        if (accountId != null) {
            final AccountVO acct = _accountDao.findById(accountId);
            if (acct == null) {
                return false;  //account is deleted or does not exist
            }
            if ((isRootAdmin(accountId)) || (isDomainAdmin(accountId)) || (isResourceDomainAdmin(accountId))) {
                return true;
            } else if (acct.getType() == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Account finalizeOwner(final Account caller, final String accountName, final Long domainId, final Long projectId) {
        // don't default the owner to the system account
        if (caller.getId() == Account.ACCOUNT_ID_SYSTEM && ((accountName == null || domainId == null) && projectId == null)) {
            throw new InvalidParameterValueException("Account and domainId are needed for resource creation");
        }

        // projectId and account/domainId can't be specified together
        if ((accountName != null && domainId != null) && projectId != null) {
            throw new InvalidParameterValueException("ProjectId and account/domainId can't be specified together");
        }

        if (projectId != null) {
            final Project project = _projectMgr.getProject(projectId);
            if (project == null) {
                throw new InvalidParameterValueException("Unable to find project by id=" + projectId);
            }

            if (!_projectMgr.canAccessProjectAccount(caller, project.getProjectAccountId())) {
                throw new PermissionDeniedException("Account " + caller + " is unauthorised to use project id=" + projectId);
            }

            return getAccount(project.getProjectAccountId());
        }

        if (isAdmin(caller.getId()) && accountName != null && domainId != null) {
            final Domain domain = _domainMgr.getDomain(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Unable to find the domain by id=" + domainId);
            }

            final Account owner = _accountDao.findActiveAccount(accountName, domainId);
            if (owner == null) {
                throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
            }
            checkAccess(caller, domain);

            return owner;
        } else if (!isAdmin(caller.getId()) && accountName != null && domainId != null) {
            if (!accountName.equals(caller.getAccountName()) || domainId.longValue() != caller.getDomainId()) {
                throw new PermissionDeniedException("Can't create/list resources for account " + accountName + " in domain " + domainId + ", permission denied");
            } else {
                return caller;
            }
        } else {
            if ((accountName == null && domainId != null) || (accountName != null && domainId == null)) {
                throw new InvalidParameterValueException("AccountName and domainId must be specified together");
            }
            // regular user can't create/list resources for other people
            return caller;
        }
    }

    public boolean isResourceDomainAdmin(final Long accountId) {
        if (accountId != null) {
            final AccountVO acct = _accountDao.findById(accountId);
            if (acct == null) {
                return false;  //account is deleted or does not exist
            }
            for (final SecurityChecker checker : _securityCheckers) {
                try {
                    if (checker.checkAccess(acct, null, null, "DomainResourceCapability")) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("ResourceDomainAdmin Access granted to " + acct + " by " + checker.getName());
                        }
                        return true;
                    }
                } catch (final PermissionDeniedException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isInternalAccount(final long accountId) {
        final Account account = _accountDao.findById(accountId);
        if (account == null) {
            return false;  //account is deleted or does not exist
        }
        if (isRootAdmin(accountId) || (account.getType() == Account.ACCOUNT_ID_SYSTEM)) {
            return true;
        }
        return false;
    }

    @Override
    public Account getActiveAccountByName(final String accountName, final Long domainId) {
        if (accountName == null || domainId == null) {
            throw new InvalidParameterValueException("Both accountName and domainId are required for finding active account in the system");
        } else {
            return _accountDao.findActiveAccount(accountName, domainId);
        }
    }

    @Override
    public UserAccount getActiveUserAccount(final String username, final Long domainId) {
        return _userAccountDao.getUserAccount(username, domainId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_UPDATE, eventDescription = "updating User")
    public UserAccount updateUser(final Long userId, final String firstName, final String lastName, final String email, final String userName, final String password, final
    String apiKey, final String secretKey, final String timeZone) {
        // Input validation
        final UserVO user = _userDao.getUser(userId);

        if (user == null) {
            throw new InvalidParameterValueException("unable to find user by id");
        }

        if ((apiKey == null && secretKey != null) || (apiKey != null && secretKey == null)) {
            throw new InvalidParameterValueException("Please provide an userApiKey/userSecretKey pair");
        }

        // If the account is an admin type, return an error. We do not allow this
        final Account account = _accountDao.findById(user.getAccountId());
        if (account == null) {
            throw new InvalidParameterValueException("unable to find user account " + user.getAccountId());
        }

        // don't allow updating project account
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("unable to find user by id");
        }

        // don't allow updating system account
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("user id : " + userId + " is system account, update is not allowed");
        }

        checkAccess(CallContext.current().getCallingAccount(), AccessType.OperateEntry, true, account);

        if (firstName != null) {
            if (firstName.isEmpty()) {
                throw new InvalidParameterValueException("Firstname is empty");
            }

            user.setFirstname(firstName);
        }
        if (lastName != null) {
            if (lastName.isEmpty()) {
                throw new InvalidParameterValueException("Lastname is empty");
            }

            user.setLastname(lastName);
        }
        if (userName != null) {
            if (userName.isEmpty()) {
                throw new InvalidParameterValueException("Username is empty");
            }

            // don't allow to have same user names in the same domain
            final List<UserVO> duplicatedUsers = _userDao.findUsersByName(userName);
            for (final UserVO duplicatedUser : duplicatedUsers) {
                if (duplicatedUser.getId() != user.getId()) {
                    final Account duplicatedUserAccount = _accountDao.findById(duplicatedUser.getAccountId());
                    if (duplicatedUserAccount.getDomainId() == account.getDomainId()) {
                        throw new InvalidParameterValueException("User with name " + userName + " already exists in domain " + duplicatedUserAccount.getDomainId());
                    }
                }
            }

            user.setUsername(userName);
        }

        if (password != null) {
            if (password.isEmpty()) {
                throw new InvalidParameterValueException("Password cannot be empty");
            }
            String encodedPassword = null;
            for (final Iterator<UserAuthenticator> en = _userPasswordEncoders.iterator(); en.hasNext(); ) {
                final UserAuthenticator authenticator = en.next();
                encodedPassword = authenticator.encode(password);
                if (encodedPassword != null) {
                    break;
                }
            }
            if (encodedPassword == null) {
                throw new CloudRuntimeException("Failed to encode password");
            }
            user.setPassword(encodedPassword);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (timeZone != null) {
            user.setTimezone(timeZone);
        }
        if (apiKey != null) {
            user.setApiKey(apiKey);
        }
        if (secretKey != null) {
            user.setSecretKey(secretKey);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("updating user with id: " + userId);
        }
        // check if the apiKey and secretKey are globally unique
        if (apiKey != null && secretKey != null) {
            final Pair<User, Account> apiKeyOwner = _accountDao.findUserAccountByApiKey(apiKey);

            if (apiKeyOwner != null) {
                final User usr = apiKeyOwner.first();
                if (usr.getId() != userId) {
                    throw new InvalidParameterValueException("The api key:" + apiKey + " exists in the system for user id:" + userId + " ,please provide a unique key");
                }
            }
        }
        _userDao.update(userId, user);
        CallContext.current().putContextParameter(User.class, user.getUuid());

        return _userAccountDao.findById(userId);
    }

    @Override
    public Account getActiveAccountById(final long accountId) {
        return _accountDao.findById(accountId);
    }

    @Override
    public Account getAccount(final long accountId) {
        return _accountDao.findByIdIncludingRemoved(accountId);
    }

    @Override
    public Long checkAccessAndSpecifyAuthority(final Account caller, final Long zoneId) {
        // We just care for resource domain admin for now. He should be permitted to see only his zone.
        if (isResourceDomainAdmin(caller.getAccountId())) {
            if (zoneId == null) {
                return getZoneIdForAccount(caller);
            } else if (zoneId.compareTo(getZoneIdForAccount(caller)) != 0) {
                throw new PermissionDeniedException("Caller " + caller + "is not allowed to access the zone " + zoneId);
            } else {
                return zoneId;
            }
        } else {
            return zoneId;
        }
    }

    @Override
    public User getActiveUser(final long userId) {
        return _userDao.findById(userId);
    }

    @Override
    public User getUserIncludingRemoved(final long userId) {
        return _userDao.findByIdIncludingRemoved(userId);
    }

    private Long getZoneIdForAccount(final Account account) {

        // Currently just for resource domain admin
        final List<DataCenterVO> dcList = _dcDao.findZonesByDomainId(account.getDomainId());
        if (dcList != null && dcList.size() != 0) {
            return dcList.get(0).getId();
        } else {
            throw new CloudRuntimeException("Failed to find any private zone for Resource domain admin.");
        }
    }

    @Override
    public boolean isRootAdmin(final Long accountId) {
        if (accountId != null) {
            final AccountVO acct = _accountDao.findById(accountId);
            if (acct == null) {
                return false;  //account is deleted or does not exist
            }
            for (final SecurityChecker checker : _securityCheckers) {
                try {
                    if (checker.checkAccess(acct, null, null, "SystemCapability")) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Root Access granted to " + acct + " by " + checker.getName());
                        }
                        return true;
                    }
                } catch (final PermissionDeniedException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDomainAdmin(final Long accountId) {
        if (accountId != null) {
            final AccountVO acct = _accountDao.findById(accountId);
            if (acct == null) {
                return false;  //account is deleted or does not exist
            }
            for (final SecurityChecker checker : _securityCheckers) {
                try {
                    if (checker.checkAccess(acct, null, null, "DomainCapability")) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("DomainAdmin Access granted to " + acct + " by " + checker.getName());
                        }
                        return true;
                    }
                } catch (final PermissionDeniedException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    @DB
    public void updateLoginAttempts(final Long id, final int attempts, final boolean toDisable) {
        try {
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final UserAccountVO user;
                    user = _userAccountDao.lockRow(id, true);
                    user.setLoginAttempts(attempts);
                    if (toDisable) {
                        user.setState(State.disabled.toString());
                    }
                    _userAccountDao.update(id, user);
                }
            });
        } catch (final Exception e) {
            s_logger.error("Failed to update login attempts for user with id " + id);
        }
    }

    @Override
    public boolean isNormalUser(final long accountId) {
        final AccountVO acct = _accountDao.findById(accountId);
        if (acct != null && acct.getType() == Account.ACCOUNT_TYPE_NORMAL) {
            return true;
        }
        return false;
    }

    @Override
    public User getActiveUserByRegistrationToken(final String registrationToken) {
        return _userDao.findUserByRegistrationToken(registrationToken);
    }

    @Override
    public void markUserRegistered(final long userId) {
        final UserVO userForUpdate = _userDao.createForUpdate();
        userForUpdate.setRegistered(true);
        _userDao.update(Long.valueOf(userId), userForUpdate);
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_REGISTER_FOR_SECRET_API_KEY, eventDescription = "register for the developer API keys")
    public String[] createApiKeyAndSecretKey(final RegisterCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long userId = cmd.getId();

        final User user = getUserIncludingRemoved(userId);
        if (user == null) {
            throw new InvalidParameterValueException("unable to find user by id");
        }

        final Account account = _accountDao.findById(user.getAccountId());
        checkAccess(caller, null, true, account);

        // don't allow updating system user
        if (user.getId() == User.UID_SYSTEM) {
            throw new PermissionDeniedException("user id : " + user.getId() + " is system account, update is not allowed");
        }

        // generate both an api key and a secret key, update the user table with the keys, return the keys to the user
        final String[] keys = new String[2];
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                keys[0] = createUserApiKey(userId);
                keys[1] = createUserSecretKey(userId);
            }
        });

        return keys;
    }

    @Override
    public boolean enableAccount(final long accountId) {
        final boolean success;
        final AccountVO acctForUpdate = _accountDao.createForUpdate();
        acctForUpdate.setState(State.enabled);
        acctForUpdate.setNeedsCleanup(false);
        success = _accountDao.update(Long.valueOf(accountId), acctForUpdate);
        return success;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_REGISTER_FOR_SECRET_API_KEY, eventDescription = "register for the developer API keys")
    public String[] createApiKeyAndSecretKey(final long userId) {
        final User user = getUserIncludingRemoved(userId);
        if (user == null) {
            throw new InvalidParameterValueException("Unable to find user by id");
        }
        final String[] keys = new String[2];
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                keys[0] = AccountManagerImpl.this.createUserApiKey(userId);
                keys[1] = AccountManagerImpl.this.createUserSecretKey(userId);
            }
        });
        return keys;
    }

    @Override
    public UserAccount getUserByApiKey(final String apiKey) {
        return _userAccountDao.getUserByApiKey(apiKey);
    }

    @Override
    public RoleType getRoleType(final Account account) {
        RoleType roleType = RoleType.Unknown;
        if (account == null) {
            return roleType;
        }
        final short accountType = account.getType();

        // Account type to role type translation
        switch (accountType) {
            case Account.ACCOUNT_TYPE_ADMIN:
                roleType = RoleType.Admin;
                break;
            case Account.ACCOUNT_TYPE_DOMAIN_ADMIN:
                roleType = RoleType.DomainAdmin;
                break;
            case Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN:
                roleType = RoleType.ResourceAdmin;
                break;
            case Account.ACCOUNT_TYPE_NORMAL:
                roleType = RoleType.User;
                break;
        }
        return roleType;
    }

    @Override
    public boolean deleteAccount(final AccountVO account, final long callerUserId, final Account caller) {
        final long accountId = account.getId();

        // delete the account record
        if (!_accountDao.remove(accountId)) {
            s_logger.error("Unable to delete account " + accountId);
            return false;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Removed account " + accountId);
        }

        return cleanupAccount(account, callerUserId, caller);
    }

    @Override
    public void checkAccess(final Account caller, final Domain domain) throws PermissionDeniedException {
        for (final SecurityChecker checker : _securityCheckers) {
            if (checker.checkAccess(caller, domain)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Access granted to " + caller + " to " + domain + " by " + checker.getName());
                }
                return;
            }
        }

        assert false : "How can all of the security checkers pass on checking this caller?";
        throw new PermissionDeniedException("There's no way to confirm " + caller + " has access to " + domain);
    }

    @Override
    public void checkAccess(final Account caller, final AccessType accessType, final boolean sameOwner, final ControlledEntity... entities) {
        checkAccess(caller, accessType, sameOwner, null, entities);
    }

    protected boolean cleanupAccount(final AccountVO account, final long callerUserId, final Account caller) {
        final long accountId = account.getId();
        boolean accountCleanupNeeded = false;

        try {
            // cleanup the users from the account
            final List<UserVO> users = _userDao.listByAccount(accountId);
            for (final UserVO user : users) {
                if (!_userDao.remove(user.getId())) {
                    s_logger.error("Unable to delete user: " + user + " as a part of account " + account + " cleanup");
                    accountCleanupNeeded = true;
                }
            }

            // delete the account from project accounts
            _projectAccountDao.removeAccountFromProjects(accountId);

            if (account.getType() != Account.ACCOUNT_TYPE_PROJECT) {
                // delete the account from group
                _messageBus.publish(_name, MESSAGE_REMOVE_ACCOUNT_EVENT, PublishScope.LOCAL, accountId);
            }

            // delete all vm groups belonging to accont
            final List<InstanceGroupVO> groups = _vmGroupDao.listByAccountId(accountId);
            for (final InstanceGroupVO group : groups) {
                if (!_vmMgr.deleteVmGroup(group.getId())) {
                    s_logger.error("Unable to delete group: " + group.getId());
                    accountCleanupNeeded = true;
                }
            }

            // Delete the snapshots dir for the account. Have to do this before destroying the VMs.
            final boolean success = _snapMgr.deleteSnapshotDirsForAccount(accountId);
            if (success) {
                s_logger.debug("Successfully deleted snapshots directories for all volumes under account " + accountId + " across all zones");
            }

            // clean up templates
            final List<VMTemplateVO> userTemplates = _templateDao.listByAccountId(accountId);
            boolean allTemplatesDeleted = true;
            for (final VMTemplateVO template : userTemplates) {
                if (template.getRemoved() == null) {
                    try {
                        allTemplatesDeleted = _tmpltMgr.delete(callerUserId, template.getId(), null);
                    } catch (final Exception e) {
                        s_logger.warn("Failed to delete template while removing account: " + template.getName() + " due to: ", e);
                        allTemplatesDeleted = false;
                    }
                }
            }

            if (!allTemplatesDeleted) {
                s_logger.warn("Failed to delete templates while removing account id=" + accountId);
                accountCleanupNeeded = true;
            }

            // Destroy VM Snapshots
            final List<VMSnapshotVO> vmSnapshots = _vmSnapshotDao.listByAccountId(Long.valueOf(accountId));
            for (final VMSnapshot vmSnapshot : vmSnapshots) {
                try {
                    _vmSnapshotMgr.deleteVMSnapshot(vmSnapshot.getId());
                } catch (final Exception e) {
                    s_logger.debug("Failed to cleanup vm snapshot " + vmSnapshot.getId() + " due to " + e.toString());
                }
            }

            // Destroy the account's VMs
            final List<UserVmVO> vms = _userVmDao.listByAccountId(accountId);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Expunging # of vms (accountId=" + accountId + "): " + vms.size());
            }

            // no need to catch exception at this place as expunging vm should pass in order to perform further cleanup
            for (final UserVmVO vm : vms) {
                if (!_vmMgr.expunge(vm, callerUserId, caller)) {
                    s_logger.error("Unable to expunge vm: " + vm.getId());
                    accountCleanupNeeded = true;
                }
            }

            // Mark the account's volumes as destroyed
            final List<VolumeVO> volumes = _volumeDao.findDetachedByAccount(accountId);
            for (final VolumeVO volume : volumes) {
                if (!volume.getState().equals(Volume.State.Destroy)) {
                    try {
                        volumeService.deleteVolume(volume.getId(), caller);
                    } catch (final Exception ex) {
                        s_logger.warn("Failed to cleanup volumes as a part of account id=" + accountId + " cleanup due to Exception: ", ex);
                        accountCleanupNeeded = true;
                    }
                }
            }

            // delete remote access vpns and associated users
            final List<RemoteAccessVpnVO> remoteAccessVpns = _remoteAccessVpnDao.findByAccount(accountId);
            final List<VpnUserVO> vpnUsers = _vpnUser.listByAccount(accountId);

            for (final VpnUserVO vpnUser : vpnUsers) {
                _remoteAccessVpnMgr.removeVpnUser(accountId, vpnUser.getUsername(), caller);
            }

            try {
                for (final RemoteAccessVpnVO vpn : remoteAccessVpns) {
                    _remoteAccessVpnMgr.destroyRemoteAccessVpnForIp(vpn.getServerAddressId(), caller);
                }
            } catch (final ResourceUnavailableException ex) {
                s_logger.warn("Failed to cleanup remote access vpn resources as a part of account id=" + accountId + " cleanup due to Exception: ", ex);
                accountCleanupNeeded = true;
            }

            // Cleanup affinity groups
            final int numAGRemoved = _affinityGroupDao.removeByAccountId(accountId);
            s_logger.info("deleteAccount: Deleted " + numAGRemoved + " affinity groups for account " + accountId);

            // Delete all the networks
            boolean networksDeleted = true;
            s_logger.debug("Deleting networks for account " + account.getId());
            final List<NetworkVO> networks = _networkDao.listByOwner(accountId);
            if (networks != null) {
                for (final NetworkVO network : networks) {

                    final ReservationContext context = new ReservationContextImpl(null, null, getActiveUser(callerUserId), caller);

                    if (!_networkMgr.destroyNetwork(network.getId(), context, false)) {
                        s_logger.warn("Unable to destroy network " + network + " as a part of account id=" + accountId + " cleanup.");
                        accountCleanupNeeded = true;
                        networksDeleted = false;
                    } else {
                        s_logger.debug("Network " + network.getId() + " successfully deleted as a part of account id=" + accountId + " cleanup.");
                    }
                }
            }

            // Delete all VPCs
            boolean vpcsDeleted = true;
            s_logger.debug("Deleting vpcs for account " + account.getId());
            final List<? extends Vpc> vpcs = _vpcMgr.getVpcsForAccount(account.getId());
            for (final Vpc vpc : vpcs) {

                if (!_vpcMgr.destroyVpc(vpc, caller, callerUserId)) {
                    s_logger.warn("Unable to destroy VPC " + vpc + " as a part of account id=" + accountId + " cleanup.");
                    accountCleanupNeeded = true;
                    vpcsDeleted = false;
                } else {
                    s_logger.debug("VPC " + vpc.getId() + " successfully deleted as a part of account id=" + accountId + " cleanup.");
                }
            }

            if (networksDeleted && vpcsDeleted) {
                // release ip addresses belonging to the account
                final List<? extends IpAddress> ipsToRelease = _ipAddressDao.listByAccount(accountId);
                for (final IpAddress ip : ipsToRelease) {
                    s_logger.debug("Releasing ip " + ip + " as a part of account id=" + accountId + " cleanup");
                    if (!_ipAddrMgr.disassociatePublicIpAddress(ip.getId(), callerUserId, caller)) {
                        s_logger.warn("Failed to release ip address " + ip
                                + " as a part of account id=" + accountId
                                + " clenaup");
                        accountCleanupNeeded = true;
                    }
                }
            }

            // Delete Site 2 Site VPN customer gateway
            s_logger.debug("Deleting site-to-site VPN customer gateways for account " + accountId);
            if (!_vpnMgr.deleteCustomerGatewayByAccount(accountId)) {
                s_logger.warn("Fail to delete site-to-site VPN customer gateways for account " + accountId);
            }

            // release account specific Virtual vlans (belong to system Public Network) - only when networks are cleaned
            // up successfully
            if (networksDeleted) {
                if (!_configMgr.releaseAccountSpecificVirtualRanges(accountId)) {
                    accountCleanupNeeded = true;
                } else {
                    s_logger.debug("Account specific Virtual IP ranges " + " are successfully released as a part of account id=" + accountId + " cleanup.");
                }
            }

            // release account specific guest vlans
            final List<AccountGuestVlanMapVO> maps = _accountGuestVlanMapDao.listAccountGuestVlanMapsByAccount(accountId);
            for (final AccountGuestVlanMapVO map : maps) {
                _dataCenterVnetDao.releaseDedicatedGuestVlans(map.getId());
            }
            final int vlansReleased = _accountGuestVlanMapDao.removeByAccountId(accountId);
            s_logger.info("deleteAccount: Released " + vlansReleased + " dedicated guest vlan ranges from account " + accountId);

            // release dedication if any
            final List<DedicatedResourceVO> dedicatedResources = _dedicatedDao.listByAccountId(accountId);
            if (dedicatedResources != null && !dedicatedResources.isEmpty()) {
                s_logger.debug("Releasing dedicated resources for account " + accountId);
                for (final DedicatedResourceVO dr : dedicatedResources) {
                    if (!_dedicatedDao.remove(dr.getId())) {
                        s_logger.warn("Fail to release dedicated resources for account " + accountId);
                    }
                }
            }

            // Updating and deleting the resourceLimit and resourceCount should be the last step in cleanupAccount
            // process.
            // Update resource count for this account and for parent domains.
            final List<ResourceCountVO> resourceCounts = _resourceCountDao.listByOwnerId(accountId, ResourceOwnerType.Account);
            for (final ResourceCountVO resourceCount : resourceCounts) {
                _resourceLimitMgr.decrementResourceCount(accountId, resourceCount.getType(), resourceCount.getCount());
            }

            // Delete resource count and resource limits entries set for this account (if there are any).
            _resourceCountDao.removeEntriesByOwner(accountId, ResourceOwnerType.Account);
            _resourceLimitDao.removeEntriesByOwner(accountId, ResourceOwnerType.Account);
            return true;
        } catch (final Exception ex) {
            s_logger.warn("Failed to cleanup account " + account + " due to ", ex);
            accountCleanupNeeded = true;
            return true;
        } finally {
            s_logger.info("Cleanup for account " + account.getId() + (accountCleanupNeeded ? " is needed." : " is not needed."));
            if (accountCleanupNeeded) {
                _accountDao.markForCleanup(accountId);
            } else {
                account.setNeedsCleanup(false);
                _accountDao.update(accountId, account);
            }
        }
    }

    @Override
    public void checkAccess(final Account account, final ServiceOffering so)
            throws PermissionDeniedException {
        for (final SecurityChecker checker : _securityCheckers) {
            if (checker.checkAccess(account, so)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Access granted to " + account + " to " + so + " by " + checker.getName());
                }
                return;
            }
        }

        assert false : "How can all of the security checkers pass on checking this caller?";
        throw new PermissionDeniedException("There's no way to confirm " + account + " has access to " + so);
    }

    @Override
    public void checkAccess(final Account account, final DiskOffering dof)
            throws PermissionDeniedException {
        for (final SecurityChecker checker : _securityCheckers) {
            if (checker.checkAccess(account, dof)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Access granted to " + account + " to " + dof + " by " + checker.getName());
                }
                return;
            }
        }

        assert false : "How can all of the security checkers pass on checking this caller?";
        throw new PermissionDeniedException("There's no way to confirm " + account + " has access to " + dof);
    }

    @Override
    public boolean disableAccount(final long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        final boolean success;
        if (accountId <= 2) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("disableAccount -- invalid account id: " + accountId);
            }
            return false;
        }

        final AccountVO account = _accountDao.findById(accountId);
        if ((account == null) || (account.getState().equals(State.disabled) && !account.getNeedsCleanup())) {
            success = true;
        } else {
            AccountVO acctForUpdate = _accountDao.createForUpdate();
            acctForUpdate.setState(State.disabled);
            success = _accountDao.update(Long.valueOf(accountId), acctForUpdate);

            if (success) {
                boolean disableAccountResult = false;
                try {
                    disableAccountResult = doDisableAccount(accountId);
                } finally {
                    if (!disableAccountResult) {
                        s_logger.warn("Failed to disable account " + account + " resources as a part of disableAccount call, marking the account for cleanup");
                        _accountDao.markForCleanup(accountId);
                    } else {
                        acctForUpdate = _accountDao.createForUpdate();
                        account.setNeedsCleanup(false);
                        _accountDao.update(accountId, account);
                    }
                }
            }
        }
        return success;
    }

    @Override
    public void checkAccess(final Account caller, final AccessType accessType, final boolean sameOwner, final String apiName, final ControlledEntity... entities) {

        //check for the same owner
        Long ownerId = null;
        ControlledEntity prevEntity = null;
        if (sameOwner) {
            for (final ControlledEntity entity : entities) {
                if (sameOwner) {
                    if (ownerId == null) {
                        ownerId = entity.getAccountId();
                    } else if (ownerId.longValue() != entity.getAccountId()) {
                        throw new PermissionDeniedException("Entity " + entity + " and entity " + prevEntity + " belong to different accounts");
                    }
                    prevEntity = entity;
                }
            }
        }

        if (caller.getId() == Account.ACCOUNT_ID_SYSTEM || isRootAdmin(caller.getId())) {
            // no need to make permission checks if the system/root admin makes the call
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("No need to make permission check for System/RootAdmin account, returning true");
            }
            return;
        }

        final HashMap<Long, List<ControlledEntity>> domains = new HashMap<>();

        for (final ControlledEntity entity : entities) {
            long domainId = entity.getDomainId();
            if (entity.getAccountId() != -1 && domainId == -1) { // If account exists domainId should too so calculate
                // it. This condition might be hit for templates or entities which miss domainId in their tables
                final Account account = ApiDBUtils.findAccountById(entity.getAccountId());
                domainId = account != null ? account.getDomainId() : -1;
            }
            if (entity.getAccountId() != -1 && domainId != -1 && !(entity instanceof VirtualMachineTemplate) &&
                    !(entity instanceof Network && accessType != null && accessType == AccessType.UseEntry) && !(entity instanceof AffinityGroup)) {
                List<ControlledEntity> toBeChecked = domains.get(entity.getDomainId());
                // for templates, we don't have to do cross domains check
                if (toBeChecked == null) {
                    toBeChecked = new ArrayList<>();
                    domains.put(domainId, toBeChecked);
                }
                toBeChecked.add(entity);
            }
            boolean granted = false;
            for (final SecurityChecker checker : _securityCheckers) {
                if (checker.checkAccess(caller, entity, accessType, apiName)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Access to " + entity + " granted to " + caller + " by " + checker.getName());
                    }
                    granted = true;
                    break;
                }
            }

            if (!granted) {
                assert false : "How can all of the security checkers pass on checking this check: " + entity;
                throw new PermissionDeniedException("There's no way to confirm " + caller + " has access to " + entity);
            }
        }

        for (final Map.Entry<Long, List<ControlledEntity>> domain : domains.entrySet()) {
            for (final SecurityChecker checker : _securityCheckers) {
                final Domain d = _domainMgr.getDomain(domain.getKey());
                if (d == null || d.getRemoved() != null) {
                    throw new PermissionDeniedException("Domain is not found.", caller, domain.getValue());
                }
                try {
                    checker.checkAccess(caller, d);
                } catch (final PermissionDeniedException e) {
                    e.addDetails(caller, domain.getValue());
                    throw e;
                }
            }
        }

        // check that resources belong to the same account

    }

    @Override
    public Long finalyzeAccountId(final String accountName, final Long domainId, final Long projectId, final boolean enabledOnly) {
        if (accountName != null) {
            if (domainId == null) {
                throw new InvalidParameterValueException("Account must be specified with domainId parameter");
            }

            final Domain domain = _domainMgr.getDomain(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Unable to find domain by id");
            }

            final Account account = getActiveAccountByName(accountName, domainId);
            if (account != null && account.getType() != Account.ACCOUNT_TYPE_PROJECT) {
                if (!enabledOnly || account.getState() == Account.State.enabled) {
                    return account.getId();
                } else {
                    throw new PermissionDeniedException("Can't add resources to the account id=" + account.getId() + " in state=" + account.getState() +
                            " as it's no longer active");
                }
            } else {
                // idList is not used anywhere, so removed it now
                // List<IdentityProxy> idList = new ArrayList<IdentityProxy>();
                // idList.add(new IdentityProxy("domain", domainId, "domainId"));
                throw new InvalidParameterValueException("Unable to find account by name " + accountName + " in domain with specified id");
            }
        }

        if (projectId != null) {
            final Project project = _projectMgr.getProject(projectId);
            if (project != null) {
                if (!enabledOnly || project.getState() == Project.State.Active) {
                    return project.getProjectAccountId();
                } else {
                    final PermissionDeniedException ex =
                            new PermissionDeniedException("Can't add resources to the project with specified projectId in state=" + project.getState() +
                                    " as it's no longer active");
                    ex.addProxyObject(project.getUuid(), "projectId");
                    throw ex;
                }
            } else {
                throw new InvalidParameterValueException("Unable to find project by id");
            }
        }
        return null;
    }

    private boolean doDisableAccount(final long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        final List<VMInstanceVO> vms = _vmDao.listByAccountId(accountId);
        boolean success = true;
        for (final VMInstanceVO vm : vms) {
            try {
                try {
                    _itMgr.advanceStop(vm.getUuid(), false);
                } catch (final OperationTimedoutException ote) {
                    s_logger.warn(
                            "Operation for stopping vm timed out, unable to stop vm "
                                    + vm.getHostName(), ote);
                    success = false;
                }
            } catch (final AgentUnavailableException aue) {
                s_logger.warn("Agent running on host " + vm.getHostId() + " is unavailable, unable to stop vm " + vm.getHostName(), aue);
                success = false;
            }
        }

        return success;
    }

    @Override
    public UserAccount getUserAccountById(final Long userId) {
        return _userAccountDao.findById(userId);
    }

    // ///////////////////////////////////////////////////
    // ////////////// API commands /////////////////////
    // ///////////////////////////////////////////////////

    private String createUserApiKey(final long userId) {
        try {
            final UserVO updatedUser = _userDao.createForUpdate();

            String encodedKey;
            Pair<User, Account> userAcct;
            int retryLimit = 10;
            do {
                // FIXME: what algorithm should we use for API keys?
                final KeyGenerator generator = KeyGenerator.getInstance("HmacSHA1");
                final SecretKey key = generator.generateKey();
                encodedKey = Base64.encodeBase64URLSafeString(key.getEncoded());
                userAcct = _accountDao.findUserAccountByApiKey(encodedKey);
                retryLimit--;
            } while ((userAcct != null) && (retryLimit >= 0));

            if (userAcct != null) {
                return null;
            }
            updatedUser.setApiKey(encodedKey);
            _userDao.update(userId, updatedUser);
            return encodedKey;
        } catch (final NoSuchAlgorithmException ex) {
            s_logger.error("error generating secret key for user id=" + userId, ex);
        }
        return null;
    }

    private String createUserSecretKey(final long userId) {
        try {
            final UserVO updatedUser = _userDao.createForUpdate();
            String encodedKey;
            int retryLimit = 10;
            UserVO userBySecretKey;
            do {
                final KeyGenerator generator = KeyGenerator.getInstance("HmacSHA1");
                final SecretKey key = generator.generateKey();
                encodedKey = Base64.encodeBase64URLSafeString(key.getEncoded());
                userBySecretKey = _userDao.findUserBySecretKey(encodedKey);
                retryLimit--;
            } while ((userBySecretKey != null) && (retryLimit >= 0));

            if (userBySecretKey != null) {
                return null;
            }

            updatedUser.setSecretKey(encodedKey);
            _userDao.update(userId, updatedUser);
            return encodedKey;
        } catch (final NoSuchAlgorithmException ex) {
            s_logger.error("error generating secret key for user id=" + userId, ex);
        }
        return null;
    }

    private boolean doSetUserStatus(final long userId, final State state) {
        final UserVO userForUpdate = _userDao.createForUpdate();
        userForUpdate.setState(state);
        return _userDao.update(Long.valueOf(userId), userForUpdate);
    }

    protected boolean lockAccount(final long accountId) {
        boolean success = false;
        final Account account = _accountDao.findById(accountId);
        if (account != null) {
            if (account.getState().equals(State.locked)) {
                return true; // already locked, no-op
            } else if (account.getState().equals(State.enabled)) {
                final AccountVO acctForUpdate = _accountDao.createForUpdate();
                acctForUpdate.setState(State.locked);
                success = _accountDao.update(Long.valueOf(accountId), acctForUpdate);
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Attempting to lock a non-enabled account, current state is " + account.getState() + " (accountId: " + accountId + "), locking failed.");
                }
            }
        } else {
            s_logger.warn("Failed to lock account " + accountId + ", account not found.");
        }
        return success;
    }

    protected UserVO createUser(final long accountId, final String userName, final String password, final String firstName, final String lastName, final String email, final
    String timezone, String userUUID,
                                final User.Source source) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating user: " + userName + ", accountId: " + accountId + " timezone:" + timezone);
        }

        String encodedPassword = null;
        for (final UserAuthenticator authenticator : _userPasswordEncoders) {
            encodedPassword = authenticator.encode(password);
            if (encodedPassword != null) {
                break;
            }
        }
        if (encodedPassword == null) {
            throw new CloudRuntimeException("Failed to encode password");
        }

        if (userUUID == null) {
            userUUID = UUID.randomUUID().toString();
        }
        final UserVO user = _userDao.persist(new UserVO(accountId, userName, encodedPassword, firstName, lastName, email, timezone, userUUID, source));
        CallContext.current().putContextParameter(User.class, user.getUuid());
        return user;
    }

    protected class AccountCleanupTask extends ManagedContextRunnable {
        @Override
        protected void runInContext() {
            try {
                final GlobalLock lock = GlobalLock.getInternLock("AccountCleanup");
                if (lock == null) {
                    s_logger.debug("Couldn't get the global lock");
                    return;
                }

                if (!lock.lock(30)) {
                    s_logger.debug("Couldn't lock the db");
                    return;
                }

                try {
                    // Cleanup removed accounts
                    final List<AccountVO> removedAccounts = _accountDao.findCleanupsForRemovedAccounts(null);
                    s_logger.info("Found " + removedAccounts.size() + " removed accounts to cleanup");
                    for (final AccountVO account : removedAccounts) {
                        s_logger.debug("Cleaning up " + account.getId());
                        cleanupAccount(account, getSystemUser().getId(), getSystemAccount());
                    }

                    // cleanup disabled accounts
                    final List<AccountVO> disabledAccounts = _accountDao.findCleanupsForDisabledAccounts();
                    s_logger.info("Found " + disabledAccounts.size() + " disabled accounts to cleanup");
                    for (final AccountVO account : disabledAccounts) {
                        s_logger.debug("Disabling account " + account.getId());
                        try {
                            disableAccount(account.getId());
                        } catch (final Exception e) {
                            s_logger.error("Skipping due to error on account " + account.getId(), e);
                        }
                    }

                    // cleanup inactive domains
                    final List<? extends Domain> inactiveDomains = _domainMgr.findInactiveDomains();
                    s_logger.info("Found " + inactiveDomains.size() + " inactive domains to cleanup");
                    for (final Domain inactiveDomain : inactiveDomains) {
                        final long domainId = inactiveDomain.getId();
                        try {
                            final List<AccountVO> accountsForCleanupInDomain = _accountDao.findCleanupsForRemovedAccounts(domainId);
                            if (accountsForCleanupInDomain.isEmpty()) {
                                // release dedication if any, before deleting the domain
                                final List<DedicatedResourceVO> dedicatedResources = _dedicatedDao.listByDomainId(domainId);
                                if (dedicatedResources != null && !dedicatedResources.isEmpty()) {
                                    s_logger.debug("Releasing dedicated resources for domain" + domainId);
                                    for (final DedicatedResourceVO dr : dedicatedResources) {
                                        if (!_dedicatedDao.remove(dr.getId())) {
                                            s_logger.warn("Fail to release dedicated resources for domain " + domainId);
                                        }
                                    }
                                }
                                s_logger.debug("Removing inactive domain id=" + domainId);
                                _domainMgr.removeDomain(domainId);
                            } else {
                                s_logger.debug("Can't remove inactive domain id=" + domainId + " as it has accounts that need cleanup");
                            }
                        } catch (final Exception e) {
                            s_logger.error("Skipping due to error on domain " + domainId, e);
                        }
                    }

                    // cleanup inactive projects
                    final List<ProjectVO> inactiveProjects = _projectDao.listByState(Project.State.Disabled);
                    s_logger.info("Found " + inactiveProjects.size() + " disabled projects to cleanup");
                    for (final ProjectVO project : inactiveProjects) {
                        try {
                            final Account projectAccount = getAccount(project.getProjectAccountId());
                            if (projectAccount == null) {
                                s_logger.debug("Removing inactive project id=" + project.getId());
                                _projectMgr.deleteProject(CallContext.current().getCallingAccount(), CallContext.current().getCallingUserId(), project);
                            } else {
                                s_logger.debug("Can't remove disabled project " + project + " as it has non removed account id=" + project.getId());
                            }
                        } catch (final Exception e) {
                            s_logger.error("Skipping due to error on project " + project, e);
                        }
                    }
                } catch (final Exception e) {
                    s_logger.error("Exception ", e);
                } finally {
                    lock.unlock();
                }
            } catch (final Exception e) {
                s_logger.error("Exception ", e);
            }
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_UPDATE, eventDescription = "updating User")
    public UserAccount updateUser(final UpdateUserCmd cmd) {
        final Long id = cmd.getId();
        final String apiKey = cmd.getApiKey();
        final String firstName = cmd.getFirstname();
        final String email = cmd.getEmail();
        final String lastName = cmd.getLastname();
        final String password = cmd.getPassword();
        final String secretKey = cmd.getSecretKey();
        final String timeZone = cmd.getTimezone();
        final String userName = cmd.getUsername();

        return updateUser(id, firstName, lastName, email, userName, password, apiKey, secretKey, timeZone);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_DISABLE, eventDescription = "disabling User", async = true)
    public UserAccount disableUser(final long userId) {
        final Account caller = CallContext.current().getCallingAccount();

        // Check if user exists in the system
        final User user = _userDao.findById(userId);
        if (user == null || user.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        final Account account = _accountDao.findById(user.getAccountId());
        if (account == null) {
            throw new InvalidParameterValueException("unable to find user account " + user.getAccountId());
        }

        // don't allow disabling user belonging to project's account
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        // If the user is a System user, return an error
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("User id : " + userId + " is a system user, disabling is not allowed");
        }

        checkAccess(caller, AccessType.OperateEntry, true, account);

        final boolean success = doSetUserStatus(userId, State.disabled);
        if (success) {

            CallContext.current().putContextParameter(User.class, user.getUuid());

            // user successfully disabled
            return _userAccountDao.findById(userId);
        } else {
            throw new CloudRuntimeException("Unable to disable user " + userId);
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_USER_ENABLE, eventDescription = "enabling User")
    public UserAccount enableUser(final long userId) {

        final Account caller = CallContext.current().getCallingAccount();

        // Check if user exists in the system
        final User user = _userDao.findById(userId);
        if (user == null || user.getRemoved() != null) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        final Account account = _accountDao.findById(user.getAccountId());
        if (account == null) {
            throw new InvalidParameterValueException("unable to find user account " + user.getAccountId());
        }

        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find active user by id " + userId);
        }

        // If the user is a System user, return an error
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("User id : " + userId + " is a system user, enabling is not allowed");
        }

        checkAccess(caller, AccessType.OperateEntry, true, account);

        final boolean success = Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                boolean success = doSetUserStatus(userId, State.enabled);

                // make sure the account is enabled too
                success = success && enableAccount(user.getAccountId());

                return success;
            }
        });

        if (success) {
            // whenever the user is successfully enabled, reset the login attempts to zero
            updateLoginAttempts(userId, 0, false);

            CallContext.current().putContextParameter(User.class, user.getUuid());

            return _userAccountDao.findById(userId);
        } else {
            throw new CloudRuntimeException("Unable to enable user " + userId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_DELETE, eventDescription = "deleting account", async = true)
    // This method deletes the account
    public boolean deleteUserAccount(final long accountId) {

        final CallContext ctx = CallContext.current();
        final long callerUserId = ctx.getCallingUserId();
        final Account caller = ctx.getCallingAccount();

        // If the user is a System user, return an error. We do not allow this
        final AccountVO account = _accountDao.findById(accountId);

        if (account == null || account.getRemoved() != null) {
            if (account != null) {
                s_logger.info("The account:" + account.getAccountName() + " is already removed");
            }
            return true;
        }

        // don't allow removing Project account
        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("The specified account does not exist in the system");
        }

        checkAccess(caller, null, true, account);

        // don't allow to delete default account (system and admin)
        if (account.isDefault()) {
            throw new InvalidParameterValueException("The account is default and can't be removed");
        }

        // Account that manages project(s) can't be removed
        final List<Long> managedProjectIds = _projectAccountDao.listAdministratedProjectIds(accountId);
        if (!managedProjectIds.isEmpty()) {
            final StringBuilder projectIds = new StringBuilder();
            for (final Long projectId : managedProjectIds) {
                projectIds.append(projectId + ", ");
            }

            throw new InvalidParameterValueException("The account id=" + accountId + " manages project(s) with ids " + projectIds + "and can't be removed");
        }

        CallContext.current().putContextParameter(Account.class, account.getUuid());

        return deleteAccount(account, callerUserId, caller);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_ENABLE, eventDescription = "enabling account", async = true)
    public AccountVO enableAccount(final String accountName, final Long domainId, final Long accountId) {

        // Check if account exists
        final Account account;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findActiveAccount(accountName, domainId);
        }

        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("Account id : " + accountId + " is a system account, enable is not allowed");
        }

        // Check if user performing the action is allowed to modify this account
        final Account caller = CallContext.current().getCallingAccount();
        checkAccess(caller, AccessType.OperateEntry, true, account);

        final boolean success = enableAccount(account.getId());
        if (success) {

            CallContext.current().putContextParameter(Account.class, account.getUuid());

            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to enable account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_DISABLE, eventDescription = "locking account", async = true)
    public AccountVO lockAccount(final String accountName, final Long domainId, final Long accountId) {
        final Account caller = CallContext.current().getCallingAccount();

        final Account account;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findActiveAccount(accountName, domainId);
        }

        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find active account by accountId: " + accountId + " OR by name: " + accountName + " in domain " +
                    domainId);
        }

        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("Account id : " + accountId + " is a system account, lock is not allowed");
        }

        checkAccess(caller, AccessType.OperateEntry, true, account);

        if (lockAccount(account.getId())) {
            CallContext.current().putContextParameter(Account.class, account.getUuid());
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to lock account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_DISABLE, eventDescription = "disabling account", async = true)
    public AccountVO disableAccount(final String accountName, final Long domainId, final Long accountId) throws ConcurrentOperationException, ResourceUnavailableException {
        final Account caller = CallContext.current().getCallingAccount();

        final Account account;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findActiveAccount(accountName, domainId);
        }

        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new PermissionDeniedException("Account id : " + accountId + " is a system account, disable is not allowed");
        }

        checkAccess(caller, AccessType.OperateEntry, true, account);

        if (disableAccount(account.getId())) {
            CallContext.current().putContextParameter(Account.class, account.getUuid());
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to update account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_ACCOUNT_UPDATE, eventDescription = "updating account", async = true)
    public AccountVO updateAccount(final UpdateAccountCmd cmd) {
        final Long accountId = cmd.getId();
        final Long domainId = cmd.getDomainId();
        final String accountName = cmd.getAccountName();
        final String newAccountName = cmd.getNewName();
        final String networkDomain = cmd.getNetworkDomain();
        final Map<String, String> details = cmd.getDetails();

        final boolean success;
        final Account account;
        if (accountId != null) {
            account = _accountDao.findById(accountId);
        } else {
            account = _accountDao.findEnabledAccount(accountName, domainId);
        }

        // Check if account exists
        if (account == null || account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            s_logger.error("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
            throw new InvalidParameterValueException("Unable to find account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }

        // Don't allow to modify system account
        if (account.getId() == Account.ACCOUNT_ID_SYSTEM) {
            throw new InvalidParameterValueException("Can not modify system account");
        }

        // Check if user performing the action is allowed to modify this account
        checkAccess(CallContext.current().getCallingAccount(), _domainMgr.getDomain(account.getDomainId()));

        // check if the given account name is unique in this domain for updating
        final Account duplicateAcccount = _accountDao.findActiveAccount(newAccountName, domainId);
        if (duplicateAcccount != null && duplicateAcccount.getId() != account.getId()) {// allow
            // same
            // account
            // to
            // update
            // itself
            throw new InvalidParameterValueException("There already exists an account with the name:" + newAccountName + " in the domain:" + domainId +
                    " with existing account id:" + duplicateAcccount.getId());
        }

        if (networkDomain != null && !networkDomain.isEmpty()) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters " +
                                "'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        final AccountVO acctForUpdate = _accountDao.findById(account.getId());
        acctForUpdate.setAccountName(newAccountName);

        if (networkDomain != null) {
            if (networkDomain.isEmpty()) {
                acctForUpdate.setNetworkDomain(null);
            } else {
                acctForUpdate.setNetworkDomain(networkDomain);
            }
        }

        final Account accountFinal = account;
        success = Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                final boolean success = _accountDao.update(accountFinal.getId(), acctForUpdate);

                if (details != null && success) {
                    _accountDetailsDao.update(accountFinal.getId(), details);
                }

                return success;
            }
        });

        if (success) {
            CallContext.current().putContextParameter(Account.class, account.getUuid());
            return _accountDao.findById(account.getId());
        } else {
            throw new CloudRuntimeException("Unable to update account by accountId: " + accountId + " OR by name: " + accountName + " in domain " + domainId);
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_USER_DELETE, eventDescription = "deleting User")
    public boolean deleteUser(final DeleteUserCmd deleteUserCmd) {
        final long id = deleteUserCmd.getId();

        final UserVO user = _userDao.findById(id);

        if (user == null) {
            throw new InvalidParameterValueException("The specified user doesn't exist in the system");
        }

        final Account account = _accountDao.findById(user.getAccountId());

        // don't allow to delete the user from the account of type Project
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("The specified user doesn't exist in the system");
        }

        // don't allow to delete default user (system and admin users)
        if (user.isDefault()) {
            throw new InvalidParameterValueException("The user is default and can't be removed");
        }

        checkAccess(CallContext.current().getCallingAccount(), AccessType.OperateEntry, true, account);
        CallContext.current().putContextParameter(User.class, user.getUuid());
        return _userDao.remove(id);
    }

    @Override
    @DB
    public AccountVO createAccount(final String accountName, final short accountType, final Long domainId, final String networkDomain, final Map<String, String> details,
                                   final String uuid) {
        // Validate domain
        final Domain domain = _domainMgr.getDomain(domainId);
        if (domain == null) {
            throw new InvalidParameterValueException("The domain " + domainId + " does not exist; unable to create account");
        }

        if (domain.getState().equals(Domain.State.Inactive)) {
            throw new CloudRuntimeException("The account cannot be created as domain " + domain.getName() + " is being deleted");
        }

        if ((domainId != Domain.ROOT_DOMAIN) && (accountType == Account.ACCOUNT_TYPE_ADMIN)) {
            throw new InvalidParameterValueException("Invalid account type " + accountType + " given for an account in domain " + domainId + "; unable to create user.");
        }

        // Validate account/user/domain settings
        if (_accountDao.findActiveAccount(accountName, domainId) != null) {
            throw new InvalidParameterValueException("The specified account: " + accountName + " already exists");
        }

        if (networkDomain != null) {
            if (!NetUtils.verifyDomainName(networkDomain)) {
                throw new InvalidParameterValueException(
                        "Invalid network domain. Total length shouldn't exceed 190 chars. Each domain label must be between 1 and 63 characters long, can contain ASCII letters " +
                                "'a' through 'z', the digits '0' through '9', "
                                + "and the hyphen ('-'); can't start or end with \"-\"");
            }
        }

        // Verify account type
        if ((accountType < Account.ACCOUNT_TYPE_NORMAL) || (accountType > Account.ACCOUNT_TYPE_PROJECT)) {
            throw new InvalidParameterValueException("Invalid account type " + accountType + " given; unable to create user");
        }

        if (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
            final List<DataCenterVO> dc = _dcDao.findZonesByDomainId(domainId);
            if (dc.isEmpty()) {
                throw new InvalidParameterValueException("The account cannot be created as domain " + domain.getName() + " is not associated with any private Zone");
            }
        }

        // Create the account
        return Transaction.execute(new TransactionCallback<AccountVO>() {
            @Override
            public AccountVO doInTransaction(final TransactionStatus status) {
                final AccountVO account = _accountDao.persist(new AccountVO(accountName, domainId, networkDomain, accountType, uuid));

                if (account == null) {
                    throw new CloudRuntimeException("Failed to create account name " + accountName + " in domain id=" + domainId);
                }

                final Long accountId = account.getId();

                if (details != null) {
                    _accountDetailsDao.persist(accountId, details);
                }

                // Create resource count records for the account
                _resourceCountDao.createResourceCounts(accountId, ResourceLimit.ResourceOwnerType.Account);

                return account;
            }
        });
    }

    @Override
    public void logoutUser(final long userId) {
        final UserAccount userAcct = _userAccountDao.findById(userId);
        if (userAcct != null) {
            ActionEventUtils.onActionEvent(userId, userAcct.getAccountId(), userAcct.getDomainId(), EventTypes.EVENT_USER_LOGOUT, "user has logged out");
        } // else log some kind of error event? This likely means the user doesn't exist, or has been deleted...
    }

    @Override
    public UserAccount authenticateUser(final String username, final String password, final Long domainId, final InetAddress loginIpAddress, final Map<String, Object[]>
            requestParameters) {
        UserAccount user = null;
        if (password != null && !password.isEmpty()) {
            user = getUserAccount(username, password, domainId, requestParameters);
        }

        if (user != null) {
            // don't allow to authenticate system user
            if (user.getId() == User.UID_SYSTEM) {
                s_logger.error("Failed to authenticate user: " + username + " in domain " + domainId);
                return null;
            }

            // We authenticated successfully by now, let's check if we are allowed to login from the ip address the reqest comes from
            final Account account = _accountMgr.getAccount(user.getAccountId());
            final DomainVO domain = (DomainVO) _domainMgr.getDomain(account.getDomainId());

            // Get the CIDRs from where this account is allowed to make calls
            final String accessAllowedCidrs = ApiServiceConfiguration.ApiAllowedSourceCidrList.valueIn(account.getId()).replaceAll("\\s", "");
            final Boolean ApiSourceCidrChecksEnabled = ApiServiceConfiguration.ApiSourceCidrChecksEnabled.value();

            if (ApiSourceCidrChecksEnabled) {
                s_logger.debug("CIDRs from which account '" + account.toString() + "' is allowed to perform API calls: " + accessAllowedCidrs);

                // Block when is not in the list of allowed IPs
                if (!NetUtils.isIpInCidrList(loginIpAddress, accessAllowedCidrs.split(","))) {
                    s_logger.warn("Request by account '" + account.toString() + "' was denied since " + loginIpAddress.toString().replaceAll("/", "")
                            + " does not match " + accessAllowedCidrs);
                    throw new CloudAuthenticationException("Failed to authenticate user '" + username + "' in domain '" + domain.getPath() + "' from ip "
                            + loginIpAddress.toString().replaceAll("/", "") + "; please provide valid credentials");
                }
            }

            // Here all is fine!
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("User: " + username + " in domain " + domainId + " has successfully logged in");
            }

            ActionEventUtils.onActionEvent(user.getId(), user.getAccountId(), user.getDomainId(), EventTypes.EVENT_USER_LOGIN, "user has logged in from IP Address " +
                    loginIpAddress);

            return user;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("User: " + username + " in domain " + domainId + " has failed to log in");
            }
            return null;
        }
    }

    private UserAccount getUserAccount(final String username, final String password, final Long domainId, final Map<String, Object[]> requestParameters) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Attempting to log in user: " + username + " in domain " + domainId);
        }
        UserAccount userAccount = _userAccountDao.getUserAccount(username, domainId);

        boolean authenticated = false;
        final HashSet<ActionOnFailedAuthentication> actionsOnFailedAuthenticaion = new HashSet<>();
        final User.Source userSource = userAccount != null ? userAccount.getSource() : User.Source.UNKNOWN;
        for (final UserAuthenticator authenticator : _userAuthenticators) {
            if (userSource != User.Source.UNKNOWN) {
                if (!authenticator.getName().equalsIgnoreCase(userSource.name())) {
                    continue;
                }
            }
            final Pair<Boolean, ActionOnFailedAuthentication> result = authenticator.authenticate(username, password, domainId, requestParameters);
            if (result.first()) {
                authenticated = true;
                break;
            } else if (result.second() != null) {
                actionsOnFailedAuthenticaion.add(result.second());
            }
        }

        final boolean updateIncorrectLoginCount = actionsOnFailedAuthenticaion.contains(ActionOnFailedAuthentication.INCREMENT_INCORRECT_LOGIN_ATTEMPT_COUNT);

        if (authenticated) {

            final Domain domain = _domainMgr.getDomain(domainId);
            String domainName = null;
            if (domain != null) {
                domainName = domain.getName();
            }
            userAccount = _userAccountDao.getUserAccount(username, domainId);

            if (!userAccount.getState().equalsIgnoreCase(Account.State.enabled.toString()) ||
                    !userAccount.getAccountState().equalsIgnoreCase(Account.State.enabled.toString())) {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("User " + username + " in domain " + domainName + " is disabled/locked (or account is disabled/locked)");
                }
                throw new CloudAuthenticationException("User " + username + " (or their account) in domain " + domainName + " is disabled/locked. Please contact the " +
                        "administrator.");
            }
            // Whenever the user is able to log in successfully, reset the login attempts to zero
            if (!isInternalAccount(userAccount.getId())) {
                updateLoginAttempts(userAccount.getId(), 0, false);
            }

            return userAccount;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to authenticate user with username " + username + " in domain " + domainId);
            }

            if (userAccount == null) {
                s_logger.warn("Unable to find an user with username " + username + " in domain " + domainId);
                return null;
            }

            if (userAccount.getState().equalsIgnoreCase(Account.State.enabled.toString())) {
                if (!isInternalAccount(userAccount.getId())) {
                    // Internal accounts are not disabled
                    final int attemptsMade = userAccount.getLoginAttempts() + 1;
                    if (updateIncorrectLoginCount) {
                        if (attemptsMade < _allowedLoginAttempts) {
                            updateLoginAttempts(userAccount.getId(), attemptsMade, false);
                            s_logger.warn("Login attempt failed. You have " + (_allowedLoginAttempts - attemptsMade) + " attempt(s) remaining");
                        } else {
                            updateLoginAttempts(userAccount.getId(), _allowedLoginAttempts, true);
                            s_logger.warn("User " + userAccount.getUsername() + " has been disabled due to multiple failed login attempts." + " Please contact admin.");
                        }
                    }
                }
            } else {
                s_logger.info("User " + userAccount.getUsername() + " is disabled/locked");
            }
            return null;
        }
    }

    @Override
    public Pair<User, Account> findUserByApiKey(final String apiKey) {
        return _accountDao.findUserAccountByApiKey(apiKey);
    }

    @Override
    public void buildACLSearchBuilder(final SearchBuilder<? extends ControlledEntity> sb,
                                      final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria
                                              listProjectResourcesCriteria) {

        if (sb.entity() instanceof IPAddressVO) {
            sb.and("accountIdIN", ((IPAddressVO) sb.entity()).getAllocatedToAccountId(), SearchCriteria.Op.IN);
            sb.and("domainId", ((IPAddressVO) sb.entity()).getAllocatedInDomainId(), SearchCriteria.Op.EQ);
        } else if (sb.entity() instanceof ProjectInvitationVO) {
            sb.and("accountIdIN", ((ProjectInvitationVO) sb.entity()).getForAccountId(), SearchCriteria.Op.IN);
            sb.and("domainId", ((ProjectInvitationVO) sb.entity()).getInDomainId(), SearchCriteria.Op.EQ);
        } else {
            sb.and("accountIdIN", sb.entity().getAccountId(), SearchCriteria.Op.IN);
            sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        }

        if (((permittedAccounts.isEmpty()) && (domainId != null) && isRecursive)) {
            // if accountId isn't specified, we can do a domain match for the admin case if isRecursive is true
            final SearchBuilder<DomainVO> domainSearch = _domainDao.createSearchBuilder();
            domainSearch.and("path", domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);

            if (sb.entity() instanceof IPAddressVO) {
                sb.join("domainSearch", domainSearch, ((IPAddressVO) sb.entity()).getAllocatedInDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else if (sb.entity() instanceof ProjectInvitationVO) {
                sb.join("domainSearch", domainSearch, ((ProjectInvitationVO) sb.entity()).getInDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else {
                sb.join("domainSearch", domainSearch, sb.entity().getDomainId(), domainSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            }
        }
        if (listProjectResourcesCriteria != null) {
            final SearchBuilder<AccountVO> accountSearch = _accountDao.createSearchBuilder();
            if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.ListProjectResourcesOnly) {
                accountSearch.and("type", accountSearch.entity().getType(), SearchCriteria.Op.EQ);
            } else if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.SkipProjectResources) {
                accountSearch.and("type", accountSearch.entity().getType(), SearchCriteria.Op.NEQ);
            }

            if (sb.entity() instanceof IPAddressVO) {
                sb.join("accountSearch", accountSearch, ((IPAddressVO) sb.entity()).getAllocatedToAccountId(), accountSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else if (sb.entity() instanceof ProjectInvitationVO) {
                sb.join("accountSearch", accountSearch, ((ProjectInvitationVO) sb.entity()).getForAccountId(), accountSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            } else {
                sb.join("accountSearch", accountSearch, sb.entity().getAccountId(), accountSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            }
        }
    }

    @Override
    public void buildACLSearchCriteria(final SearchCriteria<? extends ControlledEntity> sc,
                                       final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria
                                               listProjectResourcesCriteria) {

        if (listProjectResourcesCriteria != null) {
            sc.setJoinParameters("accountSearch", "type", Account.ACCOUNT_TYPE_PROJECT);
        }

        if (!permittedAccounts.isEmpty()) {
            sc.setParameters("accountIdIN", permittedAccounts.toArray());
        } else if (domainId != null) {
            final DomainVO domain = _domainDao.findById(domainId);
            if (isRecursive) {
                sc.setJoinParameters("domainSearch", "path", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }
    }

    //TODO: deprecate this to use the new buildACLSearchParameters with permittedDomains, permittedAccounts, and permittedResources as return
    @Override
    public void buildACLSearchParameters(final Account caller, final Long id, final String accountName, final Long projectId, final List<Long> permittedAccounts,
                                         final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject, final boolean listAll, final boolean
                                                 forProjectInvitation) {
        final Long domainId = domainIdRecursiveListProject.first();
        if (domainId != null) {
            final Domain domain = _domainDao.findById(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Unable to find domain by id " + domainId);
            }
            // check permissions
            checkAccess(caller, domain);
        }

        if (accountName != null) {
            if (projectId != null) {
                throw new InvalidParameterValueException("Account and projectId can't be specified together");
            }

            final Account userAccount;
            final Domain domain;
            if (domainId != null) {
                userAccount = _accountDao.findActiveAccount(accountName, domainId);
                domain = _domainDao.findById(domainId);
            } else {
                userAccount = _accountDao.findActiveAccount(accountName, caller.getDomainId());
                domain = _domainDao.findById(caller.getDomainId());
            }

            if (userAccount != null) {
                checkAccess(caller, null, false, userAccount);
                // check permissions
                permittedAccounts.add(userAccount.getId());
            } else {
                throw new InvalidParameterValueException("could not find account " + accountName + " in domain " + domain.getUuid());
            }
        }

        // set project information
        if (projectId != null) {
            if (!forProjectInvitation) {
                if (projectId.longValue() == -1) {
                    if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                        permittedAccounts.addAll(_projectMgr.listPermittedProjectAccounts(caller.getId()));
                    } else {
                        domainIdRecursiveListProject.third(Project.ListProjectResourcesCriteria.ListProjectResourcesOnly);
                    }
                } else {
                    final Project project = _projectMgr.getProject(projectId);
                    if (project == null) {
                        throw new InvalidParameterValueException("Unable to find project by id " + projectId);
                    }
                    if (!_projectMgr.canAccessProjectAccount(caller, project.getProjectAccountId())) {
                        throw new PermissionDeniedException("Account " + caller + " can't access project id=" + projectId);
                    }
                    permittedAccounts.add(project.getProjectAccountId());
                }
            }
        } else {
            if (id == null) {
                domainIdRecursiveListProject.third(Project.ListProjectResourcesCriteria.SkipProjectResources);
            }
            if (permittedAccounts.isEmpty() && domainId == null) {
                if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                    permittedAccounts.add(caller.getId());
                } else if (!listAll) {
                    if (id == null) {
                        permittedAccounts.add(caller.getId());
                    } else if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                        domainIdRecursiveListProject.first(caller.getDomainId());
                        domainIdRecursiveListProject.second(true);
                    }
                } else if (domainId == null) {
                    if (caller.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) {
                        domainIdRecursiveListProject.first(caller.getDomainId());
                        domainIdRecursiveListProject.second(true);
                    }
                }
            } else if (domainId != null) {
                if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                    permittedAccounts.add(caller.getId());
                }
            }
        }
    }

    @Override
    public void buildACLViewSearchBuilder(final SearchBuilder<? extends ControlledViewEntity> sb, final Long domainId,
                                          final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria listProjectResourcesCriteria) {

        sb.and("accountIdIN", sb.entity().getAccountId(), SearchCriteria.Op.IN);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);

        if (((permittedAccounts.isEmpty()) && (domainId != null) && isRecursive)) {
            // if accountId isn't specified, we can do a domain match for the
            // admin case if isRecursive is true
            sb.and("domainPath", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
        }

        if (listProjectResourcesCriteria != null) {
            if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.ListProjectResourcesOnly) {
                sb.and("accountType", sb.entity().getAccountType(), SearchCriteria.Op.EQ);
            } else if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.SkipProjectResources) {
                sb.and("accountType", sb.entity().getAccountType(), SearchCriteria.Op.NEQ);
            }
        }
    }

    @Override
    public void buildACLViewSearchCriteria(final SearchCriteria<? extends ControlledViewEntity> sc,
                                           final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria
                                                   listProjectResourcesCriteria) {
        if (listProjectResourcesCriteria != null) {
            sc.setParameters("accountType", Account.ACCOUNT_TYPE_PROJECT);
        }

        if (!permittedAccounts.isEmpty()) {
            sc.setParameters("accountIdIN", permittedAccounts.toArray());
        } else if (domainId != null) {
            final DomainVO domain = _domainDao.findById(domainId);
            if (isRecursive) {
                sc.setParameters("domainPath", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }
    }

    @Override
    public List<String> listAclGroupsByAccount(final Long accountId) {
        if (_querySelectors == null || _querySelectors.size() == 0) {
            return new ArrayList<>();
        }

        final QuerySelector qs = _querySelectors.get(0);
        return qs.listAclGroupsByAccount(accountId);
    }
}
