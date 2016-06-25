package com.cloud.hypervisor.ovm3.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

public class Repository extends OvmObject {

    private static final String VERSION = "Version";
    private static final String NAMETAG = "[@Name='";
    private final Map<String, RepoDbDetails> repoDbs = new HashMap<>();
    private final Map<String, RepoDetails> repos = new HashMap<>();
    private Object postDiscovery = null;
    private Object postDbDiscovery = null;
    private List<String> repoDbList = new ArrayList<>();
    private List<String> repoList = new ArrayList<>();

    public Repository(final Connection connection) {
        setClient(connection);
    }

    public RepoDbDetails getRepoDb(final String id) throws Ovm3ResourceException {
        if (repoDbs.containsKey(id)) {
            return repoDbs.get(id);
        }
        return null;
    }

    public List<String> getRepoDbList() throws Ovm3ResourceException {
        return repoDbList;
    }

    public RepoDetails getRepo(final String id) throws Ovm3ResourceException {
        if (repos.containsKey(id)) {
            return repos.get(id);
        }
        return null;
    }

    public List<String> getRepoList() throws Ovm3ResourceException {
        return repoList;
    }

    /*
     * delete_repository, <class 'agent.api.repository.Repository'> argument: repo_uuid - default: None argument: erase -
     * default: None
     */
    public Boolean deleteRepo(final String id, final Boolean erase)
            throws Ovm3ResourceException {
        final Object res = callWrapper("delete_repository", id, erase);
        if (res == null) {
            return true;
        }
        return false;
    }

    /*
     * import_virtual_disk, <class 'agent.api.repository.Repository'> argument: url - default: None argument:
     * virtual_disk_id - default: None argument: repo_uuid - default: None argument: option - default: None
     */
  /* should add timeout ? */
    public Boolean importVirtualDisk(final String url, final String vdiskid, final String repoid,
                                     final String option) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("import_virtual_disk", url, vdiskid,
                repoid, option);
    }

    public Boolean importVirtualDisk(final String url, final String vdiskid, final String repoid)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("import_virtual_disk", url, vdiskid,
                repoid);
    }

    /*
     * discover_repositories, <class 'agent.api.repository.Repository'> argument: args - default: None
     */
  /*
   * args are repo ids <Discover_Repositories_Result> <RepositoryList/> </Discover_Repositories_Result>
   */
    public Boolean discoverRepo(final String id) throws Ovm3ResourceException {
        postDiscovery = callWrapper("discover_repositories", id);
        if (postDiscovery == null) {
            return false;
        }
        final Document xmlDocument = prepParse((String) postDiscovery);
        final String path = "//Discover_Repositories_Result/RepositoryList/Repository";
        repoList = new ArrayList<>();
        repoList.addAll(xmlToList(path + "/@Name", xmlDocument));
        for (final String name : repoList) {
            final RepoDetails repo = new RepoDetails();
            repo.setRepoTemplates(xmlToList(path + NAMETAG + id
                    + "']/Templates/Template/File", xmlDocument));
            repo.setRepoVirtualMachines(xmlToList(path + NAMETAG + id
                    + "']/VirtualMachines/VirtualMachine/@Name", xmlDocument));
            repo.setRepoVirtualDisks(xmlToList(path + NAMETAG + name
                    + "']/VirtualDisks/Disk", xmlDocument));
            repo.setRepoIsos(xmlToList(
                    path + NAMETAG + name + "']/ISOs/ISO", xmlDocument));
            final Map<String, String> details = xmlToMap(path + NAMETAG + name
                    + "']", xmlDocument);
            repo.setRepoDetails(details);
            repos.put(name, repo);
        }
        return true;
    }

    /*
     * add_repository, <class 'agent.api.repository.Repository'> argument: fs_location - default: None argument:
     * mount_point - default: None
     */
    public Boolean addRepo(final String remote, final String local)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("add_repository", remote, local);
    }

    /*
     * mount_repository_fs, <class 'agent.api.repository.Repository'> argument: fs_location - default: None argument:
     * mount_point - default: None
     */
    public Boolean mountRepoFs(final String remote, final String local)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("mount_repository_fs", remote, local);
    }

    /*
     * unmount_repository_fs, <class 'agent.api.repository.Repository'> argument: mount_point - default: None
     */
    public Boolean unmountRepoFs(final String local) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("unmount_repository_fs", local);
    }

    /*
     * create_repository, <class 'agent.api.repository.Repository'> argument: fs_location - default: None argument:
     * mount_point - default: None argument: repo_uuid - default: None argument: repo_alias - default: None
     */
    public Boolean createRepo(final String remote, final String local, final String repoid,
                              final String repoalias) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("create_repository", remote, local,
                repoid, repoalias);
    }

    /*
     * discover_repository_db, <class 'agent.api.repository.Repository'> <Discover_Repository_Db_Result>
     * <RepositoryDbList> <Repository Uuid="0004fb0000030000aeaca859e4a8f8c0">
     * <Fs_location>cs-mgmt:/volumes/cs-data/primary</Fs_location> <Mount_point>/
     * OVS/Repositories/0004fb0000030000aeaca859e4a8f8c0</Mount_point> <Filesystem_type>nfs</Filesystem_type>
     * <Version>3.0</Version> <Alias>MyRepo</Alias> <Manager_uuid>0004fb00000100000af70d20dcce7d65</Manager_uuid>
     * <Status>Unmounted</Status> </Repository> <Repository> ... </Repository> </RepositoryDbList>
     * </Discover_Repository_Db_Result>
     */
    public Boolean discoverRepoDb() throws Ovm3ResourceException {
        postDbDiscovery = callWrapper("discover_repository_db");
        final Document xmlDocument = prepParse((String) postDbDiscovery);
        final String path = "//Discover_Repository_Db_Result/RepositoryDbList/Repository";
        repoDbList = new ArrayList<>();
        repoDbList.addAll(xmlToList(path + "/@Uuid", xmlDocument));
        for (final String id : repoDbList) {
            final RepoDbDetails repoDb = new RepoDbDetails();
            final Map<String, String> rep = xmlToMap(path + "[@Uuid='" + id + "']",
                    xmlDocument);
            repoDb.setRepoDbDetails(rep);
            repoDb.setUuid(id);
            repoDbs.put(id, repoDb);
        }
        return true;
    }

    public static class RepoDbDetails {
        private final Map<String, String> dbEntry = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("Uuid", null);
                put("Fs_location", null);
                put("Mount_point", null);
                put("Filesystem_type", null);
                put(VERSION, null);
                put("Alias", null);
                put("Manager_uuid", null);
                put("Status", null);
            }
        };

        public RepoDbDetails() {
        }

        public void setRepoDbDetails(final Map<String, String> det) {
            dbEntry.putAll(det);
        }

        public String getStatus() {
            return dbEntry.get("Status");
        }

        public String getManagerUuid() {
            return dbEntry.get("Manager_uuid");
        }

        public String getAlias() {
            return dbEntry.get("Alias");
        }

        public String getVersion() {
            return dbEntry.get(VERSION);
        }

        public String getFilesystemType() {
            return dbEntry.get("Filesystem_type");
        }

        public String getMountPoint() {
            return dbEntry.get("Mount_point");
        }

        public String getFsLocation() {
            return dbEntry.get("Fs_location");
        }

        public String getUuid() {
            return dbEntry.get("Uuid");
        }

        public void setUuid(final String id) {
            dbEntry.put("Uuid", id);
        }
    }

    public static class RepoDetails {
        private final List<String> templates = new ArrayList<>();
        private final List<String> virtualMachines = new ArrayList<>();
        private final List<String> virtualDisks = new ArrayList<>();
        private final List<String> isos = new ArrayList<>();
        private final Map<String, String> dbEntry = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("Repository_UUID", null);
                put(VERSION, null);
                put("Repository_Alias", null);
                put("Manager_UUID", null);
            }
        };

        public RepoDetails() {
        }

        public String getManagerUuid() {
            return dbEntry.get("Manager_UUID");
        }

        public String getAlias() {
            return dbEntry.get("Repository_Alias");
        }

        public String getVersion() {
            return dbEntry.get(VERSION);
        }

        public String getUuid() {
            return dbEntry.get("Repository_UUID");
        }

        public void setRepoDetails(final Map<String, String> det) {
            dbEntry.putAll(det);
        }

        public List<String> getRepoTemplates() {
            return templates;
        }

        public void setRepoTemplates(final List<String> temp) {
            templates.addAll(temp);
        }

        public List<String> getRepoVirtualMachines() {
            return virtualMachines;
        }

        public void setRepoVirtualMachines(final List<String> vms) {
            virtualMachines.addAll(vms);
        }

        public List<String> getRepoVirtualDisks() {
            return virtualDisks;
        }

        public void setRepoVirtualDisks(final List<String> disks) {
            virtualDisks.addAll(disks);
        }

        public List<String> getRepoIsos() {
            return isos;
        }

        public void setRepoIsos(final List<String> isolist) {
            isos.addAll(isolist);
        }
    }
}
