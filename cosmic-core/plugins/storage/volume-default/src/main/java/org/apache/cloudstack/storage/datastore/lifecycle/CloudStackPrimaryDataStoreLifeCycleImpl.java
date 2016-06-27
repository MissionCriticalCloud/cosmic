package org.apache.cloudstack.storage.datastore.lifecycle;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.alert.AlertManager;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.StorageConflictException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ManagementServer;
import com.cloud.storage.OCFS2Manager;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolAutomation;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.StoragePoolWorkDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.UriUtils;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.ConsoleProxyDao;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.SecondaryStorageVmDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.engine.subsystem.api.storage.ClusterScope;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.HostScope;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreLifeCycle;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreParameters;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.volume.datastore.PrimaryDataStoreHelper;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStackPrimaryDataStoreLifeCycleImpl implements PrimaryDataStoreLifeCycle {
    private static final Logger s_logger = LoggerFactory.getLogger(CloudStackPrimaryDataStoreLifeCycleImpl.class);
    @Inject
    protected ResourceManager _resourceMgr;
    @Inject
    protected OCFS2Manager _ocfs2Mgr;
    @Inject
    protected VirtualMachineManager vmMgr;
    @Inject
    protected SecondaryStorageVmDao _secStrgDao;
    @Inject
    protected UserDao _userDao;
    @Inject
    protected DomainRouterDao _domrDao;
    @Inject
    protected StoragePoolHostDao _storagePoolHostDao;
    @Inject
    protected AlertManager _alertMgr;
    @Inject
    protected ConsoleProxyDao _consoleProxyDao;
    @Inject
    protected StoragePoolWorkDao _storagePoolWorkDao;
    @Inject
    protected HostDao _hostDao;
    @Inject
    PrimaryDataStoreDao primaryDataStoreDao;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    AgentManager agentMgr;
    @Inject
    StorageManager storageMgr;
    @Inject
    VolumeDao volumeDao;
    @Inject
    VMInstanceDao vmDao;
    @Inject
    ManagementServer server;
    @Inject
    UserVmDao userVmDao;
    @Inject
    PrimaryDataStoreHelper dataStoreHelper;
    @Inject
    StoragePoolAutomation storagePoolAutmation;

    @Override
    public DataStore initialize(final Map<String, Object> dsInfos) {
        final Long clusterId = (Long) dsInfos.get("clusterId");
        final Long podId = (Long) dsInfos.get("podId");
        final Long zoneId = (Long) dsInfos.get("zoneId");
        final String url = (String) dsInfos.get("url");
        final String providerName = (String) dsInfos.get("providerName");
        if (clusterId != null && podId == null) {
            throw new InvalidParameterValueException("Cluster id requires pod id");
        }

        final PrimaryDataStoreParameters parameters = new PrimaryDataStoreParameters();

        URI uri = null;
        try {
            uri = new URI(UriUtils.encodeURIComponent(url));
            if (uri.getScheme() == null) {
                throw new InvalidParameterValueException("scheme is null " + url + ", add nfs:// (or cifs://) as a prefix");
            } else if (uri.getScheme().equalsIgnoreCase("nfs")) {
                final String uriHost = uri.getHost();
                final String uriPath = uri.getPath();
                if (uriHost == null || uriPath == null || uriHost.trim().isEmpty() || uriPath.trim().isEmpty()) {
                    throw new InvalidParameterValueException("host or path is null, should be nfs://hostname/path");
                }
            } else if (uri.getScheme().equalsIgnoreCase("cifs")) {
                // Don't validate against a URI encoded URI.
                final URI cifsUri = new URI(url);
                final String warnMsg = UriUtils.getCifsUriParametersProblems(cifsUri);
                if (warnMsg != null) {
                    throw new InvalidParameterValueException(warnMsg);
                }
            } else if (uri.getScheme().equalsIgnoreCase("sharedMountPoint")) {
                final String uriPath = uri.getPath();
                if (uriPath == null) {
                    throw new InvalidParameterValueException("host or path is null, should be sharedmountpoint://localhost/path");
                }
            } else if (uri.getScheme().equalsIgnoreCase("rbd")) {
                final String uriPath = uri.getPath();
                if (uriPath == null) {
                    throw new InvalidParameterValueException("host or path is null, should be rbd://hostname/pool");
                }
            } else if (uri.getScheme().equalsIgnoreCase("gluster")) {
                final String uriHost = uri.getHost();
                final String uriPath = uri.getPath();
                if (uriHost == null || uriPath == null || uriHost.trim().isEmpty() || uriPath.trim().isEmpty()) {
                    throw new InvalidParameterValueException("host or path is null, should be gluster://hostname/volume");
                }
            }
        } catch (final URISyntaxException e) {
            throw new InvalidParameterValueException(url + " is not a valid uri");
        }

        final String tags = (String) dsInfos.get("tags");
        final Map<String, String> details = (Map<String, String>) dsInfos.get("details");

        parameters.setTags(tags);
        parameters.setDetails(details);

        final String scheme = uri.getScheme();
        final String storageHost = uri.getHost();
        String hostPath = null;
        try {
            hostPath = URLDecoder.decode(uri.getPath(), "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            s_logger.error("[ignored] we are on a platform not supporting \"UTF-8\"!?!", e);
        }
        if (hostPath == null) { // if decoding fails, use getPath() anyway
            hostPath = uri.getPath();
        }
        final Object localStorage = dsInfos.get("localStorage");
        if (localStorage != null) {
            hostPath = hostPath.replaceFirst("/", "");
            hostPath = hostPath.replace("+", " ");
        }
        final String userInfo = uri.getUserInfo();
        int port = uri.getPort();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("createPool Params @ scheme - " + scheme + " storageHost - " + storageHost + " hostPath - " + hostPath + " port - " + port);
        }
        if (scheme.equalsIgnoreCase("nfs")) {
            if (port == -1) {
                port = 2049;
            }
            parameters.setType(StoragePoolType.NetworkFilesystem);
            parameters.setHost(storageHost);
            parameters.setPort(port);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("cifs")) {
            if (port == -1) {
                port = 445;
            }

            parameters.setType(StoragePoolType.SMB);
            parameters.setHost(storageHost);
            parameters.setPort(port);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("file")) {
            if (port == -1) {
                port = 0;
            }
            parameters.setType(StoragePoolType.Filesystem);
            parameters.setHost("localhost");
            parameters.setPort(0);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("sharedMountPoint")) {
            parameters.setType(StoragePoolType.SharedMountPoint);
            parameters.setHost(storageHost);
            parameters.setPort(0);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("clvm")) {
            parameters.setType(StoragePoolType.CLVM);
            parameters.setHost(storageHost);
            parameters.setPort(0);
            parameters.setPath(hostPath.replaceFirst("/", ""));
        } else if (scheme.equalsIgnoreCase("rbd")) {
            if (port == -1) {
                port = 6789;
            }
            parameters.setType(StoragePoolType.RBD);
            parameters.setHost(storageHost);
            parameters.setPort(port);
            parameters.setPath(hostPath.replaceFirst("/", ""));
            parameters.setUserInfo(userInfo);
        } else if (scheme.equalsIgnoreCase("PreSetup")) {
            parameters.setType(StoragePoolType.PreSetup);
            parameters.setHost(storageHost);
            parameters.setPort(0);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("iscsi")) {
            final String[] tokens = hostPath.split("/");
            final int lun = NumbersUtil.parseInt(tokens[tokens.length - 1], -1);
            if (port == -1) {
                port = 3260;
            }
            if (lun != -1) {
                if (clusterId == null) {
                    throw new IllegalArgumentException("IscsiLUN need to have clusters specified");
                }
                parameters.setType(StoragePoolType.IscsiLUN);
                parameters.setHost(storageHost);
                parameters.setPort(port);
                parameters.setPath(hostPath);
            } else {
                throw new IllegalArgumentException("iSCSI needs to have LUN number");
            }
        } else if (scheme.equalsIgnoreCase("iso")) {
            if (port == -1) {
                port = 2049;
            }
            parameters.setType(StoragePoolType.ISO);
            parameters.setHost(storageHost);
            parameters.setPort(port);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("ocfs2")) {
            port = 7777;
            parameters.setType(StoragePoolType.OCFS2);
            parameters.setHost("clustered");
            parameters.setPort(port);
            parameters.setPath(hostPath);
        } else if (scheme.equalsIgnoreCase("gluster")) {
            if (port == -1) {
                port = 24007;
            }
            parameters.setType(StoragePoolType.Gluster);
            parameters.setHost(storageHost);
            parameters.setPort(port);
            parameters.setPath(hostPath);
        } else {
            final StoragePoolType type = Enum.valueOf(StoragePoolType.class, scheme);

            if (type != null) {
                parameters.setType(type);
                parameters.setHost(storageHost);
                parameters.setPort(0);
                parameters.setPath(hostPath);
            } else {
                s_logger.warn("Unable to figure out the scheme for URI: " + uri);
                throw new IllegalArgumentException("Unable to figure out the scheme for URI: " + uri);
            }
        }

        if (localStorage == null) {
            final List<StoragePoolVO> pools = primaryDataStoreDao.listPoolByHostPath(storageHost, hostPath);
            if (!pools.isEmpty() && !scheme.equalsIgnoreCase("sharedmountpoint")) {
                final Long oldPodId = pools.get(0).getPodId();
                throw new CloudRuntimeException("Storage pool " + uri + " already in use by another pod (id=" + oldPodId + ")");
            }
        }

        final Object existingUuid = dsInfos.get("uuid");
        String uuid = null;

        if (existingUuid != null) {
            uuid = (String) existingUuid;
        } else if (scheme.equalsIgnoreCase("sharedmountpoint") || scheme.equalsIgnoreCase("clvm")) {
            uuid = UUID.randomUUID().toString();
        } else if (scheme.equalsIgnoreCase("PreSetup")) {
            uuid = hostPath.replace("/", "");
        } else {
            uuid = UUID.nameUUIDFromBytes((storageHost + hostPath).getBytes()).toString();
        }

        final List<StoragePoolVO> spHandles = primaryDataStoreDao.findIfDuplicatePoolsExistByUUID(uuid);
        if (spHandles != null && spHandles.size() > 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Another active pool with the same uuid already exists");
            }
            throw new CloudRuntimeException("Another active pool with the same uuid already exists");
        }

        final String poolName = (String) dsInfos.get("name");

        parameters.setUuid(uuid);
        parameters.setZoneId(zoneId);
        parameters.setPodId(podId);
        parameters.setName(poolName);
        parameters.setClusterId(clusterId);
        parameters.setProviderName(providerName);

        return dataStoreHelper.createPrimaryDataStore(parameters);
    }

    @Override
    public boolean attachCluster(final DataStore store, final ClusterScope scope) {
        final PrimaryDataStoreInfo primarystore = (PrimaryDataStoreInfo) store;
        // Check if there is host up in this cluster
        final List<HostVO> allHosts =
                _resourceMgr.listAllUpAndEnabledHosts(Host.Type.Routing, primarystore.getClusterId(), primarystore.getPodId(), primarystore.getDataCenterId());
        if (allHosts.isEmpty()) {
            primaryDataStoreDao.expunge(primarystore.getId());
            throw new CloudRuntimeException("No host up to associate a storage pool with in cluster " + primarystore.getClusterId());
        }

        if (primarystore.getPoolType() == StoragePoolType.OCFS2 && !_ocfs2Mgr.prepareNodes(allHosts, primarystore)) {
            s_logger.warn("Can not create storage pool " + primarystore + " on cluster " + primarystore.getClusterId());
            primaryDataStoreDao.expunge(primarystore.getId());
            return false;
        }

        boolean success = false;
        for (final HostVO h : allHosts) {
            success = createStoragePool(h.getId(), primarystore);
            if (success) {
                break;
            }
        }

        s_logger.debug("In createPool Adding the pool to each of the hosts");
        final List<HostVO> poolHosts = new ArrayList<>();
        for (final HostVO h : allHosts) {
            try {
                storageMgr.connectHostToSharedPool(h.getId(), primarystore.getId());
                poolHosts.add(h);
            } catch (final StorageConflictException se) {
                primaryDataStoreDao.expunge(primarystore.getId());
                throw new CloudRuntimeException("Storage has already been added as local storage");
            } catch (final Exception e) {
                s_logger.warn("Unable to establish a connection between " + h + " and " + primarystore, e);
            }
        }

        if (poolHosts.isEmpty()) {
            s_logger.warn("No host can access storage pool " + primarystore + " on cluster " + primarystore.getClusterId());
            primaryDataStoreDao.expunge(primarystore.getId());
            throw new CloudRuntimeException("Failed to access storage pool");
        }

        dataStoreHelper.attachCluster(store);
        return true;
    }

    protected boolean createStoragePool(final long hostId, final StoragePool pool) {
        s_logger.debug("creating pool " + pool.getName() + " on  host " + hostId);

        if (pool.getPoolType() != StoragePoolType.NetworkFilesystem && pool.getPoolType() != StoragePoolType.Filesystem &&
                pool.getPoolType() != StoragePoolType.IscsiLUN && pool.getPoolType() != StoragePoolType.Iscsi &&
                pool.getPoolType() != StoragePoolType.SharedMountPoint && pool.getPoolType() != StoragePoolType.PreSetup && pool.getPoolType() != StoragePoolType.OCFS2 &&
                pool.getPoolType() != StoragePoolType.RBD && pool.getPoolType() != StoragePoolType.CLVM && pool.getPoolType() != StoragePoolType.SMB &&
                pool.getPoolType() != StoragePoolType.Gluster) {
            s_logger.warn(" Doesn't support storage pool type " + pool.getPoolType());
            return false;
        }
        final CreateStoragePoolCommand cmd = new CreateStoragePoolCommand(true, pool);
        final Answer answer = agentMgr.easySend(hostId, cmd);
        if (answer != null && answer.getResult()) {
            return true;
        } else {
            primaryDataStoreDao.expunge(pool.getId());
            String msg = "";
            if (answer != null) {
                msg = "Can not create storage pool through host " + hostId + " due to " + answer.getDetails();
                s_logger.warn(msg);
            } else {
                msg = "Can not create storage pool through host " + hostId + " due to CreateStoragePoolCommand returns null";
                s_logger.warn(msg);
            }
            throw new CloudRuntimeException(msg);
        }
    }

    @Override
    public boolean attachHost(final DataStore store, final HostScope scope, final StoragePoolInfo existingInfo) {
        dataStoreHelper.attachHost(store, scope, existingInfo);
        return true;
    }

    @Override
    public boolean attachZone(final DataStore dataStore, final ZoneScope scope, final HypervisorType hypervisorType) {
        final List<HostVO> hosts = _resourceMgr.listAllUpAndEnabledHostsInOneZoneByHypervisor(hypervisorType, scope.getScopeId());
        s_logger.debug("In createPool. Attaching the pool to each of the hosts.");
        final List<HostVO> poolHosts = new ArrayList<>();
        for (final HostVO host : hosts) {
            try {
                storageMgr.connectHostToSharedPool(host.getId(), dataStore.getId());
                poolHosts.add(host);
            } catch (final StorageConflictException se) {
                primaryDataStoreDao.expunge(dataStore.getId());
                throw new CloudRuntimeException("Storage has already been added as local storage to host: " + host.getName());
            } catch (final Exception e) {
                s_logger.warn("Unable to establish a connection between " + host + " and " + dataStore, e);
            }
        }
        if (poolHosts.isEmpty()) {
            s_logger.warn("No host can access storage pool " + dataStore + " in this zone.");
            primaryDataStoreDao.expunge(dataStore.getId());
            throw new CloudRuntimeException("Failed to create storage pool as it is not accessible to hosts.");
        }
        dataStoreHelper.attachZone(dataStore, hypervisorType);
        return true;
    }

    @Override
    public boolean maintain(final DataStore dataStore) {
        storagePoolAutmation.maintain(dataStore);
        dataStoreHelper.maintain(dataStore);
        return true;
    }

    @Override
    public boolean cancelMaintain(final DataStore store) {
        dataStoreHelper.cancelMaintain(store);
        storagePoolAutmation.cancelMaintain(store);
        return true;
    }

    @DB
    @Override
    public boolean deleteDataStore(final DataStore store) {
        final List<StoragePoolHostVO> hostPoolRecords = _storagePoolHostDao.listByPoolId(store.getId());
        final StoragePool pool = (StoragePool) store;
        boolean deleteFlag = false;
        // find the hypervisor where the storage is attached to.
        HypervisorType hType = null;
        if (hostPoolRecords.size() > 0) {
            hType = getHypervisorType(hostPoolRecords.get(0).getHostId());
        }

        // Remove the SR associated with the Xenserver
        for (final StoragePoolHostVO host : hostPoolRecords) {
            final DeleteStoragePoolCommand deleteCmd = new DeleteStoragePoolCommand(pool);
            final Answer answer = agentMgr.easySend(host.getHostId(), deleteCmd);

            if (answer != null && answer.getResult()) {
                deleteFlag = true;
                // if host is KVM hypervisor then send deleteStoragepoolcmd to all the kvm hosts.
                if (HypervisorType.KVM != hType) {
                    break;
                }
            } else {
                if (answer != null) {
                    s_logger.debug("Failed to delete storage pool: " + answer.getResult());
                }
            }
        }

        if (!hostPoolRecords.isEmpty() && !deleteFlag) {
            throw new CloudRuntimeException("Failed to delete storage pool on host");
        }

        return dataStoreHelper.deletePrimaryDataStore(store);
    }

    private HypervisorType getHypervisorType(final long hostId) {
        final HostVO host = _hostDao.findById(hostId);
        if (host != null) {
            return host.getHypervisorType();
        }
        return HypervisorType.None;
    }

    /* (non-Javadoc)
     * @see org.apache.cloudstack.engine.subsystem.api.storage.DataStoreLifeCycle#migrateToObjectStore(org.apache.cloudstack.engine.subsystem.api.storage.DataStore)
     */
    @Override
    public boolean migrateToObjectStore(final DataStore store) {
        return false;
    }

    @Override
    public void updateStoragePool(final StoragePool storagePool, final Map<String, String> details) {
    }

    @Override
    public void enableStoragePool(final DataStore dataStore) {
        dataStoreHelper.enable(dataStore);
    }

    @Override
    public void disableStoragePool(final DataStore dataStore) {
        dataStoreHelper.disable(dataStore);
    }
}
