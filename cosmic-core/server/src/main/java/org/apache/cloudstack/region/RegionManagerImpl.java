package org.apache.cloudstack.region;

import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.user.UserAccount;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DbProperties;
import org.apache.cloudstack.api.command.admin.account.UpdateAccountCmd;
import org.apache.cloudstack.api.command.admin.domain.UpdateDomainCmd;
import org.apache.cloudstack.api.command.admin.user.DeleteUserCmd;
import org.apache.cloudstack.api.command.admin.user.UpdateUserCmd;
import org.apache.cloudstack.region.dao.RegionDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegionManagerImpl extends ManagerBase implements RegionManager, Manager {
    public static final Logger s_logger = LoggerFactory.getLogger(RegionManagerImpl.class);

    @Inject
    RegionDao _regionDao;
    @Inject
    AccountDao _accountDao;
    @Inject
    private AccountManager _accountMgr;
    @Inject
    private DomainManager _domainMgr;

    private String _name;
    private int _id;

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        final Properties dbProps = DbProperties.getDbProperties();
        final String regionId = dbProps.getProperty("region.id");
        _id = 1;
        if (regionId != null) {
            _id = Integer.parseInt(regionId);
        }
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

    @Override
    public int getId() {
        return _id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region addRegion(final int id, final String name, final String endPoint) {
        //Region Id should be unique
        if (_regionDao.findById(id) != null) {
            throw new InvalidParameterValueException("Region with id: " + id + " already exists");
        }
        //Region Name should be unique
        if (_regionDao.findByName(name) != null) {
            throw new InvalidParameterValueException("Region with name: " + name + " already exists");
        }
        final RegionVO region = new RegionVO(id, name, endPoint);
        return _regionDao.persist(region);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region updateRegion(final int id, final String name, final String endPoint) {
        final RegionVO region = _regionDao.findById(id);

        if (region == null) {
            throw new InvalidParameterValueException("Region with id: " + id + " does not exist");
        }

        //Ensure region name is unique
        if (name != null) {
            final RegionVO region1 = _regionDao.findByName(name);
            if (region1 != null && id != region1.getId()) {
                throw new InvalidParameterValueException("Region with name: " + name + " already exists");
            }
        }

        if (name != null) {
            region.setName(name);
        }

        if (endPoint != null) {
            region.setEndPoint(endPoint);
        }

        _regionDao.update(id, region);
        return _regionDao.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeRegion(final int id) {
        final RegionVO region = _regionDao.findById(id);
        if (region == null) {
            throw new InvalidParameterValueException("Failed to delete Region: " + id + ", Region not found");
        }
        return _regionDao.remove(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegionVO> listRegions(final Integer id, final String name) {
        final List<RegionVO> regions = new ArrayList<>();
        if (id != null) {
            final RegionVO region = _regionDao.findById(id);
            if (region != null) {
                regions.add(region);
            }
            return regions;
        }
        if (name != null) {
            final RegionVO region = _regionDao.findByName(name);
            if (region != null) {
                regions.add(region);
            }
            return regions;
        }
        return _regionDao.listAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteUserAccount(final long accountId) {
        return _accountMgr.deleteUserAccount(accountId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account updateAccount(final UpdateAccountCmd cmd) {
        return _accountMgr.updateAccount(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account disableAccount(final String accountName, final Long domainId, final Long accountId, final Boolean lockRequested) throws ConcurrentOperationException,
            ResourceUnavailableException {
        Account account = null;
        if (lockRequested) {
            account = _accountMgr.lockAccount(accountName, domainId, accountId);
        } else {
            account = _accountMgr.disableAccount(accountName, domainId, accountId);
        }
        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account enableAccount(final String accountName, final Long domainId, final Long accountId) {
        return _accountMgr.enableAccount(accountName, domainId, accountId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteUser(final DeleteUserCmd cmd) {
        return _accountMgr.deleteUser(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Domain updateDomain(final UpdateDomainCmd cmd) {
        return _domainMgr.updateDomain(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDomain(final Long id, final Boolean cleanup) {
        return _domainMgr.deleteDomain(id, cleanup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount updateUser(final UpdateUserCmd cmd) {
        return _accountMgr.updateUser(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount disableUser(final Long userId) {
        return _accountMgr.disableUser(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount enableUser(final long userId) {
        return _accountMgr.enableUser(userId);
    }
}
