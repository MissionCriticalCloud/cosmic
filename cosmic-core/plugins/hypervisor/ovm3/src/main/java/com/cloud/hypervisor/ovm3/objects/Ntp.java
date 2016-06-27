package com.cloud.hypervisor.ovm3.objects;

import java.util.ArrayList;
import java.util.List;

public class Ntp extends OvmObject {
    private List<String> ntpHosts = new ArrayList<>();
    private Boolean isServer = null;
    private Boolean isRunning = null;

    public Ntp(final Connection connection) {
        setClient(connection);
    }

    public List<String> removeServer(final String server) {
        if (ntpHosts.contains(server)) {
            ntpHosts.remove(server);
        }
        return ntpHosts;
    }

    public List<String> getServers() {
        return ntpHosts;
    }

    public void setServers(final List<String> servers) {
        ntpHosts = servers;
    }

    public Boolean isRunning() {
        return isRunning;
    }

    public Boolean isServer() {
        return isServer;
    }

    public Boolean getDetails() throws Ovm3ResourceException {
        return getNtp();
    }

    /*
     * get_ntp, <class 'agent.api.host.linux.Linux'> argument: self - default: None
     */
    public Boolean getNtp() throws Ovm3ResourceException {
        final Object[] v = (Object[]) callWrapper("get_ntp");
        int counter = 0;
        for (final Object o : v) {
            if (o instanceof java.lang.Boolean) {
                if (counter == 0) {
                    isServer = (Boolean) o;
                }
                if (counter == 1) {
                    isRunning = (Boolean) o;
                }
                counter += 1;
            } else if (o instanceof java.lang.Object) {
                final Object[] s = (Object[]) o;
                for (final Object m : s) {
                    addServer((String) m);
                }
            }
        }
        return true;
    }

    public List<String> addServer(final String server) {
        if (!ntpHosts.contains(server)) {
            ntpHosts.add(server);
        }
        return ntpHosts;
    }

    /* also cleans the vector */
    public Boolean setNtp(final String server, final Boolean running)
            throws Ovm3ResourceException {
        ntpHosts = new ArrayList<>();
        ntpHosts.add(server);
        return setNtp(ntpHosts, running);
    }

    public Boolean setNtp(final List<String> ntpHosts, final Boolean running)
            throws Ovm3ResourceException {
        if (ntpHosts.isEmpty()) {
            return false;
        }
        return nullIsTrueCallWrapper("set_ntp", ntpHosts, running);
    }

    public Boolean setNtp(final Boolean running) throws Ovm3ResourceException {
        return setNtp(ntpHosts, running);
    }

    public Boolean disableNtp() throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("disable_ntp");
    }

    public Boolean enableNtp() throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("enable_ntp");
    }
}
