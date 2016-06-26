package com.cloud.hypervisor.ovm3.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/*
 * should become an interface implementation
 */
public class StoragePlugin extends OvmObject {
    private static final String EMPTY_STRING = "";
    private static final String PLUGINPATH = "//Discover_Storage_Plugins_Result/storage_plugin_info_list/"
            + "storage_plugin_info";
    private static final String NFSPLUGIN = "oracle.generic.NFSPlugin.GenericNFSPlugin";
    private static final String FILESYS = "FileSys";
    private static final String STATUS = "status";
    private static final String UUID = "uuid";
    private static final String SSUUID = "ss_uuid";
    private static final String SIZE = "size";
    private static final String FREESIZE = "free_sz";
    private static final String STATE = "state";
    private static final String ACCESSGROUPNAMES = "access_grp_names";
    private static final String ACCESSPATH = "access_path";
    private static final String NAME = "name";
    private static final String MOUNTOPTIONS = "mount_options";
    private static final String ADMINUSER = "admin_user";
    private static final String ADMINHOST = "admin_host";
    private static final String TOTALSIZE = "total_sz";
    private static final String ADMINPASSWORD = "admin_passwd";
    private static final String STORAGEDESC = "storage_desc";
    private static final String ACCESSHOST = "access_host";
    private static final String STORAGETYPE = "storage_type";
    private static final String ALLOCSIZE = "alloc_sz";
    private static final String ACCESSGROUPS = "access_grps";
    private static final String USEDSIZE = "used_sz";
    private static final String FRTYPE = "fr_type";
    private static final String ONDISKSIZE = "ondisk_sz";
    private static final String FSUUID = "fs_uuid";
    private static final String FILEPATH = "file_path";
    private static final String FILESIZE = "file_sz";
    private static final Boolean ACTIVE = true;
    private final List<String> someList = new ArrayList<>(); /* empty */
    private String getPluginType = NFSPLUGIN;
    private List<String> supportedPlugins = new ArrayList<>();
    private FileProperties fileProperties = new FileProperties();
    private StorageDetails storageDetails = new StorageDetails();
    private StorageServer storageServer = new StorageServer();

    public StoragePlugin(final Connection connection) {
        setClient(connection);
    }

    public String getPluginType() {
        return getPluginType;
    }

    public Boolean setIscsi() throws Ovm3ResourceException {
        return setPluginType("SCSI");
    }

    private Boolean setPluginType(final String val) throws Ovm3ResourceException {
        for (final String plugin : discoverStoragePlugins()) {
            if (plugin.matches("(?i:.*" + val + ".*)")) {
                getPluginType = plugin;
                return true;
            }
        }
        return false;
    }

    /*
     * discover_storage_plugins, <class 'agent.api.storageplugin.StoragePlugin'>
     */
    public List<String> discoverStoragePlugins() throws Ovm3ResourceException {
        supportedPlugins = new ArrayList<>();
        final Object result = callWrapper("discover_storage_plugins");
        if (result == null) {
            return supportedPlugins;
        }
        final Document xmlDocument = prepParse((String) result);
        supportedPlugins.addAll(xmlToList(PLUGINPATH + "/@plugin_impl_name", xmlDocument));
        return supportedPlugins;
    }

    public Boolean setOcfS2() throws Ovm3ResourceException {
        return setPluginType("OCFS2");
    }

    public Boolean setNfs() throws Ovm3ResourceException {
        return setPluginType("NFS");
    }

    /*
     * now only for files storage_plugin_create, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name -
     * default: None - calls resize secretly.. after "create"
     */
    public FileProperties storagePluginCreate(final String poolUuid, final String host,
                                              final String file, final Long size, final Boolean dir) throws Ovm3ResourceException {
    /* this is correct ordering stuff and correct naming!!! */
        final String uuid = deDash(poolUuid);
        final StorageServer ss = new StorageServer();
        final StorageDetails sd = new StorageDetails();
        final FileProperties fp = new FileProperties();
        ss.setUuid(uuid);
        ss.setStorageType(FILESYS);
        ss.setAccessHost(host);
        sd.setUuid(poolUuid);
        sd.setDetailsRelationalUuid(uuid);
        sd.setState(2);
        String type = "File";
        if (dir) {
            type = "Directory";
        }
        fp.setProperties((HashMap<String, Object>) callWrapper("storage_plugin_create",
                getPluginType, ss.getDetails(),
                sd.getDetails(), file, type, size));
        return fp;
    }

    /*
     * storage_plugin_listFileSystems, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
     */
    public Boolean storagePluginListFs(final String host) throws Ovm3ResourceException {
        final StorageServer ss = new StorageServer();
        ss.setAccessHost(host);
        ss.setStorageType(FILESYS);
        ss.setDetails((Map<String, Object>) callWrapper("storage_plugin_listFileSystems",
                getPluginType, ss.getDetails()));
        return true;
    }

  /* Actions for the storage plugin */
  /*
   * storage_plugin_resizeFileSystem, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_getStatus, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None meh ?
   */

  /*
   * storage_plugin_validate, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_setQoS, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

    public final StorageDetails storagePluginMountNfs(final String nfsHost, final String nfsRemotePath,
                                                      final String mntUuid, String mountPoint) throws Ovm3ResourceException {
        final String propUuid = deDash(mntUuid);
        final StorageServer ss = new StorageServer();
        ss.setUuid(propUuid);
        ss.setName(propUuid);
        ss.setAccessHost(nfsHost);

        StorageDetails sd = new StorageDetails();
        sd.setDetailsRelationalUuid(propUuid);
        sd.setUuid(mntUuid);
        sd.setAccessPath(nfsHost + ":" + nfsRemotePath);
        if (!mountPoint.contains(mntUuid)) {
            mountPoint += "/" + mntUuid;
        }
        sd.setDetails((HashMap<String, Object>) callWrapper(
                "storage_plugin_mount", getPluginType, ss.getDetails(),
                sd.getDetails(), mountPoint, EMPTY_STRING, ACTIVE,
                someList));
    /*
     * this magically means it's already mounted.... double check
     */
        if (sd.getDetails() == null) {
            sd = storagePluginGetFileSystemInfo(propUuid,
                    mntUuid, nfsHost, nfsRemotePath);
        }
        if (EMPTY_STRING.contains(ss.getUuid())) {
            throw new Ovm3ResourceException("Unable to mount NFS FileSystem");
        }
        return sd;
    }

  /*
   * storage_plugin_createAccessGroups, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_deviceTeardown, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_startPresent, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

    /*
     * Should do some input checking of ss and base storage_plugin_getFileSystemInfo, <class
     * 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None requires a minumum of uuid,
     * access_host, storage_type ss_uuid, access_path, uuid (the ss
     */
    public StorageDetails storagePluginGetFileSystemInfo(final String propUuid,
                                                         final String mntUuid, final String nfsHost, final String nfsRemotePath) throws Ovm3ResourceException {
    /* clean the props */
        final StorageServer ss = new StorageServer();
        final StorageDetails sd = new StorageDetails();
        new FileProperties();
        ss.setUuid(propUuid);
        sd.setDetailsRelationalUuid(propUuid);
        sd.setUuid(mntUuid);
        ss.setAccessHost(nfsHost);
        if (nfsRemotePath.contains(nfsHost + ":")) {
            sd.setAccessPath(nfsRemotePath);
        } else {
            sd.setAccessPath(nfsHost + ":" + nfsRemotePath);
        }
        ss.setStorageType(FILESYS);
        sd.setDetails((HashMap<String, Object>) callWrapper(
                "storage_plugin_getFileSystemInfo", getPluginType,
                ss.getDetails(), sd.getDetails()));
        return sd;
    }

  /*
   * storage_plugin_getFileSystemCloneLimits, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name -
   * default: None
   */

  /*
   * storage_plugin_getQoSList, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_stopPresent, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_isCloneable, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

    public final Boolean storagePluginUnmountNfs(final String nfsHost, final String remotePath, final String mntUuid,
                                                 final String localPath) throws Ovm3ResourceException {
        final StorageServer ss = new StorageServer();
        final StorageDetails sd = new StorageDetails();
        sd.setUuid(mntUuid);
        sd.setDetailsRelationalUuid(deDash(mntUuid));
        ss.setUuid(deDash(mntUuid));
        ss.setAccessHost(nfsHost);
        sd.setAccessPath(nfsHost + ":" + remotePath);
        sd.setState(1);
        ss.setStorageType(FILESYS);
        final String mountPoint = localPath + "/" + mntUuid;
        callWrapper("storage_plugin_unmount", getPluginType,
                ss.getDetails(), sd.getDetails(), mountPoint, ACTIVE);
        return true;
    }

    public String checkStoragePluginAbility(final String type, final String property) throws Ovm3ResourceException {
        return checkStoragePluginBoth(type, property, true);
    }

  /*
   * storage_plugin_resize, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_deviceSizeRefresh, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */
  /*
   * storage_plugin_getStorageNames, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_splitClone, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_destroyFileSystem, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_snapRestore, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_updateSERecords, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getSnapLimits, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

    private String checkStoragePluginBoth(final String type, final String property, final Boolean ab) throws Ovm3ResourceException {
        final String val = checkStoragePluginDetails(type, ab).get(property);
        if (val == null) {
            throw new Ovm3ResourceException("StoragePlugin " + type + " has no " + property);
        }
        return val;
    }

    private Map<String, String> checkStoragePluginDetails(final String plugin, final Boolean ability) throws Ovm3ResourceException {
        final Object result = callWrapper("discover_storage_plugins");
        final Document xmlDocument = prepParse((String) result);
        if (discoverStoragePlugins().contains(plugin)) {
            final String details = PLUGINPATH + "[@plugin_impl_name='" + plugin + "']";
            if (ability) {
                return xmlToMap(details + "/abilities", xmlDocument);
            } else {
                return xmlToMap(details, xmlDocument);
            }
        } else {
            throw new Ovm3ResourceException("StoragePlugin should be one of: " + supportedPlugins);
        }
    }

    public String checkStoragePluginProperties(final String type, final String property) throws Ovm3ResourceException {
        return checkStoragePluginBoth(type, property, false);
    }

    /*
     * INFO: is used for files and dirs..., we only implement files for now... storage_plugin_destroy, <class
     * 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
     */
    public Boolean storagePluginDestroy(final String poolUuid, final String file) throws Ovm3ResourceException {
        final String uuid = deDash(poolUuid);
        final StorageServer ss = new StorageServer();
        final StorageDetails sd = new StorageDetails();
        final FileProperties fp = new FileProperties();
        ss.setUuid(uuid);
        sd.setDetailsRelationalUuid(uuid);
        sd.setUuid(poolUuid);
        fp.setType("file");
        fp.setUuid(poolUuid);
        fp.setName(file);
        return nullIsTrueCallWrapper(
                "storage_plugin_destroy", getPluginType, ss.getDetails(),
                sd.getDetails(), fp.getProperties());
    }

    /*
     * storage_plugin_getFileInfo, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
     */
    public FileProperties storagePluginGetFileInfo(final String poolUuid, final String host,
                                                   final String file) throws Ovm3ResourceException {
    /* file path is the full path */
        final String uuid = deDash(poolUuid);
        final StorageServer ss = new StorageServer();
        final StorageDetails sd = new StorageDetails();
        final FileProperties fp = new FileProperties();
        ss.setUuid(uuid);
        ss.setAccessHost(host);
        sd.setUuid(poolUuid);
        sd.setDetailsRelationalUuid(uuid);
        sd.setState(1);
        fp.setName(file);
        fp.setProperties((HashMap<String, Object>) callWrapper(
                "storage_plugin_getFileInfo",
                getPluginType,
                ss.getDetails(),
                sd.getDetails(),
                fp.getProperties()));
        if ("".equals(fp.getName())) {
            throw new Ovm3ResourceException("Unable to get file info for " + file);
        }
        return fp;
    }

  /*
   * storage_plugin_deviceResize, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getCloneLimits, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

    public StorageDetails getStorageDetails() {
        return storageDetails;
    }

  /*
   * storage_plugin_isSnapable, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getDetailsInfo, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_removeFromAccessGroup, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name -
   * default: None
   */

  /*
   * storage_plugin_renameAccessGroup, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_stop, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_createMultiSnap, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getCurrentSnaps, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

    public void setStorageDetails(final StorageDetails storageDetails) {
        this.storageDetails = storageDetails;
    }

    public StorageServer getStorageServer() {
        return storageServer;
    }

    public void setStorageServer(final StorageServer storageServer) {
        this.storageServer = storageServer;
    }

    public FileProperties getFileProperties() {
        return fileProperties;
    }

    public void setFileProperties(final FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    /* uuid has dashes here!, and ss_uuid is the relation to the storage source uuid */
    public class StorageDetails {
        private Map<String, Object> storageDetails = new HashMap<String, Object>() {
            private static final long serialVersionUID = 3L;

            {
                put(STATUS, EMPTY_STRING);
                put(UUID, EMPTY_STRING);
                put(SSUUID, EMPTY_STRING);
                put(SIZE, EMPTY_STRING);
                put(FREESIZE, 0);
                put(STATE, 0);
                put(ACCESSGROUPNAMES, new ArrayList<String>());
                put(ACCESSPATH, EMPTY_STRING);
                put(NAME, EMPTY_STRING);
                put(MOUNTOPTIONS, new ArrayList<String>());
            }
        };

        public Map<String, Object> getDetails() {
            return storageDetails;
        }

        public void setDetails(final Map<String, Object> details) {
            storageDetails = details;
        }

        public String getSize() {
            return (String) storageDetails.get(SIZE);
        }

        public void setSize(final String val) {
            storageDetails.put(SIZE, val);
        }

        public String getFreeSize() {
            return (String) storageDetails.get(FREESIZE);
        }

        public void setFreeSize(final String val) {
            storageDetails.put(FREESIZE, val);
        }

        public Integer getState() {
            return (Integer) storageDetails.get(STATE);
        }

        public void setState(final Integer val) {
            storageDetails.put(STATE, val);
        }

        public String getStatus() {
            return (String) storageDetails.get(STATUS);
        }

        public void setStatus(final String val) {
            storageDetails.put(STATUS, val);
        }

        public String getAccessPath() {
            return (String) storageDetails.get(ACCESSPATH);
        }

        /* format depends on storagesource type ? */
        public void setAccessPath(final String val) {
            storageDetails.put(ACCESSPATH, val);
        }

        public String getName() {
            return (String) storageDetails.get(NAME);
        }

        public void setName(final String val) {
            storageDetails.put(NAME, val);
        }

        public String getUuid() {
            return (String) storageDetails.get(UUID);
        }

        public void setUuid(final String val) throws Ovm3ResourceException {
            if (!val.contains("-")) {
                throw new Ovm3ResourceException("Storage Details UUID should contain dashes: " + val);
            }
            storageDetails.put(UUID, val);
        }

        public String getDetailsRelationalUuid() {
            return (String) storageDetails.get(SSUUID);
        }

        public void setDetailsRelationalUuid(final String val) throws Ovm3ResourceException {
            if (val.contains("-")) {
                throw new Ovm3ResourceException("Storage Details UUID that relates to Storage Source "
                        + "should notcontain dashes: " + val);
            }
            storageDetails.put(SSUUID, val);
        }

        public List<String> getAccessGroupNames() {
            return (List<String>) storageDetails.get(ACCESSGROUPNAMES);
        }

        public void setAccessGroupNames(final List<String> groupNames) {
            storageDetails.put(ACCESSGROUPNAMES, groupNames);
        }

        public List<String> getMountOptions() {
            return (List<String>) storageDetails.get(MOUNTOPTIONS);
        }

        public void setMountOptions(final List<String> mountOptions) {
            storageDetails.put(MOUNTOPTIONS, mountOptions);
        }
    }

    /* mind you uuid has NO dashes here */
    public class StorageServer {
        private Map<String, Object> storageSource = new HashMap<String, Object>() {
            private static final long serialVersionUID = 4L;

            {
                put(STATUS, EMPTY_STRING);
                put(ADMINUSER, EMPTY_STRING);
                put(ADMINHOST, EMPTY_STRING);
                put(UUID, EMPTY_STRING);
                put(TOTALSIZE, 0);
                put(ADMINPASSWORD, EMPTY_STRING);
                put(STORAGEDESC, EMPTY_STRING);
                put(FREESIZE, 0);
                put(ACCESSHOST, EMPTY_STRING);
                put(STORAGETYPE, EMPTY_STRING);
                put(ALLOCSIZE, 0);
                put(ACCESSGROUPS, new ArrayList<String>());
                put(USEDSIZE, 0);
                put(NAME, EMPTY_STRING);
            }
        };

        public Map<String, Object> getDetails() {
            return storageSource;
        }

        public void setDetails(final Map<String, Object> details) {
            storageSource = details;
        }

        public List<String> getAccessGroups() {
            return (List<String>) storageSource.get(ACCESSGROUPS);
        }

        public void setAccessGroups(final List<String> accessGroups) {
            storageSource.put(ACCESSGROUPS, accessGroups);
        }

        public String getStatus() {
            return (String) storageSource.get(STATUS);
        }

        public void setStatus(final String val) {
            storageSource.put(STATUS, val);
        }

        public String getAdminUser() {
            return (String) storageSource.get(ADMINUSER);
        }

        public void setAdminUser(final String val) {
            storageSource.put(ADMINUSER, val);
        }

        public String getAdminHost() {
            return (String) storageSource.get(ADMINHOST);
        }

        public void setAdminHost(final String val) {
            storageSource.put(ADMINHOST, val);
        }

        public String getUuid() {
            return (String) storageSource.get(UUID);
        }

        public void setUuid(final String val) throws Ovm3ResourceException {
            if (val.contains("-")) {
                throw new Ovm3ResourceException("Storage Source UUID should not contain dashes: " + val);
            }
            storageSource.put(UUID, val);
        }

        public String getTotalSize() {
            return (String) storageSource.get(TOTALSIZE);
        }

        public void setTotalSize(final Integer val) {
            storageSource.put(TOTALSIZE, val);
        }

        public String getAdminPassword() {
            return (String) storageSource.get("admin_password");
        }

        public void setAdminPassword(final String val) {
            storageSource.put("admin_password", val);
        }

        public String getDescription() {
            return (String) storageSource.get(STORAGEDESC);
        }

        public void setDescription(final String val) {
            storageSource.put(STORAGEDESC, val);
        }

        public String getFreeSize() {
            return (String) storageSource.get(FREESIZE);
        }

        public void setFreeSize(final Integer val) {
            storageSource.put(FREESIZE, val);
        }

        public String getAccessHost() {
            return (String) storageSource.get(ACCESSHOST);
        }

        public void setAccessHost(final String val) {
            storageSource.put(ACCESSHOST, val);
        }

        public String getStorageType() {
            return (String) storageSource.get(STORAGETYPE);
        }

        public void setStorageType(final String val) {
            storageSource.put(STORAGETYPE, val);
        }

        public Integer getAllocationSize() {
            return (Integer) storageSource.get(ALLOCSIZE);
        }

        public void setAllocationSize(final Integer val) {
            storageSource.put(ALLOCSIZE, val);
        }

        public Integer getUsedSize() {
            return (Integer) storageSource.get(USEDSIZE);
        }

        public void setUsedSize(final Integer val) {
            storageSource.put(USEDSIZE, val);
        }

        public String getName() {
            return (String) storageSource.get(NAME);
        }

        public void setName(final String val) {
            storageSource.put(NAME, val);
        }
    }

    public class FileProperties {
        private Map<String, Object> fileProperties = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1234L;

            {
                put(FRTYPE, EMPTY_STRING);
                put(ONDISKSIZE, EMPTY_STRING);
                put(FSUUID, EMPTY_STRING);
                put(FILEPATH, EMPTY_STRING);
                put(FILESIZE, EMPTY_STRING);
            }
        };

        public Map<String, Object> getProperties() {
            return fileProperties;
        }

        public void setProperties(final Map<String, Object> props) {
            fileProperties = props;
        }

        public String getName() {
            return (String) fileProperties.get(FILEPATH);
        }

        public String setName(final String name) {
            return (String) fileProperties.put(FILEPATH, name);
        }

        public String setType(final String type) {
            return (String) fileProperties.put(FRTYPE, type);
        }

        public String getType() {
            return (String) fileProperties.get(FRTYPE);
        }

        public Long getSize() {
            return Long.parseLong((String) fileProperties.get(FILESIZE));
        }

        public void setSize(final Long size) {
            fileProperties.put(FILESIZE, size);
        }

        public String setOnDiskSize(final String diskSize) {
            return (String) fileProperties.put(ONDISKSIZE, diskSize);
        }

        public String getOnDiskSize() {
            return (String) fileProperties.get(ONDISKSIZE);
        }

        public String setUuid(final String uuid) {
            return (String) fileProperties.put(FSUUID, uuid);
        }

        public String getUuid() {
            return (String) fileProperties.get(FSUUID);
        }
    }

  /*
   * storage_plugin_clone, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_list, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getInfo, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_snapRemove, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getCapabilities, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_createSnap, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getFileSystemSnapLimits, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name -
   * default: None
   */

  /*
   * storage_plugin_remove, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getCurrentClones, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_online, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_isRestorable, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_iSCSI_logoutTarget, <class 'agent.api.storageplugin.StoragePlugin'> argument: target - default: None
   * argument: portal - default: None
   */

  /*
   * storage_plugin_discover, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_start, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_removeAccessGroups, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_refresh, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_getAccessGroups, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_iSCSI_deletePortal, <class 'agent.api.storageplugin.StoragePlugin'> argument: portal - default: None
   */

  /*
   * storage_plugin_createFileSystem, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_cloneFromSnap, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_addToAccessGroup, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default:
   * None
   */

  /*
   * storage_plugin_offline, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */

  /*
   * storage_plugin_listMountPoints, <class 'agent.api.storageplugin.StoragePlugin'> argument: impl_name - default: None
   */
}
