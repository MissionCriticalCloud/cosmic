package com.cloud.storage;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PrepareOCFS2NodesCommand;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.resource.ResourceListener;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ServerResource;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OCFS2ManagerImpl extends ManagerBase implements OCFS2Manager, ResourceListener {
    private static final Logger s_logger = LoggerFactory.getLogger(OCFS2ManagerImpl.class);

    @Inject
    ClusterDetailsDao _clusterDetailsDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    HostDao _hostDao;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    StoragePoolHostDao _poolHostDao;
    @Inject
    PrimaryDataStoreDao _poolDao;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        _resourceMgr.registerResourceEvent(ResourceListener.EVENT_DELETE_HOST_AFTER, this);
        return true;
    }

    @Override
    public boolean stop() {
        _resourceMgr.unregisterResourceEvent(this);
        return true;
    }

    @Override
    public boolean prepareNodes(final List<HostVO> hosts, final StoragePool pool) {
        if (pool.getPoolType() != StoragePoolType.OCFS2) {
            throw new CloudRuntimeException("None OCFS2 storage pool is getting into OCFS2 manager!");
        }

        return prepareNodes(getClusterName(pool.getClusterId()), hosts);
    }

    private boolean prepareNodes(final String clusterName, final List<HostVO> hosts) {
        final PrepareOCFS2NodesCommand cmd = new PrepareOCFS2NodesCommand(clusterName, marshalNodes(hosts));
        for (final HostVO h : hosts) {
            final Answer ans = _agentMgr.easySend(h.getId(), cmd);
            if (ans == null) {
                s_logger.debug("Host " + h.getId() + " is not in UP state, skip preparing OCFS2 node on it");
                continue;
            }
            if (!ans.getResult()) {
                s_logger.warn("PrepareOCFS2NodesCommand failed on host " + h.getId() + " " + ans.getDetails());
                return false;
            }
        }

        return true;
    }

    private String getClusterName(final Long clusterId) {
        final ClusterVO cluster = _clusterDao.findById(clusterId);
        if (cluster == null) {
            throw new CloudRuntimeException("Cannot get cluster for id " + clusterId);
        }

        final String clusterName = "OvmCluster" + cluster.getId();
        return clusterName;
    }

    private List<Ternary<Integer, String, String>> marshalNodes(final List<HostVO> hosts) {
        Integer i = 0;
        final List<Ternary<Integer, String, String>> lst = new ArrayList<>();
        for (final HostVO h : hosts) {
            /**
             * Don't show "node" in node name otherwise OVM's utils/config_o2cb.sh will be going crazy
             */
            final String nodeName = "ovm_" + h.getPrivateIpAddress().replace(".", "_");
            final Ternary<Integer, String, String> node = new Ternary<>(i, h.getPrivateIpAddress(), nodeName);
            lst.add(node);
            i++;
        }
        return lst;
    }

    @Override
    public boolean prepareNodes(final Long clusterId) {
        final ClusterVO cluster = _clusterDao.findById(clusterId);
        if (cluster == null) {
            throw new CloudRuntimeException("Cannot find cluster for ID " + clusterId);
        }

        final QueryBuilder<HostVO> sc = QueryBuilder.create(HostVO.class);
        sc.and(sc.entity().getClusterId(), Op.EQ, clusterId);
        sc.and(sc.entity().getPodId(), Op.EQ, cluster.getPodId());
        sc.and(sc.entity().getDataCenterId(), Op.EQ, cluster.getDataCenterId());
        sc.and(sc.entity().getType(), Op.EQ, Host.Type.Routing);
        final List<HostVO> hosts = sc.list();
        if (hosts.isEmpty()) {
            s_logger.debug("There is no host in cluster " + clusterId + ", no need to prepare OCFS2 nodes");
            return true;
        }

        return prepareNodes(getClusterName(clusterId), hosts);
    }

    @Override
    public void processDiscoverEventBefore(final Long dcid, final Long podId, final Long clusterId, final URI uri, final String username, final String password, final
    List<String> hostTags) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processDiscoverEventAfter(final Map<? extends ServerResource, Map<String, String>> resources) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processDeleteHostEventBefore(final Host host) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processDeletHostEventAfter(final Host host) {
        // TODO Auto-generated method stub
    }

    @Override
    public void processCancelMaintenaceEventBefore(final Long hostId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processCancelMaintenaceEventAfter(final Long hostId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processPrepareMaintenaceEventBefore(final Long hostId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processPrepareMaintenaceEventAfter(final Long hostId) {
        // TODO Auto-generated method stub

    }
}
