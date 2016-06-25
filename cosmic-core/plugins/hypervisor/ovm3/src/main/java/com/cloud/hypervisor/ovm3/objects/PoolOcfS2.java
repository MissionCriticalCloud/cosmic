package com.cloud.hypervisor.ovm3.objects;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class PoolOcfS2 extends OvmObject {

    private final Logger logger = LoggerFactory.getLogger(PoolOcfS2.class);
    private Map<String, String> poolFileSystem = new HashMap<>();
    private String poolFsTarget;
    private String poolFsType;
    private String poolFsNfsBaseId;
    private String poolFsId;
    private String poolFsVersion;
    private String poolFsManagerUuid;
    private String poolPoolFsId;

    public PoolOcfS2(final Connection connection) {
        setClient(connection);
    }

    public String getPoolFsNfsBaseId() {
        return poolFsNfsBaseId;
    }

    public String getPoolFsId() {
        return poolFsId;
    }

    public String getPoolFsUuid() {
        return poolFsId;
    }

    public String getPoolFsTarget() {
        return poolFsTarget;
    }

    public String getPoolFsManagerUuid() {
        return poolFsManagerUuid;
    }

    public String getPoolFsVersion() {
        return poolFsVersion;
    }

    public String getPoolPoolFsId() {
        return poolPoolFsId;
    }

    public String getPoolFsType() {
        return poolFsType;
    }

    public Boolean destroyPoolFs(final String type, final String target, final String uuid,
                                 final String nfsbaseuuid) throws Ovm3ResourceException {
        // should throw exception if no poolIps set
        return nullIsTrueCallWrapper("destroy_pool_filesystem", type, target, uuid,
                nfsbaseuuid);
    }

    public Boolean destroyPoolFs() throws Ovm3ResourceException {
        // should throw exception if no poolIps set
        return nullIsTrueCallWrapper("destroy_pool_filesystem", poolFsType,
                poolFsTarget, poolFsId, poolFsNfsBaseId);
    }

    public Boolean createPoolFs(final String type, final String target, final String clustername,
                                final String fsid, final String nfsbaseid, final String managerid) throws Ovm3ResourceException {
        if (!hasAPoolFs()) {
            return nullIsTrueCallWrapper("create_pool_filesystem", type, target,
                    clustername, fsid, nfsbaseid, managerid, fsid);
        } else if (hasPoolFs(fsid)) {
            logger.debug("PoolFs already exists on this host: " + fsid);
            return true;
        } else {
            throw new Ovm3ResourceException("Unable to add pool filesystem to host, "
                    + "pool filesystem with other id found: " + poolFsId);
        }
    }

    public Boolean hasAPoolFs() throws Ovm3ResourceException {
        if (poolFsId == null) {
            discoverPoolFs();
        }
        if (poolFsId == null) {
            return false;
        }
        return true;
    }

    public Boolean hasPoolFs(final String id) throws Ovm3ResourceException {
        if (poolFsId == null) {
            discoverPoolFs();
        }
        if (hasAPoolFs() && poolFsId.equals(id)) {
            return true;
        }
        return false;
    }

    /* Assume a single pool can be used for a host... */
    public Boolean discoverPoolFs() throws Ovm3ResourceException {
        // should throw exception if no poolIps set
        final Object x = callWrapper("discover_pool_filesystem");
        if (x == null) {
            return false;
        }
        final Document xmlDocument = prepParse((String) x);
        final String path = "//Discover_Pool_Filesystem_Result";
        poolFileSystem = xmlToMap(path + "/Pool_Filesystem", xmlDocument);
        poolFsTarget = poolFileSystem.get("Pool_Filesystem_Target");
        poolFsType = poolFileSystem.get("Pool_Filesystem_Type");
        poolFsNfsBaseId = poolFileSystem.get("Pool_Filesystem_Nfsbase_Uuid");
        poolFsId = poolFileSystem.get("Pool_Filesystem_Uuid");
        poolPoolFsId = poolFileSystem.get("Pool_Filesystem_Pool_Uuid");
        poolFsManagerUuid = poolFileSystem.get("Pool_Filesystem_Manager_Uuid");
        poolFsVersion = poolFileSystem.get("Pool_Filesystem_Version");
        return true;
    }

    public Boolean ocfs2GetMetaData(final String device, final String filename) throws Ovm3ResourceException {
        final Object x = callWrapper("ocfs2_get_meta_data", device, filename);
        if (x == null) {
            return true;
        }
        return false;
    }
}
