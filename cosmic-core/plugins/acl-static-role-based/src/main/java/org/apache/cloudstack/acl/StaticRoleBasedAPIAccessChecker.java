package org.apache.cloudstack.acl;

import com.cloud.exception.PermissionDeniedException;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.PluggableService;
import org.apache.cloudstack.api.APICommand;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This is the default API access checker that grab's the user's account
// based on the account type, access is granted
public class StaticRoleBasedAPIAccessChecker extends AdapterBase implements APIChecker {

    protected static final Logger s_logger = LoggerFactory.getLogger(StaticRoleBasedAPIAccessChecker.class);

    Set<String> commandPropertyFiles = new HashSet<>();
    Set<String> commandsPropertiesOverrides = new HashSet<>();
    Map<RoleType, Set<String>> commandsPropertiesRoleBasedApisMap = new HashMap<>();
    Map<RoleType, Set<String>> annotationRoleBasedApisMap = new HashMap<>();

    List<PluggableService> _services;
    @Inject
    AccountService _accountService;

    protected StaticRoleBasedAPIAccessChecker() {
        super();
        for (final RoleType roleType : RoleType.values()) {
            commandsPropertiesRoleBasedApisMap.put(roleType, new HashSet<>());
            annotationRoleBasedApisMap.put(roleType, new HashSet<>());
        }
    }

    @Override
    public boolean checkAccess(final User user, final String commandName) throws PermissionDeniedException {
        final Account account = _accountService.getAccount(user.getAccountId());
        if (account == null) {
            throw new PermissionDeniedException("The account id=" + user.getAccountId() + "for user id=" + user.getId() + "is null");
        }

        final RoleType roleType = _accountService.getRoleType(account);
        final boolean isAllowed =
                commandsPropertiesOverrides.contains(commandName) ? commandsPropertiesRoleBasedApisMap.get(roleType).contains(commandName) : annotationRoleBasedApisMap.get(
                        roleType).contains(commandName);

        if (!isAllowed) {
            throw new PermissionDeniedException("The API does not exist or is blacklisted. Role type=" + roleType.toString() + " is not allowed to request the api: " +
                    commandName);
        }
        return isAllowed;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        for (final String commandPropertyFile : commandPropertyFiles) {
            processMapping(PropertiesUtil.processConfigFile(new String[]{commandPropertyFile}));
        }
        return true;
    }

    @Override
    public boolean start() {
        for (final PluggableService service : _services) {
            for (final Class<?> clz : service.getCommands()) {
                final APICommand command = clz.getAnnotation(APICommand.class);
                for (final RoleType role : command.authorized()) {
                    final Set<String> commands = annotationRoleBasedApisMap.get(role);
                    if (!commands.contains(command.name())) {
                        commands.add(command.name());
                    }
                }
            }
        }
        return super.start();
    }

    private void processMapping(final Map<String, String> configMap) {
        for (final Map.Entry<String, String> entry : configMap.entrySet()) {
            final String apiName = entry.getKey();
            final String roleMask = entry.getValue();
            commandsPropertiesOverrides.add(apiName);
            try {
                final short cmdPermissions = Short.parseShort(roleMask);
                for (final RoleType roleType : RoleType.values()) {
                    if ((cmdPermissions & roleType.getValue()) != 0) {
                        commandsPropertiesRoleBasedApisMap.get(roleType).add(apiName);
                    }
                }
            } catch (final NumberFormatException nfe) {
                s_logger.info("Malformed key=value pair for entry: " + entry.toString());
            }
        }
    }

    public List<PluggableService> getServices() {
        return _services;
    }

    @Inject
    public void setServices(final List<PluggableService> services) {
        this._services = services;
    }

    public Set<String> getCommandPropertyFiles() {
        return commandPropertyFiles;
    }

    public void setCommandPropertyFiles(final Set<String> commandPropertyFiles) {
        this.commandPropertyFiles = commandPropertyFiles;
    }
}
