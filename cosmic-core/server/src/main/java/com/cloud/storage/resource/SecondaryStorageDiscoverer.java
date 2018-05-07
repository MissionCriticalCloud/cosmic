package com.cloud.storage.resource;

import com.cloud.agent.AgentManager;
import com.cloud.common.resource.ServerResource;
import com.cloud.host.HostVO;
import com.cloud.model.enumeration.Event;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SecondaryStorageDiscoverer is used to discover secondary
 * storage servers and make sure everything it can do is
 * correct.
 */
public class SecondaryStorageDiscoverer extends DiscovererBase implements Discoverer {
    private static final Logger s_logger = LoggerFactory.getLogger(SecondaryStorageDiscoverer.class);
    @Inject
    protected VMTemplateDao _tmpltDao = null;
    @Inject
    protected VMTemplateZoneDao _vmTemplateZoneDao = null;
    @Inject
    protected VMTemplateDao _vmTemplateDao = null;
    @Inject
    protected AgentManager _agentMgr = null;
    long _timeout = 2 * 60 * 1000; // 2 minutes
    String _mountParent;
    boolean _useServiceVM = false;
    Random _random = new Random(System.currentTimeMillis());

    protected SecondaryStorageDiscoverer() {
    }

    @Override
    public Map<? extends ServerResource, Map<String, String>> find(final long dcId, final Long podId, final Long clusterId, final URI uri, final String username, final String password, final
    List<String> hostTags) {
        if (!uri.getScheme().equalsIgnoreCase("nfs") && !uri.getScheme().equalsIgnoreCase("cifs") && !uri.getScheme().equalsIgnoreCase("iso") && !uri.getScheme().equalsIgnoreCase("dummy")) {
            s_logger.debug("It's not NFS or ISO, so not a secondary storage server: " + uri.toString());
            return null;
        }

        if (uri.getScheme().equalsIgnoreCase("nfs") || uri.getScheme().equalsIgnoreCase("cifs") || uri.getScheme().equalsIgnoreCase("iso")) {
            return createNfsSecondaryStorageResource(dcId, podId, uri);
        } else if (uri.getScheme().equalsIgnoreCase("dummy")) {
            return createDummySecondaryStorageResource(dcId, podId, uri);
        } else {
            return null;
        }
    }

    protected Map<? extends ServerResource, Map<String, String>> createNfsSecondaryStorageResource(final long dcId, final Long podId, final URI uri) {
        // We always use the system vm, so create a dummy vm in the management server
        return createDummySecondaryStorageResource(dcId, podId, uri);
    }

    protected Map<ServerResource, Map<String, String>> createDummySecondaryStorageResource(final long dcId, final Long podId, final URI uri) {
        final Map<ServerResource, Map<String, String>> srs = new HashMap<>();

        DummySecondaryStorageResource storage = new DummySecondaryStorageResource(this._useServiceVM);
        storage = ComponentContext.inject(storage);

        final Map<String, String> details = new HashMap<>();

        details.put("mount.path", uri.toString());
        details.put("orig.url", uri.toString());

        final Map<String, Object> params = new HashMap<>();
        params.putAll(details);
        params.put("zone", Long.toString(dcId));
        if (podId != null) {
            params.put("pod", podId.toString());
        }
        params.put("guid", uri.toString());

        try {
            storage.configure("Storage", params);
        } catch (final ConfigurationException e) {
            s_logger.warn("Unable to configure the storage ", e);
            return null;
        }
        srs.put(storage, details);

        return srs;
    }

    @Override
    public void postDiscovery(final List<HostVO> hosts, final long msId) {
        if (this._useServiceVM) {
            for (final HostVO h : hosts) {
                this._agentMgr.agentStatusTransitTo(h, Event.AgentDisconnected, msId);
            }
        }
        for (final HostVO h : hosts) {
            associateTemplatesToZone(h.getId(), h.getDataCenterId());
        }
    }

    @Override
    public boolean matchHypervisor(final String hypervisor) {
        if (hypervisor.equals("SecondaryStorage")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.None;
    }

    private void associateTemplatesToZone(final long hostId, final long dcId) {
        VMTemplateZoneVO tmpltZone;

        final List<VMTemplateVO> allTemplates = this._vmTemplateDao.listAll();
        for (final VMTemplateVO vt : allTemplates) {
            if (vt.isCrossZones()) {
                tmpltZone = this._vmTemplateZoneDao.findByZoneTemplate(dcId, vt.getId());
                if (tmpltZone == null) {
                    final VMTemplateZoneVO vmTemplateZone = new VMTemplateZoneVO(dcId, vt.getId(), new Date());
                    this._vmTemplateZoneDao.persist(vmTemplateZone);
                }
            }
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        this._mountParent = this._params.get("mount.parent");
        if (this._mountParent == null) {
            this._mountParent = "/mnt";
        }

        final String useServiceVM = this._params.get("secondary.storage.vm");
        if ("true".equalsIgnoreCase(useServiceVM)) {
            this._useServiceVM = true;
        }
        return true;
    }
}
