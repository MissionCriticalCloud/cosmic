package com.cloud.resource;

import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.org.Grouping;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;

import org.apache.log4j.Logger;

public class ResourceChecker {

    private static final Logger logger = Logger.getLogger(ResourceManagerImpl.class);

    private final DataCenterDao dataCenterDao;
    private final AccountManager accountManager;
    private final HostPodDao hostPodDao;

    private ResourceChecker(final Builder builder) {
        dataCenterDao = builder.dataCenterDao;
        accountManager = builder.accountManager;
        hostPodDao = builder.hostPodDao;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DataCenterVO checkIfDataCenterExists(final Long dataCenterId) {
        logger.debug("Checking if data center" + dataCenterId + " exists");
        final DataCenterVO zone = dataCenterDao.findById(dataCenterId);
        if (zone == null) {
            final InvalidParameterValueException e = new InvalidParameterValueException("Can't find data center by the id: " + dataCenterId);
            e.addProxyObject(String.valueOf(dataCenterId), "dcId");
            throw e;
        }
        logger.debug("Data center " + dataCenterId + "exists");
        return zone;
    }

    public void checkIfDataCenterIsUsable(final DataCenter dataCenter, final Account account) {
        final long dataCenterId = dataCenter.getId();
        logger.debug("Checking if data center " + dataCenterId + " is usable.");
        if (Grouping.AllocationState.Disabled == dataCenter.getAllocationState() && !accountManager.isRootAdmin(account.getId())) {
            final PermissionDeniedException ex = new PermissionDeniedException("Cannot perform this operation, data center with specified id is currently disabled");
            ex.addProxyObject(dataCenter.getUuid(), "dcId");
            throw ex;
        }
        logger.debug("Data center " + dataCenterId + "is usable");
    }

    public HostPodVO checkIfPodExists(final Long hostPodId) {
        logger.debug("Checking if pod " + hostPodId + " exists");
        final HostPodVO pod = hostPodDao.findById(hostPodId);
        if (pod == null) {
            throw new InvalidParameterValueException("Can't find pod by id " + hostPodId);
        }
        return pod;
    }

    public void checkIfPodIsUsable(final DataCenter dataCenter, final HostPodVO hostPod) {
        final long hostPodId = hostPod.getId();
        logger.debug("Checking if pod " + hostPodId + " is usable.");
        if (!hostPod.belongsToDataCenter(dataCenter.getId())) {
            final String hostPodUuid = hostPod.getUuid();
            final String dataCenterUuid = dataCenter.getUuid();
            final InvalidParameterValueException ex =
                    new InvalidParameterValueException("Pod with specified pod " + hostPodUuid + " doesn't belong to the zone with specified dataCenter " + dataCenterUuid);
            ex.addProxyObject(hostPodUuid, "podId");
            ex.addProxyObject(dataCenterUuid, "dcId");
            throw ex;
        }
        logger.debug("Pod " + hostPodId + "is usable");
    }

    public static class Builder {
        private DataCenterDao dataCenterDao;
        private AccountManager accountManager;
        private HostPodDao hostPodDao;

        public Builder dataCenterDao(final DataCenterDao dataCenterDao) {
            this.dataCenterDao = dataCenterDao;
            return this;
        }

        public Builder accountManager(final AccountManager accountManager) {
            this.accountManager = accountManager;
            return this;
        }

        public Builder hostPodDao(final HostPodDao hostPodDao) {
            this.hostPodDao = hostPodDao;
            return this;
        }

        public ResourceChecker build() {
            return new ResourceChecker(this);
        }
    }
}
