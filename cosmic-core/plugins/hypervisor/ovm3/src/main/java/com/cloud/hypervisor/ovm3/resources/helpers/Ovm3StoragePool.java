package com.cloud.hypervisor.ovm3.resources.helpers;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.ovm3.objects.CloudstackPlugin;
import com.cloud.hypervisor.ovm3.objects.Connection;
import com.cloud.hypervisor.ovm3.objects.Linux;
import com.cloud.hypervisor.ovm3.objects.Ovm3ResourceException;
import com.cloud.hypervisor.ovm3.objects.OvmObject;
import com.cloud.hypervisor.ovm3.objects.Pool;
import com.cloud.hypervisor.ovm3.objects.PoolOcfS2;
import com.cloud.hypervisor.ovm3.objects.Repository;
import com.cloud.hypervisor.ovm3.objects.StoragePlugin;
import com.cloud.hypervisor.ovm3.objects.StoragePlugin.FileProperties;
import com.cloud.hypervisor.ovm3.objects.StoragePlugin.StorageDetails;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.template.TemplateProp;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;
import com.cloud.utils.ssh.SshHelper;

import javax.naming.ConfigurationException;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3StoragePool {

    private final Logger logger = LoggerFactory.getLogger(Ovm3StoragePool.class);
    private final Connection connection;
    private final Ovm3Configuration config;
    private final OvmObject ovmObject = new OvmObject();

    public Ovm3StoragePool(final Connection conn, final Ovm3Configuration ovm3config) {
        connection = conn;
        config = ovm3config;
    }

    public boolean prepareForPool() throws ConfigurationException {
    /* need single master uuid */
        try {
            final Linux host = new Linux(connection);
            final Pool pool = new Pool(connection);

      /* setup pool and role, needs utility to be able to do things */
            if (host.getServerRoles().contentEquals(
                    pool.getValidRoles().toString())) {
                logger.info("Server role for host " + config.getAgentHostname() + " is ok");
            } else {
                setRoles(pool);
            }
            if (host.getMembershipState().contentEquals("Unowned")) {
                if (host.getOvmVersion().startsWith("3.2.")) {
                    takeOwnership(pool);
                } else if (host.getOvmVersion().startsWith("3.3.")) {
                    takeOwnership33x(pool);
                }
            } else {
                if (host.getManagerUuid().equals(config.getAgentOwnedByUuid())) {
                    final String msg = "Host " + config.getAgentHostname() + " owned by us";
                    logger.debug(msg);
                    return true;
                } else {
                    final String msg = "Host " + config.getAgentHostname() + " already part of a pool, and not owned by us";
                    logger.error(msg);
                    throw new ConfigurationException(msg);
                }
            }
        } catch (ConfigurationException | Ovm3ResourceException es) {
            final String msg = "Failed to prepare " + config.getAgentHostname() + " for pool: " + es.getMessage();
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
        return true;
    }

    private void setRoles(final Pool pool) throws ConfigurationException {
        try {
            pool.setServerRoles(pool.getValidRoles());
        } catch (final Ovm3ResourceException e) {
            final String msg = "Failed to set server role for host "
                    + config.getAgentHostname() + ": " + e.getMessage();
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    private void takeOwnership(final Pool pool) throws ConfigurationException {
        try {
            logger.debug("Take ownership of host " + config.getAgentHostname());
            pool.takeOwnership(config.getAgentOwnedByUuid(), "");
        } catch (final Ovm3ResourceException e) {
            final String msg = "Failed to take ownership of host "
                    + config.getAgentHostname();
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    /* FIXME: Placeholders for now, implement later!!!! */
    private void takeOwnership33x(final Pool pool) throws ConfigurationException {
        try {
            logger.debug("Take ownership of host " + config.getAgentHostname());
            final String event = "http://localhost:10024/event";
            final String stats = "http://localhost:10024/stats";
            final String mgrCert = "None";
            final String signCert = "None";
            pool.takeOwnership33x(config.getAgentOwnedByUuid(),
                    event,
                    stats,
                    mgrCert,
                    signCert);
        } catch (final Ovm3ResourceException e) {
            final String msg = "Failed to take ownership of host "
                    + config.getAgentHostname();
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    public Answer execute(final ModifyStoragePoolCommand cmd) {
        final StorageFilerTO pool = cmd.getPool();
        logger.debug("modifying pool " + pool);
        try {
            if (config.getAgentInOvm3Cluster()) {
                // no native ovm cluster for now, I got to break it in horrible
                // ways
            }
            if (pool.getType() == StoragePoolType.NetworkFilesystem) {
                createRepo(pool);
                final StoragePlugin store = new StoragePlugin(connection);
                final String propUuid = store.deDash(pool.getUuid());
                final String mntUuid = pool.getUuid();
                final String nfsHost = pool.getHost();
                final String nfsPath = pool.getPath();
                final StorageDetails ss = store.storagePluginGetFileSystemInfo(
                        propUuid, mntUuid, nfsHost, nfsPath);

                final Map<String, TemplateProp> tInfo = new HashMap<>();
                return new ModifyStoragePoolAnswer(cmd, Long.parseLong(ss.getSize()), Long.parseLong(ss.getFreeSize()), tInfo);
            } else if (pool.getType() == StoragePoolType.OCFS2) {
                createOcfS2sr(pool);
            }
            return new Answer(cmd, false, "The pool type: "
                    + pool.getType().name() + " is not supported.");
        } catch (final Exception e) {
            logger.debug("ModifyStoragePoolCommand failed", e);
            return new Answer(cmd, false, e.getMessage());
        }
    }

    private boolean createRepo(final StorageFilerTO cmd) throws XmlRpcException {
        final String basePath = config.getAgentOvmRepoPath();
        final Repository repo = new Repository(connection);
        final String primUuid = repo.deDash(cmd.getUuid());
        final String ovsRepo = basePath + "/" + primUuid;
    /* should add port ? */
        final String mountPoint = String.format("%1$s:%2$s", cmd.getHost(),
                cmd.getPath());
        String msg;

        if (cmd.getType() == StoragePoolType.NetworkFilesystem) {
            Boolean repoExists = false;
      /* base repo first */
            try {
                repo.mountRepoFs(mountPoint, ovsRepo);
            } catch (final Ovm3ResourceException e) {
                logger.debug("Unable to mount NFS repository " + mountPoint
                        + " on " + ovsRepo + " requested for "
                        + config.getAgentHostname() + ": " + e.getMessage());
            }
            try {
                repo.addRepo(mountPoint, ovsRepo);
                repoExists = true;
            } catch (final Ovm3ResourceException e) {
                logger.debug("NFS repository " + mountPoint + " on " + ovsRepo
                        + " not found creating repo: " + e.getMessage());
            }
            if (!repoExists) {
                try {
          /*
           * a mount of the NFS fs by the createrepo actually generates a null if it is already mounted... -sigh-
           */
                    repo.createRepo(mountPoint, ovsRepo, primUuid,
                            "OVS Repository");
                } catch (final Ovm3ResourceException e) {
                    msg = "NFS repository " + mountPoint + " on " + ovsRepo
                            + " create failed!";
                    logger.debug(msg);
                    throw new CloudRuntimeException(msg + " " + e.getMessage(),
                            e);
                }
            }

      /* add base pooling first */
            if (config.getAgentInOvm3Pool()) {
                try {
                    msg = "Configuring " + config.getAgentHostname() + "("
                            + config.getAgentIp() + ") for pool";
                    logger.debug(msg);
                    setupPool(cmd);
                    msg = "Configured host for pool";
          /* add clustering after pooling */
                    if (config.getAgentInOvm3Cluster()) {
                        msg = "Setup " + config.getAgentHostname() + "("
                                + config.getAgentIp() + ")  for cluster";
                        logger.debug(msg);
            /* setup cluster */
            /*
             * From cluster.java configure_server_for_cluster(cluster conf, fs, mount, fsuuid, poolfsbaseuuid)
             */
            /* create_cluster(poolfsuuid,) */
                    }
                } catch (final Ovm3ResourceException e) {
                    msg = "Unable to setup pool on  "
                            + config.getAgentHostname() + "("
                            + config.getAgentIp() + ") for " + ovsRepo;
                    throw new CloudRuntimeException(msg + " " + e.getMessage(),
                            e);
                }
            } else {
                msg = "no way dude I can't stand for this";
                logger.debug(msg);
            }
      /*
       * this is to create the .generic_fs_stamp else we're not allowed to create any data\disks on this thing
       */
            try {
                final URI uri = new URI(cmd.getType() + "://" + cmd.getHost() + ":"
                        + +cmd.getPort() + cmd.getPath() + "/VirtualMachines");
                setupNfsStorage(uri, cmd.getUuid());
            } catch (final Exception e) {
                msg = "NFS mount " + mountPoint + " on "
                        + config.getAgentSecStoragePath() + "/" + cmd.getUuid()
                        + " create failed!";
                throw new CloudRuntimeException(msg + " " + e.getMessage(), e);
            }
        } else {
            msg = "NFS repository " + mountPoint + " on " + ovsRepo
                    + " create failed, was type " + cmd.getType();
            logger.debug(msg);
            return false;
        }

        try {
      /* systemvm iso is imported here */
            prepareSecondaryStorageStore(ovsRepo, cmd.getUuid(), cmd.getHost());
        } catch (final Exception e) {
            msg = "systemvm.iso copy failed to " + ovsRepo;
            logger.debug(msg, e);
            return false;
        }
        return true;
    }

    private Boolean createOcfS2sr(final StorageFilerTO pool) throws XmlRpcException {
        logger.debug("OCFS2 Not implemented yet");
        return false;
    }

    private Boolean setupPool(final StorageFilerTO cmd) throws Ovm3ResourceException {
        final String primUuid = cmd.getUuid();
        final String ssUuid = ovmObject.deDash(primUuid);
        final String fsType = "nfs";
        final String clusterUuid = config.getAgentOwnedByUuid().substring(0, 15);
        final String managerId = config.getAgentOwnedByUuid();
        final String poolAlias = cmd.getHost() + ":" + cmd.getPath();
        final String mountPoint = String.format("%1$s:%2$s", cmd.getHost(),
                cmd.getPath())
                + "/VirtualMachines";
        final Integer poolSize = 0;

        final Pool poolHost = new Pool(connection);
        final PoolOcfS2 poolFs = new PoolOcfS2(connection);
        if (config.getAgentIsMaster()) {
            try {
                logger.debug("Create poolfs on " + config.getAgentHostname() + " for repo " + primUuid);
        /* double check if we're not overwritting anything here!@ */
                poolFs.createPoolFs(fsType, mountPoint, clusterUuid, primUuid,
                        ssUuid, managerId);
            } catch (final Ovm3ResourceException e) {
                throw e;
            }
            try {
                poolHost.createServerPool(poolAlias, primUuid,
                        config.getOvm3PoolVip(), poolSize + 1,
                        config.getAgentHostname(), connection.getIp());
            } catch (final Ovm3ResourceException e) {
                throw e;
            }
        } else if (config.getAgentHasMaster()) {
            try {
                poolHost.joinServerPool(poolAlias, primUuid,
                        config.getOvm3PoolVip(), poolSize + 1,
                        config.getAgentHostname(), connection.getIp());
            } catch (final Ovm3ResourceException e) {
                throw e;
            }
        }
        try {
      /* should contain check if we're in an OVM pool or not */
            final CloudstackPlugin csp = new CloudstackPlugin(connection);
            final Boolean vip = csp.dom0CheckPort(config.getOvm3PoolVip(), 22, 60, 1);
            if (!vip) {
                throw new Ovm3ResourceException(
                        "Unable to reach Ovm3 Pool VIP "
                                + config.getOvm3PoolVip());
            }
      /*
       * should also throw exception, we need to stop pool creation here, or is the manual addition fine?
       */
            if (!addMembers()) {
                return false;
            }
        } catch (final Ovm3ResourceException e) {
            throw new Ovm3ResourceException("Unable to add members to pool"
                    + e.getMessage());
        }
        return true;
    }

    private String setupNfsStorage(final URI uri, final String uuid)
            throws Ovm3ResourceException {
        final String fsUri = "nfs";
        String msg = "";
        final String mountPoint = config.getAgentSecStoragePath() + "/" + uuid;
        final Linux host = new Linux(connection);

        final Map<String, Linux.FileSystem> fsList = host.getFileSystemMap(fsUri);
        final Linux.FileSystem fs = fsList.get(uuid);
        if (fs == null || !fs.getRemoteDir().equals(mountPoint)) {
            try {
                final StoragePlugin sp = new StoragePlugin(connection);
                sp.storagePluginMountNfs(uri.getHost(), uri.getPath(), uuid,
                        mountPoint);
                msg = "Nfs storage " + uri + " mounted on " + mountPoint;
                return uuid;
            } catch (final Ovm3ResourceException ec) {
                msg = "Nfs storage " + uri + " mount on " + mountPoint
                        + " FAILED " + ec.getMessage();
                logger.error(msg);
                throw ec;
            }
        } else {
            msg = "NFS storage " + uri + " already mounted on " + mountPoint;
            return uuid;
        }
    }

    private void prepareSecondaryStorageStore(final String storageUrl,
                                              final String poolUuid, final String host) {
        final String mountPoint = storageUrl;

        final GlobalLock lock = GlobalLock.getInternLock("prepare.systemvm");
        try {
      /* double check */
            if (config.getAgentHasMaster() && config.getAgentInOvm3Pool()) {
                logger.debug("Skip systemvm iso copy, leave it to the master");
                return;
            }
            if (lock.lock(3600)) {
                try {
          /*
           * save src iso real name for reuse, so we don't depend on other happy little accidents.
           */
                    final File srcIso = getSystemVmPatchIsoFile();
                    final String destPath = mountPoint + "/ISOs/";
                    try {
                        final StoragePlugin sp = new StoragePlugin(connection);
                        final FileProperties fp = sp.storagePluginGetFileInfo(
                                poolUuid, host, destPath + "/"
                                        + srcIso.getName());
                        if (fp.getSize() != srcIso.getTotalSpace()) {
                            logger.info(" System VM patch ISO file already exists: "
                                    + srcIso.getAbsolutePath().toString()
                                    + ", destination: " + destPath);
                        }
                    } catch (final Exception e) {
                        logger.info("Copy System VM patch ISO file to secondary storage. source ISO: "
                                + srcIso.getAbsolutePath()
                                + ", destination: "
                                + destPath);
                        try {
              /* Perhaps use a key instead ? */
                            SshHelper.scpTo(connection.getIp(), 22, config.getAgentSshUserName(), null,
                                    config.getAgentSshPassword(),
                                    destPath, srcIso.getAbsolutePath().toString(), "0644");
                        } catch (final Exception es) {
                            logger.error("Unexpected exception ", es);
                            final String msg = "Unable to copy systemvm ISO on secondary storage. src location: "
                                    + srcIso.toString()
                                    + ", dest location: "
                                    + destPath;
                            logger.error(msg);
                            throw new CloudRuntimeException(msg, es);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            lock.releaseRef();
        }
    }

    private Boolean addMembers() throws Ovm3ResourceException {
        final List<String> members = new ArrayList<>();
        try {
            final Connection m = new Connection(config.getOvm3PoolVip(), connection.getPort(),
                    connection.getUserName(), connection.getPassword());
            final Pool poolMaster = new Pool(m);
            if (poolMaster.isInAPool()) {
                members.addAll(poolMaster.getPoolMemberList());
                if (!poolMaster.getPoolMemberList().contains(connection.getIp())
                        && connection.getIp().equals(config.getOvm3PoolVip())) {
                    members.add(connection.getIp());
                }
            } else {
                logger.warn(connection.getIp() + " noticed master "
                        + config.getOvm3PoolVip() + " is not part of pool");
                return false;
            }
      /* a cluster shares usernames and passwords */
            for (final String member : members) {
                final Connection x = new Connection(member, connection.getPort(),
                        connection.getUserName(), connection.getPassword());
                final Pool poolM = new Pool(x);
                if (poolM.isInAPool()) {
                    poolM.setPoolMemberList(members);
                    logger.debug("Added " + members + " to pool "
                            + poolM.getPoolId() + " on member " + member);
                } else {
                    logger.warn(member
                            + " unable to be member of a pool it's not in");
                    return false;
                }
            }
        } catch (final Exception e) {
            throw new Ovm3ResourceException("Unable to add members: "
                    + e.getMessage(), e);
        }
        return true;
    }

    public File getSystemVmPatchIsoFile() {
        final String iso = "systemvm.iso";
        final String systemVmIsoPath = Script.findScript("", "vms/" + iso);
        File isoFile = null;
        if (systemVmIsoPath != null) {
            logger.debug("found systemvm patch iso " + systemVmIsoPath);
            isoFile = new File(systemVmIsoPath);
        }
        if (isoFile == null || !isoFile.exists()) {
            final String svm = "client/target/generated-webapp/WEB-INF/classes/vms/"
                    + iso;
            logger.debug("last resort for systemvm patch iso " + svm);
            isoFile = new File(svm);
        }
        assert isoFile != null;
        if (!isoFile.exists()) {
            logger.error("Unable to locate " + iso + " in your setup at "
                    + isoFile.toString());
        }
        return isoFile;
    }

    public Answer execute(final DeleteStoragePoolCommand cmd) {
        try {
            final Pool pool = new Pool(connection);
            pool.leaveServerPool(cmd.getPool().getUuid());
      /* also connect to the master and update the pool list ? */
        } catch (final Ovm3ResourceException e) {
            logger.debug(
                    "Delete storage pool on host "
                            + config.getAgentHostname()
                            + " failed, however, we leave to user for cleanup and tell managment server it succeeded",
                    e);
        }

        return new Answer(cmd);
    }

    public GetStorageStatsAnswer execute(final GetStorageStatsCommand cmd) {
        logger.debug("Getting stats for: " + cmd.getStorageId());
        try {
            final Linux host = new Linux(connection);
            final Linux.FileSystem fs = host.getFileSystemByUuid(cmd.getStorageId(),
                    "nfs");
            final StoragePlugin store = new StoragePlugin(connection);
            final String propUuid = store.deDash(cmd.getStorageId());
            final String mntUuid = cmd.getStorageId();
            if (store == null || propUuid == null || mntUuid == null
                    || fs == null) {
                final String msg = "Null returned when retrieving stats for "
                        + cmd.getStorageId();
                logger.error(msg);
                return new GetStorageStatsAnswer(cmd, msg);
            }
      /* or is it mntUuid ish ? */
            final StorageDetails sd = store.storagePluginGetFileSystemInfo(propUuid,
                    mntUuid, fs.getHost(), fs.getDevice());
      /*
       * FIXME: cure me or kill me, this needs to trigger a reinit of primary storage, actually the problem is more
       * deeprooted, as when the hypervisor reboots it looses partial context and needs to be reinitiated.... actually a
       * full configure round... how to trigger that ?
       */
            if ("".equals(sd.getSize())) {
                final String msg = "No size when retrieving stats for "
                        + cmd.getStorageId();
                logger.debug(msg);
                return new GetStorageStatsAnswer(cmd, msg);
            }
            final long total = Long.parseLong(sd.getSize());
            final long used = total - Long.parseLong(sd.getFreeSize());
            return new GetStorageStatsAnswer(cmd, total, used);
        } catch (final Ovm3ResourceException e) {
            logger.debug("GetStorageStatsCommand for " + cmd.getStorageId() + " failed", e);
            return new GetStorageStatsAnswer(cmd, e.getMessage());
        }
    }

    public Answer execute(final CreateStoragePoolCommand cmd) {
        final StorageFilerTO pool = cmd.getPool();
        logger.debug("creating pool " + pool);
        try {
            if (pool.getType() == StoragePoolType.NetworkFilesystem) {
                createRepo(pool);
            } else if (pool.getType() == StoragePoolType.IscsiLUN) {
                return new Answer(cmd, false,
                        "iSCSI is unsupported at the moment");
        /*
         * iScsi like so: getIscsiSR(conn, pool.getUuid(), pool.getHost(), pool.getPath(), null, null, false);
         */
            } else if (pool.getType() == StoragePoolType.OCFS2) {
                return new Answer(cmd, false,
                        "OCFS2 is unsupported at the moment");
            } else if (pool.getType() == StoragePoolType.PreSetup) {
                logger.warn("pre setup for pool " + pool);
            } else {
                return new Answer(cmd, false, "The pool type: "
                        + pool.getType().name() + " is not supported.");
            }
        } catch (final Exception e) {
            final String msg = "Catch Exception " + e.getClass().getName()
                    + ", create StoragePool failed due to " + e.toString()
                    + " on host:" + config.getAgentHostname() + " pool: "
                    + pool.getHost() + pool.getPath();
            logger.warn(msg, e);
            return new Answer(cmd, false, msg);
        }
        return new Answer(cmd, true, "success");
    }

    public PrimaryStorageDownloadAnswer execute(
            final PrimaryStorageDownloadCommand cmd) {
        try {
            final Repository repo = new Repository(connection);
            final String tmplturl = cmd.getUrl();
            final String poolName = cmd.getPoolUuid();
            final String image = repo.deDash(repo.newUuid()) + ".raw";

      /* url to download from, image name, and repo to copy it to */
            repo.importVirtualDisk(tmplturl, image, poolName);
            return new PrimaryStorageDownloadAnswer(image);
        } catch (final Exception e) {
            logger.debug("PrimaryStorageDownloadCommand failed", e);
            return new PrimaryStorageDownloadAnswer(e.getMessage());
        }
    }

    public String setupSecondaryStorage(final String url)
            throws Ovm3ResourceException {
        final URI uri = URI.create(url);
        if (uri.getHost() == null) {
            throw new Ovm3ResourceException(
                    "Secondary storage host can not be empty!");
        }
        final String uuid = ovmObject.newUuid(uri.getHost() + ":" + uri.getPath());
        logger.info("Secondary storage with uuid: " + uuid);
        return setupNfsStorage(uri, uuid);
    }
}
