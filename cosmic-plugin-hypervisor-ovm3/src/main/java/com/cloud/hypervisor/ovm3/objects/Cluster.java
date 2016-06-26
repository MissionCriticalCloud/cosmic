package com.cloud.hypervisor.ovm3.objects;

public class Cluster extends OvmObject {

    public Cluster(final Connection connection) {
        setClient(connection);
    }

    public Boolean leaveCluster(final String poolfsUuid) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("leave_cluster", poolfsUuid);
    }

    public Boolean configureServerForCluster(final String poolfsUuid) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("configure_server_for_cluster", poolfsUuid);
    }

    public Boolean deconfigureServerForCluster(final String poolfsUuid) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("deconfigure_server_for_cluster", poolfsUuid);
    }

    public Boolean joinCLuster(final String poolfsUuid) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("join_cluster", poolfsUuid);
    }

    /* TODO: Intepret existing clusters... */
    public Boolean discoverCluster() throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("discover_cluster");
    }

    public Boolean updateClusterConfiguration(final String clusterConf) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("update_clusterConfiguration", clusterConf);
    }

    public Boolean destroyCluster(final String poolfsUuid) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("destroy_cluster", poolfsUuid);
    }

    public Boolean isClusterOnline() throws Ovm3ResourceException {
        final Object x = callWrapper("is_cluster_online");
        return Boolean.valueOf(x.toString());
    }

    public Boolean createCluster(final String poolfsUuid) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("create_cluster", poolfsUuid);
    }
}
