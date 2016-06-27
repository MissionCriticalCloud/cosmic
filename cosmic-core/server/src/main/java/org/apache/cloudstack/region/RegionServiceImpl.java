package org.apache.cloudstack.region;

import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.UserAccount;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.api.command.admin.account.DeleteAccountCmd;
import org.apache.cloudstack.api.command.admin.account.DisableAccountCmd;
import org.apache.cloudstack.api.command.admin.account.EnableAccountCmd;
import org.apache.cloudstack.api.command.admin.account.UpdateAccountCmd;
import org.apache.cloudstack.api.command.admin.domain.DeleteDomainCmd;
import org.apache.cloudstack.api.command.admin.domain.UpdateDomainCmd;
import org.apache.cloudstack.api.command.admin.user.DeleteUserCmd;
import org.apache.cloudstack.api.command.admin.user.DisableUserCmd;
import org.apache.cloudstack.api.command.admin.user.EnableUserCmd;
import org.apache.cloudstack.api.command.admin.user.UpdateUserCmd;
import org.apache.cloudstack.api.command.user.region.ListRegionsCmd;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegionServiceImpl extends ManagerBase implements RegionService, Manager {
    public static final Logger s_logger = LoggerFactory.getLogger(RegionServiceImpl.class);

    @Inject
    private RegionManager _regionMgr;

    private String _name;

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region addRegion(final int id, final String name, final String endPoint) {
        //Check for valid Name
        //Check valid end_point url
        return _regionMgr.addRegion(id, name, endPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region updateRegion(final int id, final String name, final String endPoint) {
        //Check for valid Name
        //Check valid end_point url
        return _regionMgr.updateRegion(id, name, endPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeRegion(final int id) {
        return _regionMgr.removeRegion(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Region> listRegions(final ListRegionsCmd cmd) {
        return _regionMgr.listRegions(cmd.getId(), cmd.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteUserAccount(final DeleteAccountCmd cmd) {
        return _regionMgr.deleteUserAccount(cmd.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account updateAccount(final UpdateAccountCmd cmd) {
        return _regionMgr.updateAccount(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account disableAccount(final DisableAccountCmd cmd) throws ConcurrentOperationException, ResourceUnavailableException {
        return _regionMgr.disableAccount(cmd.getAccountName(), cmd.getDomainId(), cmd.getId(), cmd.getLockRequested());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account enableAccount(final EnableAccountCmd cmd) {
        return _regionMgr.enableAccount(cmd.getAccountName(), cmd.getDomainId(), cmd.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteUser(final DeleteUserCmd cmd) {
        return _regionMgr.deleteUser(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Domain updateDomain(final UpdateDomainCmd cmd) {
        return _regionMgr.updateDomain(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDomain(final DeleteDomainCmd cmd) {
        return _regionMgr.deleteDomain(cmd.getId(), cmd.getCleanup());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount updateUser(final UpdateUserCmd cmd) {
        return _regionMgr.updateUser(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount disableUser(final DisableUserCmd cmd) {
        return _regionMgr.disableUser(cmd.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount enableUser(final EnableUserCmd cmd) {
        return _regionMgr.enableUser(cmd.getId());
    }
}
