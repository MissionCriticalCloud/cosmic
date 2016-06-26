package org.apache.cloudstack.dedicated;

import com.cloud.configuration.Config;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.DedicatedResources;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupService;
import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.api.commands.DedicateClusterCmd;
import org.apache.cloudstack.api.commands.DedicateHostCmd;
import org.apache.cloudstack.api.commands.DedicatePodCmd;
import org.apache.cloudstack.api.commands.DedicateZoneCmd;
import org.apache.cloudstack.api.commands.ListDedicatedClustersCmd;
import org.apache.cloudstack.api.commands.ListDedicatedHostsCmd;
import org.apache.cloudstack.api.commands.ListDedicatedPodsCmd;
import org.apache.cloudstack.api.commands.ListDedicatedZonesCmd;
import org.apache.cloudstack.api.commands.ReleaseDedicatedClusterCmd;
import org.apache.cloudstack.api.commands.ReleaseDedicatedHostCmd;
import org.apache.cloudstack.api.commands.ReleaseDedicatedPodCmd;
import org.apache.cloudstack.api.commands.ReleaseDedicatedZoneCmd;
import org.apache.cloudstack.api.response.DedicateClusterResponse;
import org.apache.cloudstack.api.response.DedicateHostResponse;
import org.apache.cloudstack.api.response.DedicatePodResponse;
import org.apache.cloudstack.api.response.DedicateZoneResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Local({DedicatedService.class})
public class DedicatedResourceManagerImpl implements DedicatedService {
    private static final Logger s_logger = LoggerFactory.getLogger(DedicatedResourceManagerImpl.class);

    @Inject
    AccountDao _accountDao;
    @Inject
    DomainDao _domainDao;
    @Inject
    HostPodDao _podDao;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    HostDao _hostDao;
    @Inject
    DedicatedResourceDao _dedicatedDao;
    @Inject
    DataCenterDao _zoneDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    UserVmDao _userVmDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    AffinityGroupDao _affinityGroupDao;

    @Inject
    AffinityGroupService _affinityGroupService;

    private int capacityReleaseInterval;

    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        capacityReleaseInterval = NumbersUtil.parseInt(_configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);
        return true;
    }

    @Override
    public DedicatePodResponse createDedicatePodResponse(final DedicatedResources resource) {
        final DedicatePodResponse dedicatePodResponse = new DedicatePodResponse();
        final HostPodVO pod = _podDao.findById(resource.getPodId());
        final DomainVO domain = _domainDao.findById(resource.getDomainId());
        final AccountVO account = _accountDao.findById(resource.getAccountId());
        final AffinityGroup group = _affinityGroupDao.findById(resource.getAffinityGroupId());
        dedicatePodResponse.setId(resource.getUuid());
        dedicatePodResponse.setPodId(pod.getUuid());
        dedicatePodResponse.setPodName(pod.getName());
        dedicatePodResponse.setDomainId(domain.getUuid());
        dedicatePodResponse.setAffinityGroupId(group.getUuid());
        if (account != null) {
            dedicatePodResponse.setAccountId(account.getUuid());
        }
        dedicatePodResponse.setObjectName("dedicatedpod");
        return dedicatePodResponse;
    }

    @Override
    public DedicateClusterResponse createDedicateClusterResponse(final DedicatedResources resource) {
        final DedicateClusterResponse dedicateClusterResponse = new DedicateClusterResponse();
        final ClusterVO cluster = _clusterDao.findById(resource.getClusterId());
        final DomainVO domain = _domainDao.findById(resource.getDomainId());
        final AccountVO account = _accountDao.findById(resource.getAccountId());
        final AffinityGroup group = _affinityGroupDao.findById(resource.getAffinityGroupId());
        dedicateClusterResponse.setId(resource.getUuid());
        dedicateClusterResponse.setClusterId(cluster.getUuid());
        dedicateClusterResponse.setClusterName(cluster.getName());
        dedicateClusterResponse.setDomainId(domain.getUuid());
        dedicateClusterResponse.setAffinityGroupId(group.getUuid());
        if (account != null) {
            dedicateClusterResponse.setAccountId(account.getUuid());
        }
        dedicateClusterResponse.setObjectName("dedicatedcluster");
        return dedicateClusterResponse;
    }

    @Override
    public DedicateHostResponse createDedicateHostResponse(final DedicatedResources resource) {
        final DedicateHostResponse dedicateHostResponse = new DedicateHostResponse();
        final HostVO host = _hostDao.findById(resource.getHostId());
        final DomainVO domain = _domainDao.findById(resource.getDomainId());
        final AccountVO account = _accountDao.findById(resource.getAccountId());
        final AffinityGroup group = _affinityGroupDao.findById(resource.getAffinityGroupId());
        dedicateHostResponse.setId(resource.getUuid());
        dedicateHostResponse.setHostId(host.getUuid());
        dedicateHostResponse.setHostName(host.getName());
        dedicateHostResponse.setDomainId(domain.getUuid());
        dedicateHostResponse.setAffinityGroupId(group.getUuid());
        if (account != null) {
            dedicateHostResponse.setAccountId(account.getUuid());
        }
        dedicateHostResponse.setObjectName("dedicatedhost");
        return dedicateHostResponse;
    }

    @Override
    public Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedPods(final ListDedicatedPodsCmd cmd) {
        final Long podId = cmd.getPodId();
        final Long domainId = cmd.getDomainId();
        final String accountName = cmd.getAccountName();
        Long accountId = null;
        final Long affinityGroupId = cmd.getAffinityGroupId();

        if (accountName != null) {
            if (domainId != null) {
                final Account account = _accountDao.findActiveAccount(accountName, domainId);
                if (account != null) {
                    accountId = account.getId();
                }
            } else {
                throw new InvalidParameterValueException("Please specify the domain id of the account: " + accountName);
            }
        }
        final Pair<List<DedicatedResourceVO>, Integer> result = _dedicatedDao.searchDedicatedPods(podId, domainId, accountId, affinityGroupId);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    public Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedHosts(final ListDedicatedHostsCmd cmd) {
        final Long hostId = cmd.getHostId();
        final Long domainId = cmd.getDomainId();
        final String accountName = cmd.getAccountName();
        final Long affinityGroupId = cmd.getAffinityGroupId();

        Long accountId = null;
        if (accountName != null) {
            if (domainId != null) {
                final Account account = _accountDao.findActiveAccount(accountName, domainId);
                if (account != null) {
                    accountId = account.getId();
                }
            } else {
                throw new InvalidParameterValueException("Please specify the domain id of the account: " + accountName);
            }
        }

        final Pair<List<DedicatedResourceVO>, Integer> result = _dedicatedDao.searchDedicatedHosts(hostId, domainId, accountId, affinityGroupId);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    public Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedClusters(final ListDedicatedClustersCmd cmd) {
        final Long clusterId = cmd.getClusterId();
        final Long domainId = cmd.getDomainId();
        final String accountName = cmd.getAccountName();
        Long accountId = null;
        final Long affinityGroupId = cmd.getAffinityGroupId();

        if (accountName != null) {
            if (domainId != null) {
                final Account account = _accountDao.findActiveAccount(accountName, domainId);
                if (account != null) {
                    accountId = account.getId();
                }
            } else {
                throw new InvalidParameterValueException("Please specify the domain id of the account: " + accountName);
            }
        }
        final Pair<List<DedicatedResourceVO>, Integer> result = _dedicatedDao.searchDedicatedClusters(clusterId, domainId, accountId, affinityGroupId);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_DEDICATE_RESOURCE_RELEASE, eventDescription = "Releasing dedicated resource")
    public boolean releaseDedicatedResource(final Long zoneId, final Long podId, final Long clusterId, final Long hostId) throws InvalidParameterValueException {
        DedicatedResourceVO resource = null;
        if (zoneId != null) {
            resource = _dedicatedDao.findByZoneId(zoneId);
        }
        if (podId != null) {
            resource = _dedicatedDao.findByPodId(podId);
        }
        if (clusterId != null) {
            resource = _dedicatedDao.findByClusterId(clusterId);
        }
        if (hostId != null) {
            resource = _dedicatedDao.findByHostId(hostId);
        }
        if (resource == null) {
            throw new InvalidParameterValueException("No Dedicated Resource available to release");
        } else {
            final DedicatedResourceVO resourceFinal = resource;
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final Long resourceId = resourceFinal.getId();
                    if (!_dedicatedDao.remove(resourceId)) {
                        throw new CloudRuntimeException("Failed to delete Resource " + resourceId);
                    }
                    if (zoneId != null) {
                        // remove the domainId set in zone
                        final DataCenterVO dc = _zoneDao.findById(zoneId);
                        if (dc != null) {
                            dc.setDomainId(null);
                            dc.setDomain(null);
                            if (!_zoneDao.update(zoneId, dc)) {
                                throw new CloudRuntimeException("Failed to release dedicated zone, could not clear domainId. Please contact Cloud Support.");
                            }
                        }
                    }
                }
            });

            // find the group associated and check if there are any more
            // resources under that group
            final List<DedicatedResourceVO> resourcesInGroup = _dedicatedDao.listByAffinityGroupId(resource.getAffinityGroupId());
            if (resourcesInGroup.isEmpty()) {
                // delete the group
                _affinityGroupService.deleteAffinityGroup(resource.getAffinityGroupId(), null, null, null, null);
            }
        }
        return true;
    }

    @Override
    public DedicateZoneResponse createDedicateZoneResponse(final DedicatedResources resource) {
        final DedicateZoneResponse dedicateZoneResponse = new DedicateZoneResponse();
        final DataCenterVO dc = _zoneDao.findById(resource.getDataCenterId());
        final DomainVO domain = _domainDao.findById(resource.getDomainId());
        final AccountVO account = _accountDao.findById(resource.getAccountId());
        final AffinityGroup group = _affinityGroupDao.findById(resource.getAffinityGroupId());
        dedicateZoneResponse.setId(resource.getUuid());
        dedicateZoneResponse.setZoneId(dc.getUuid());
        dedicateZoneResponse.setZoneName(dc.getName());
        dedicateZoneResponse.setDomainId(domain.getUuid());
        dedicateZoneResponse.setAffinityGroupId(group.getUuid());
        if (account != null) {
            dedicateZoneResponse.setAccountId(account.getUuid());
        }
        dedicateZoneResponse.setObjectName("dedicatedzone");
        return dedicateZoneResponse;
    }

    @Override
    public Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedZones(final ListDedicatedZonesCmd cmd) {
        final Long zoneId = cmd.getZoneId();
        final Long domainId = cmd.getDomainId();
        final String accountName = cmd.getAccountName();
        Long accountId = null;
        final Long affinityGroupId = cmd.getAffinityGroupId();

        if (accountName != null) {
            if (domainId != null) {
                final Account account = _accountDao.findActiveAccount(accountName, domainId);
                if (account != null) {
                    accountId = account.getId();
                }
            } else {
                throw new InvalidParameterValueException("Please specify the domain id of the account: " + accountName);
            }
        }
        final Pair<List<DedicatedResourceVO>, Integer> result = _dedicatedDao.searchDedicatedZones(zoneId, domainId, accountId, affinityGroupId);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_DEDICATE_RESOURCE, eventDescription = "dedicating a Zone")
    public List<DedicatedResourceVO> dedicateZone(final Long zoneId, final Long domainId, final String accountName) {
        Long accountId = null;
        List<HostVO> hosts = null;
        if (accountName != null) {
            final Account caller = CallContext.current().getCallingAccount();
            final Account owner = _accountMgr.finalizeOwner(caller, accountName, domainId, null);
            accountId = owner.getId();
        }
        final List<Long> childDomainIds = getDomainChildIds(domainId);
        childDomainIds.add(domainId);
        checkAccountAndDomain(accountId, domainId);
        final DataCenterVO dc = _zoneDao.findById(zoneId);
        if (dc == null) {
            throw new InvalidParameterValueException("Unable to find zone by id " + zoneId);
        } else {
            final DedicatedResourceVO dedicatedZone = _dedicatedDao.findByZoneId(zoneId);
            //check if zone is dedicated
            if (dedicatedZone != null) {
                s_logger.error("Zone " + dc.getName() + " is already dedicated");
                throw new CloudRuntimeException("Zone  " + dc.getName() + " is already dedicated");
            }

            //check if any resource under this zone is dedicated to different account or sub-domain
            final List<HostPodVO> pods = _podDao.listByDataCenterId(dc.getId());
            final List<DedicatedResourceVO> podsToRelease = new ArrayList<>();
            final List<DedicatedResourceVO> clustersToRelease = new ArrayList<>();
            final List<DedicatedResourceVO> hostsToRelease = new ArrayList<>();
            for (final HostPodVO pod : pods) {
                final DedicatedResourceVO dPod = _dedicatedDao.findByPodId(pod.getId());
                if (dPod != null) {
                    if (!(childDomainIds.contains(dPod.getDomainId()))) {
                        throw new CloudRuntimeException("Pod " + pod.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                    }
                    if (accountId != null) {
                        if (dPod.getAccountId().equals(accountId)) {
                            podsToRelease.add(dPod);
                        } else {
                            s_logger.error("Pod " + pod.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                            throw new CloudRuntimeException("Pod " + pod.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                        }
                    } else {
                        if (dPod.getAccountId() == null && dPod.getDomainId().equals(domainId)) {
                            podsToRelease.add(dPod);
                        }
                    }
                }
            }

            for (final DedicatedResourceVO dr : podsToRelease) {
                releaseDedicatedResource(null, dr.getPodId(), null, null);
            }

            final List<ClusterVO> clusters = _clusterDao.listClustersByDcId(dc.getId());
            for (final ClusterVO cluster : clusters) {
                final DedicatedResourceVO dCluster = _dedicatedDao.findByClusterId(cluster.getId());
                if (dCluster != null) {
                    if (!(childDomainIds.contains(dCluster.getDomainId()))) {
                        throw new CloudRuntimeException("Cluster " + cluster.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                    }
                    if (accountId != null) {
                        if (dCluster.getAccountId().equals(accountId)) {
                            clustersToRelease.add(dCluster);
                        } else {
                            s_logger.error("Cluster " + cluster.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                            throw new CloudRuntimeException("Cluster " + cluster.getName() + " under this Zone " + dc.getName() +
                                    " is dedicated to different account/domain");
                        }
                    } else {
                        if (dCluster.getAccountId() == null && dCluster.getDomainId().equals(domainId)) {
                            clustersToRelease.add(dCluster);
                        }
                    }
                }
            }

            for (final DedicatedResourceVO dr : clustersToRelease) {
                releaseDedicatedResource(null, null, dr.getClusterId(), null);
            }

            hosts = _hostDao.listByDataCenterId(dc.getId());
            for (final HostVO host : hosts) {
                final DedicatedResourceVO dHost = _dedicatedDao.findByHostId(host.getId());
                if (dHost != null) {
                    if (!(childDomainIds.contains(dHost.getDomainId()))) {
                        throw new CloudRuntimeException("Host " + host.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                    }
                    if (accountId != null) {
                        if (dHost.getAccountId().equals(accountId)) {
                            hostsToRelease.add(dHost);
                        } else {
                            s_logger.error("Host " + host.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                            throw new CloudRuntimeException("Host " + host.getName() + " under this Zone " + dc.getName() + " is dedicated to different account/domain");
                        }
                    } else {
                        if (dHost.getAccountId() == null && dHost.getDomainId().equals(domainId)) {
                            hostsToRelease.add(dHost);
                        }
                    }
                }
            }

            for (final DedicatedResourceVO dr : hostsToRelease) {
                releaseDedicatedResource(null, null, null, dr.getHostId());
            }
        }

        checkHostsSuitabilityForExplicitDedication(accountId, childDomainIds, hosts);

        final Long accountIdFinal = accountId;
        return Transaction.execute(new TransactionCallback<List<DedicatedResourceVO>>() {
            @Override
            public List<DedicatedResourceVO> doInTransaction(final TransactionStatus status) {
                // find or create the affinity group by name under this account/domain
                final AffinityGroup group = findOrCreateDedicatedAffinityGroup(domainId, accountIdFinal);
                if (group == null) {
                    s_logger.error("Unable to dedicate zone due to, failed to create dedication affinity group");
                    throw new CloudRuntimeException("Failed to dedicate zone. Please contact Cloud Support.");
                }

                DedicatedResourceVO dedicatedResource = new DedicatedResourceVO(zoneId, null, null, null, null, null, group.getId());
                try {
                    dedicatedResource.setDomainId(domainId);
                    if (accountIdFinal != null) {
                        dedicatedResource.setAccountId(accountIdFinal);
                    }
                    dedicatedResource = _dedicatedDao.persist(dedicatedResource);

                    // save the domainId in the zone
                    dc.setDomainId(domainId);
                    if (!_zoneDao.update(zoneId, dc)) {
                        throw new CloudRuntimeException("Failed to dedicate zone, could not set domainId. Please contact Cloud Support.");
                    }
                } catch (final Exception e) {
                    s_logger.error("Unable to dedicate zone due to " + e.getMessage(), e);
                    throw new CloudRuntimeException("Failed to dedicate zone. Please contact Cloud Support.");
                }

                final List<DedicatedResourceVO> result = new ArrayList<>();
                result.add(dedicatedResource);
                return result;
            }
        });
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_DEDICATE_RESOURCE, eventDescription = "dedicating a Pod")
    public List<DedicatedResourceVO> dedicatePod(final Long podId, final Long domainId, final String accountName) {
        Long accountId = null;
        if (accountName != null) {
            final Account caller = CallContext.current().getCallingAccount();
            final Account owner = _accountMgr.finalizeOwner(caller, accountName, domainId, null);
            accountId = owner.getId();
        }
        final List<Long> childDomainIds = getDomainChildIds(domainId);
        childDomainIds.add(domainId);
        checkAccountAndDomain(accountId, domainId);
        final HostPodVO pod = _podDao.findById(podId);
        List<HostVO> hosts = null;
        if (pod == null) {
            throw new InvalidParameterValueException("Unable to find pod by id " + podId);
        } else {
            final DedicatedResourceVO dedicatedPod = _dedicatedDao.findByPodId(podId);
            final DedicatedResourceVO dedicatedZoneOfPod = _dedicatedDao.findByZoneId(pod.getDataCenterId());
            //check if pod is dedicated
            if (dedicatedPod != null) {
                s_logger.error("Pod " + pod.getName() + " is already dedicated");
                throw new CloudRuntimeException("Pod " + pod.getName() + " is already dedicated");
            }

            if (dedicatedZoneOfPod != null) {
                final boolean domainIdInChildreanList = getDomainChildIds(dedicatedZoneOfPod.getDomainId()).contains(domainId);
                //can dedicate a pod to an account/domain if zone is dedicated to parent-domain
                if (dedicatedZoneOfPod.getAccountId() != null || (accountId == null && !domainIdInChildreanList) ||
                        (accountId != null && !(dedicatedZoneOfPod.getDomainId().equals(domainId) || domainIdInChildreanList))) {
                    final DataCenterVO zone = _zoneDao.findById(pod.getDataCenterId());
                    s_logger.error("Cannot dedicate Pod. Its zone is already dedicated");
                    throw new CloudRuntimeException("Pod's Zone " + zone.getName() + " is already dedicated");
                }
            }

            //check if any resource under this pod is dedicated to different account or sub-domain
            final List<ClusterVO> clusters = _clusterDao.listByPodId(pod.getId());
            final List<DedicatedResourceVO> clustersToRelease = new ArrayList<>();
            final List<DedicatedResourceVO> hostsToRelease = new ArrayList<>();
            for (final ClusterVO cluster : clusters) {
                final DedicatedResourceVO dCluster = _dedicatedDao.findByClusterId(cluster.getId());
                if (dCluster != null) {
                    if (!(childDomainIds.contains(dCluster.getDomainId()))) {
                        throw new CloudRuntimeException("Cluster " + cluster.getName() + " under this Pod " + pod.getName() + " is dedicated to different account/domain");
                    }
                    /*if all dedicated resources belongs to same account and domain then we should release dedication
                    and make new entry for this Pod*/
                    if (accountId != null) {
                        if (dCluster.getAccountId().equals(accountId)) {
                            clustersToRelease.add(dCluster);
                        } else {
                            s_logger.error("Cluster " + cluster.getName() + " under this Pod " + pod.getName() + " is dedicated to different account/domain");
                            throw new CloudRuntimeException("Cluster " + cluster.getName() + " under this Pod " + pod.getName() +
                                    " is dedicated to different account/domain");
                        }
                    } else {
                        if (dCluster.getAccountId() == null && dCluster.getDomainId().equals(domainId)) {
                            clustersToRelease.add(dCluster);
                        }
                    }
                }
            }

            for (final DedicatedResourceVO dr : clustersToRelease) {
                releaseDedicatedResource(null, null, dr.getClusterId(), null);
            }

            hosts = _hostDao.findByPodId(pod.getId());
            for (final HostVO host : hosts) {
                final DedicatedResourceVO dHost = _dedicatedDao.findByHostId(host.getId());
                if (dHost != null) {
                    if (!(getDomainChildIds(domainId).contains(dHost.getDomainId()))) {
                        throw new CloudRuntimeException("Host " + host.getName() + " under this Pod " + pod.getName() + " is dedicated to different account/domain");
                    }
                    if (accountId != null) {
                        if (dHost.getAccountId().equals(accountId)) {
                            hostsToRelease.add(dHost);
                        } else {
                            s_logger.error("Host " + host.getName() + " under this Pod " + pod.getName() + " is dedicated to different account/domain");
                            throw new CloudRuntimeException("Host " + host.getName() + " under this Pod " + pod.getName() + " is dedicated to different account/domain");
                        }
                    } else {
                        if (dHost.getAccountId() == null && dHost.getDomainId().equals(domainId)) {
                            hostsToRelease.add(dHost);
                        }
                    }
                }
            }

            for (final DedicatedResourceVO dr : hostsToRelease) {
                releaseDedicatedResource(null, null, null, dr.getHostId());
            }
        }

        checkHostsSuitabilityForExplicitDedication(accountId, childDomainIds, hosts);

        final Long accountIdFinal = accountId;
        return Transaction.execute(new TransactionCallback<List<DedicatedResourceVO>>() {
            @Override
            public List<DedicatedResourceVO> doInTransaction(final TransactionStatus status) {
                // find or create the affinity group by name under this account/domain
                final AffinityGroup group = findOrCreateDedicatedAffinityGroup(domainId, accountIdFinal);
                if (group == null) {
                    s_logger.error("Unable to dedicate zone due to, failed to create dedication affinity group");
                    throw new CloudRuntimeException("Failed to dedicate zone. Please contact Cloud Support.");
                }
                DedicatedResourceVO dedicatedResource = new DedicatedResourceVO(null, podId, null, null, null, null, group.getId());
                try {
                    dedicatedResource.setDomainId(domainId);
                    if (accountIdFinal != null) {
                        dedicatedResource.setAccountId(accountIdFinal);
                    }
                    dedicatedResource = _dedicatedDao.persist(dedicatedResource);
                } catch (final Exception e) {
                    s_logger.error("Unable to dedicate pod due to " + e.getMessage(), e);
                    throw new CloudRuntimeException("Failed to dedicate pod. Please contact Cloud Support.");
                }

                final List<DedicatedResourceVO> result = new ArrayList<>();
                result.add(dedicatedResource);
                return result;
            }
        });
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_DEDICATE_RESOURCE, eventDescription = "dedicating a Cluster")
    public List<DedicatedResourceVO> dedicateCluster(final Long clusterId, final Long domainId, final String accountName) {
        Long accountId = null;
        List<HostVO> hosts = null;
        if (accountName != null) {
            final Account caller = CallContext.current().getCallingAccount();
            final Account owner = _accountMgr.finalizeOwner(caller, accountName, domainId, null);
            accountId = owner.getId();
        }
        final List<Long> childDomainIds = getDomainChildIds(domainId);
        childDomainIds.add(domainId);
        checkAccountAndDomain(accountId, domainId);
        final ClusterVO cluster = _clusterDao.findById(clusterId);
        if (cluster == null) {
            throw new InvalidParameterValueException("Unable to find cluster by id " + clusterId);
        } else {
            final DedicatedResourceVO dedicatedCluster = _dedicatedDao.findByClusterId(clusterId);
            final DedicatedResourceVO dedicatedPodOfCluster = _dedicatedDao.findByPodId(cluster.getPodId());
            final DedicatedResourceVO dedicatedZoneOfCluster = _dedicatedDao.findByZoneId(cluster.getDataCenterId());

            //check if cluster is dedicated
            if (dedicatedCluster != null) {
                s_logger.error("Cluster " + cluster.getName() + " is already dedicated");
                throw new CloudRuntimeException("Cluster " + cluster.getName() + " is already dedicated");
            }

            if (dedicatedPodOfCluster != null) {
                final boolean domainIdInChildreanList = getDomainChildIds(dedicatedPodOfCluster.getDomainId()).contains(domainId);
                //can dedicate a cluster to an account/domain if pod is dedicated to parent-domain
                if (dedicatedPodOfCluster.getAccountId() != null || (accountId == null && !domainIdInChildreanList) ||
                        (accountId != null && !(dedicatedPodOfCluster.getDomainId().equals(domainId) || domainIdInChildreanList))) {
                    s_logger.error("Cannot dedicate Cluster. Its Pod is already dedicated");
                    final HostPodVO pod = _podDao.findById(cluster.getPodId());
                    throw new CloudRuntimeException("Cluster's Pod " + pod.getName() + " is already dedicated");
                }
            }

            if (dedicatedZoneOfCluster != null) {
                final boolean domainIdInChildreanList = getDomainChildIds(dedicatedZoneOfCluster.getDomainId()).contains(domainId);
                //can dedicate a cluster to an account/domain if zone is dedicated to parent-domain
                if (dedicatedZoneOfCluster.getAccountId() != null || (accountId == null && !domainIdInChildreanList) ||
                        (accountId != null && !(dedicatedZoneOfCluster.getDomainId().equals(domainId) || domainIdInChildreanList))) {
                    s_logger.error("Cannot dedicate Cluster. Its zone is already dedicated");
                    final DataCenterVO zone = _zoneDao.findById(cluster.getDataCenterId());
                    throw new CloudRuntimeException("Cluster's Zone " + zone.getName() + " is already dedicated");
                }
            }

            //check if any resource under this cluster is dedicated to different account or sub-domain
            hosts = _hostDao.findByClusterId(cluster.getId());
            final List<DedicatedResourceVO> hostsToRelease = new ArrayList<>();
            for (final HostVO host : hosts) {
                final DedicatedResourceVO dHost = _dedicatedDao.findByHostId(host.getId());
                if (dHost != null) {
                    if (!(childDomainIds.contains(dHost.getDomainId()))) {
                        throw new CloudRuntimeException("Host " + host.getName() + " under this Cluster " + cluster.getName() +
                                " is dedicated to different account/domain");
                    }
                    /*if all dedicated resources belongs to same account and domain then we should release dedication
                    and make new entry for this cluster */
                    if (accountId != null) {
                        if (dHost.getAccountId().equals(accountId)) {
                            hostsToRelease.add(dHost);
                        } else {
                            s_logger.error("Cannot dedicate Cluster " + cluster.getName() + " to account" + accountName);
                            throw new CloudRuntimeException("Cannot dedicate Cluster " + cluster.getName() + " to account" + accountName);
                        }
                    } else {
                        if (dHost.getAccountId() == null && dHost.getDomainId().equals(domainId)) {
                            hostsToRelease.add(dHost);
                        }
                    }
                }
            }

            for (final DedicatedResourceVO dr : hostsToRelease) {
                releaseDedicatedResource(null, null, null, dr.getHostId());
            }
        }

        checkHostsSuitabilityForExplicitDedication(accountId, childDomainIds, hosts);

        final Long accountIdFinal = accountId;
        return Transaction.execute(new TransactionCallback<List<DedicatedResourceVO>>() {
            @Override
            public List<DedicatedResourceVO> doInTransaction(final TransactionStatus status) {
                // find or create the affinity group by name under this account/domain
                final AffinityGroup group = findOrCreateDedicatedAffinityGroup(domainId, accountIdFinal);
                if (group == null) {
                    s_logger.error("Unable to dedicate zone due to, failed to create dedication affinity group");
                    throw new CloudRuntimeException("Failed to dedicate zone. Please contact Cloud Support.");
                }
                DedicatedResourceVO dedicatedResource = new DedicatedResourceVO(null, null, clusterId, null, null, null, group.getId());
                try {
                    dedicatedResource.setDomainId(domainId);
                    if (accountIdFinal != null) {
                        dedicatedResource.setAccountId(accountIdFinal);
                    }
                    dedicatedResource = _dedicatedDao.persist(dedicatedResource);
                } catch (final Exception e) {
                    s_logger.error("Unable to dedicate host due to " + e.getMessage(), e);
                    throw new CloudRuntimeException("Failed to dedicate cluster. Please contact Cloud Support.");
                }

                final List<DedicatedResourceVO> result = new ArrayList<>();
                result.add(dedicatedResource);
                return result;
            }
        });
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_DEDICATE_RESOURCE, eventDescription = "dedicating a Host")
    public List<DedicatedResourceVO> dedicateHost(final Long hostId, final Long domainId, final String accountName) {
        Long accountId = null;
        if (accountName != null) {
            final Account caller = CallContext.current().getCallingAccount();
            final Account owner = _accountMgr.finalizeOwner(caller, accountName, domainId, null);
            accountId = owner.getId();
        }
        checkAccountAndDomain(accountId, domainId);
        final HostVO host = _hostDao.findById(hostId);
        if (host == null) {
            throw new InvalidParameterValueException("Unable to find host by id " + hostId);
        } else {
            //check if host is of routing type
            if (host.getType() != Host.Type.Routing) {
                throw new CloudRuntimeException("Invalid host type for host " + host.getName());
            }

            final DedicatedResourceVO dedicatedHost = _dedicatedDao.findByHostId(hostId);
            final DedicatedResourceVO dedicatedClusterOfHost = _dedicatedDao.findByClusterId(host.getClusterId());
            final DedicatedResourceVO dedicatedPodOfHost = _dedicatedDao.findByPodId(host.getPodId());
            final DedicatedResourceVO dedicatedZoneOfHost = _dedicatedDao.findByZoneId(host.getDataCenterId());

            if (dedicatedHost != null) {
                s_logger.error("Host " + host.getName() + " is already dedicated");
                throw new CloudRuntimeException("Host " + host.getName() + " is already dedicated");
            }

            if (dedicatedClusterOfHost != null) {
                final boolean domainIdInChildreanList = getDomainChildIds(dedicatedClusterOfHost.getDomainId()).contains(domainId);
                //can dedicate a host to an account/domain if cluster is dedicated to parent-domain
                if (dedicatedClusterOfHost.getAccountId() != null || (accountId == null && !domainIdInChildreanList) ||
                        (accountId != null && !(dedicatedClusterOfHost.getDomainId().equals(domainId) || domainIdInChildreanList))) {
                    final ClusterVO cluster = _clusterDao.findById(host.getClusterId());
                    s_logger.error("Host's Cluster " + cluster.getName() + " is already dedicated");
                    throw new CloudRuntimeException("Host's Cluster " + cluster.getName() + " is already dedicated");
                }
            }

            if (dedicatedPodOfHost != null) {
                final boolean domainIdInChildreanList = getDomainChildIds(dedicatedPodOfHost.getDomainId()).contains(domainId);
                //can dedicate a host to an account/domain if pod is dedicated to parent-domain
                if (dedicatedPodOfHost.getAccountId() != null || (accountId == null && !domainIdInChildreanList) ||
                        (accountId != null && !(dedicatedPodOfHost.getDomainId().equals(domainId) || domainIdInChildreanList))) {
                    final HostPodVO pod = _podDao.findById(host.getPodId());
                    s_logger.error("Host's Pod " + pod.getName() + " is already dedicated");
                    throw new CloudRuntimeException("Host's Pod " + pod.getName() + " is already dedicated");
                }
            }

            if (dedicatedZoneOfHost != null) {
                final boolean domainIdInChildreanList = getDomainChildIds(dedicatedZoneOfHost.getDomainId()).contains(domainId);
                //can dedicate a host to an account/domain if zone is dedicated to parent-domain
                if (dedicatedZoneOfHost.getAccountId() != null || (accountId == null && !domainIdInChildreanList) ||
                        (accountId != null && !(dedicatedZoneOfHost.getDomainId().equals(domainId) || domainIdInChildreanList))) {
                    final DataCenterVO zone = _zoneDao.findById(host.getDataCenterId());
                    s_logger.error("Host's Data Center " + zone.getName() + " is already dedicated");
                    throw new CloudRuntimeException("Host's Data Center " + zone.getName() + " is already dedicated");
                }
            }
        }

        final List<Long> childDomainIds = getDomainChildIds(domainId);
        childDomainIds.add(domainId);
        checkHostSuitabilityForExplicitDedication(accountId, childDomainIds, hostId);

        final Long accountIdFinal = accountId;
        return Transaction.execute(new TransactionCallback<List<DedicatedResourceVO>>() {
            @Override
            public List<DedicatedResourceVO> doInTransaction(final TransactionStatus status) {
                // find or create the affinity group by name under this account/domain
                final AffinityGroup group = findOrCreateDedicatedAffinityGroup(domainId, accountIdFinal);
                if (group == null) {
                    s_logger.error("Unable to dedicate zone due to, failed to create dedication affinity group");
                    throw new CloudRuntimeException("Failed to dedicate zone. Please contact Cloud Support.");
                }
                DedicatedResourceVO dedicatedResource = new DedicatedResourceVO(null, null, null, hostId, null, null, group.getId());
                try {
                    dedicatedResource.setDomainId(domainId);
                    if (accountIdFinal != null) {
                        dedicatedResource.setAccountId(accountIdFinal);
                    }
                    dedicatedResource = _dedicatedDao.persist(dedicatedResource);
                } catch (final Exception e) {
                    s_logger.error("Unable to dedicate host due to " + e.getMessage(), e);
                    throw new CloudRuntimeException("Failed to dedicate host. Please contact Cloud Support.");
                }

                final List<DedicatedResourceVO> result = new ArrayList<>();
                result.add(dedicatedResource);
                return result;
            }
        });
    }

    private List<Long> getDomainChildIds(final long domainId) {
        final DomainVO domainRecord = _domainDao.findById(domainId);
        final List<Long> domainIds = new ArrayList<>();
        domainIds.add(domainRecord.getId());
        // find all domain Ids till leaf
        final List<DomainVO> allChildDomains = _domainDao.findAllChildren(domainRecord.getPath(), domainRecord.getId());
        for (final DomainVO domain : allChildDomains) {
            domainIds.add(domain.getId());
        }
        return domainIds;
    }

    private void checkAccountAndDomain(final Long accountId, final Long domainId) {
        final DomainVO domain = _domainDao.findById(domainId);
        if (domain == null) {
            throw new InvalidParameterValueException("Unable to find the domain by id " + domainId + ", please specify valid domainId");
        }
        //check if account belongs to the domain id
        if (accountId != null) {
            final AccountVO account = _accountDao.findById(accountId);
            if (account == null || domainId != account.getDomainId()) {
                throw new InvalidParameterValueException("Please specify the domain id of the account id " + accountId);
            }
        }
    }

    private boolean checkHostsSuitabilityForExplicitDedication(final Long accountId, final List<Long> domainIds, final List<HostVO> hosts) {
        final boolean suitable = true;
        for (final HostVO host : hosts) {
            checkHostSuitabilityForExplicitDedication(accountId, domainIds, host.getId());
        }
        return suitable;
    }

    private AffinityGroup findOrCreateDedicatedAffinityGroup(final Long domainId, final Long accountId) {
        if (domainId == null) {
            return null;
        }

        AffinityGroup group = null;
        String accountName = null;
        String affinityGroupName = null;

        if (accountId != null) {
            final AccountVO account = _accountDao.findById(accountId);
            accountName = account.getAccountName();

            group = _affinityGroupDao.findByAccountAndType(accountId, "ExplicitDedication");
            if (group != null) {
                return group;
            }
            // default to a groupname with account/domain information
            affinityGroupName = "DedicatedGrp-" + accountName;
        } else {
            // domain level group
            group = _affinityGroupDao.findDomainLevelGroupByType(domainId, "ExplicitDedication");
            if (group != null) {
                return group;
            }
            // default to a groupname with account/domain information
            final String domainName = _domainDao.findById(domainId).getName();
            affinityGroupName = "DedicatedGrp-domain-" + domainName;
        }

        group = _affinityGroupService.createAffinityGroup(accountName, null, domainId, affinityGroupName, "ExplicitDedication", "dedicated resources group");

        return group;
    }

    private boolean checkHostSuitabilityForExplicitDedication(final Long accountId, final List<Long> domainIds, final long hostId) {
        final boolean suitable = true;
        final List<UserVmVO> allVmsOnHost = getVmsOnHost(hostId);
        if (accountId != null) {
            for (final UserVmVO vm : allVmsOnHost) {
                if (vm.getAccountId() != accountId) {
                    s_logger.info("Host " + vm.getHostId() + " found to be unsuitable for explicit dedication as it is " + "running instances of another account");
                    throw new CloudRuntimeException("Host " + hostId + " found to be unsuitable for explicit dedication as it is " +
                            "running instances of another account");
                }
            }
        } else {
            for (final UserVmVO vm : allVmsOnHost) {
                if (!domainIds.contains(vm.getDomainId())) {
                    s_logger.info("Host " + vm.getHostId() + " found to be unsuitable for explicit dedication as it is " + "running instances of another domain");
                    throw new CloudRuntimeException("Host " + hostId + " found to be unsuitable for explicit dedication as it is " +
                            "running instances of another domain");
                }
            }
        }
        return suitable;
    }

    private List<UserVmVO> getVmsOnHost(final long hostId) {
        final List<UserVmVO> vms = _userVmDao.listUpByHostId(hostId);
        final List<UserVmVO> vmsByLastHostId = _userVmDao.listByLastHostId(hostId);
        if (vmsByLastHostId.size() > 0) {
            // check if any VMs are within skip.counting.hours, if yes we have to consider the host.
            for (final UserVmVO stoppedVM : vmsByLastHostId) {
                final long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - stoppedVM.getUpdateTime().getTime()) / 1000;
                if (secondsSinceLastUpdate < capacityReleaseInterval) {
                    vms.add(stoppedVM);
                }
            }
        }

        return vms;
    }

    @Override
    public List<Class<?>> getCommands() {
        final List<Class<?>> cmdList = new ArrayList<>();
        cmdList.add(DedicateZoneCmd.class);
        cmdList.add(DedicatePodCmd.class);
        cmdList.add(DedicateClusterCmd.class);
        cmdList.add(DedicateHostCmd.class);
        cmdList.add(ListDedicatedZonesCmd.class);
        cmdList.add(ListDedicatedPodsCmd.class);
        cmdList.add(ListDedicatedClustersCmd.class);
        cmdList.add(ListDedicatedHostsCmd.class);
        cmdList.add(ReleaseDedicatedClusterCmd.class);
        cmdList.add(ReleaseDedicatedHostCmd.class);
        cmdList.add(ReleaseDedicatedPodCmd.class);
        cmdList.add(ReleaseDedicatedZoneCmd.class);
        return cmdList;
    }
}
