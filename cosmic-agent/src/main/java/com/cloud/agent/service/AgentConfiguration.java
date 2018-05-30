package com.cloud.agent.service;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cosmic")
public class AgentConfiguration {
    private String guid;
    private String resource;
    private Integer workers;
    private List<String> hosts;
    private Integer port;
    private String cluster;
    private String pod;
    private String zone;
    private String hostReservedMemMb;
    private String pidDir;
    private Integer pingRetries;

    private Cmds cmds;
    private Domr domr;
    private Guest guest;
    private Hypervisor hypervisor;
    private Libvirt libvirt;
    private Localstorage localstorage;
    private Network network;
    private Systemvm systemvm;
    private Termpolicy termpolicy;
    private Vm vm;

    // SystemVM
    private String controlip;
    private String controlmac;
    private String controlmask;
    private String controlnic;
    private String disable_rp_filter;
    private String dns1;
    private String dns2;
    private String gateway;
    private String instance;
    private String internaldns1;
    private String internaldns2;
    private String localgw;
    private String mgmtcidr;
    private String mgtip;
    private String mgtmac;
    private String mgtmask;
    private String mgtnic;
    private String mtu;
    private String name;
    private String proxy_vm;
    private String publicip;
    private String publicmac;
    private String publicmask;
    private String publicnic;
    private String role;
    private String sslcopy;
    private String template;
    private String type;

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
    }

    public Integer getWorkers() {
        return workers;
    }

    public void setWorkers(final Integer workers) {
        this.workers = workers;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(final List<String> hosts) {
        this.hosts = hosts;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(final String cluster) {
        this.cluster = cluster;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(final String pod) {
        this.pod = pod;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(final String zone) {
        this.zone = zone;
    }

    public String getHostReservedMemMb() {
        return hostReservedMemMb;
    }

    public void setHostReservedMemMb(final String hostReservedMemMb) {
        this.hostReservedMemMb = hostReservedMemMb;
    }

    public String getPidDir() {
        return pidDir;
    }

    public void setPidDir(final String pidDir) {
        this.pidDir = pidDir;
    }

    public Integer getPingRetries() {
        return pingRetries;
    }

    public void setPingRetries(final Integer pingRetries) {
        this.pingRetries = pingRetries;
    }

    public Cmds getCmds() {
        return cmds;
    }

    public void setCmds(final Cmds cmds) {
        this.cmds = cmds;
    }

    public Domr getDomr() {
        return domr;
    }

    public void setDomr(final Domr domr) {
        this.domr = domr;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(final Guest guest) {
        this.guest = guest;
    }

    public Hypervisor getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final Hypervisor hypervisor) {
        this.hypervisor = hypervisor;
    }

    public Libvirt getLibvirt() {
        return libvirt;
    }

    public void setLibvirt(final Libvirt libvirt) {
        this.libvirt = libvirt;
    }

    public Localstorage getLocalstorage() {
        return localstorage;
    }

    public void setLocalstorage(final Localstorage localstorage) {
        this.localstorage = localstorage;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(final Network network) {
        this.network = network;
    }

    public Systemvm getSystemvm() {
        return systemvm;
    }

    public void setSystemvm(final Systemvm systemvm) {
        this.systemvm = systemvm;
    }

    public Termpolicy getTermpolicy() {
        return termpolicy;
    }

    public void setTermpolicy(final Termpolicy termpolicy) {
        this.termpolicy = termpolicy;
    }

    public Vm getVm() {
        return vm;
    }

    public void setVm(final Vm vm) {
        this.vm = vm;
    }

    // SystemVM
    public String getControlip() {
        return controlip;
    }

    public void setControlip(final String controlip) {
        this.controlip = controlip;
    }

    public String getControlmac() {
        return controlmac;
    }

    public void setControlmac(final String controlmac) {
        this.controlmac = controlmac;
    }

    public String getControlmask() {
        return controlmask;
    }

    public void setControlmask(final String controlmask) {
        this.controlmask = controlmask;
    }

    public String getControlnic() {
        return controlnic;
    }

    public void setControlnic(final String controlnic) {
        this.controlnic = controlnic;
    }

    public String getDisable_rp_filter() {
        return disable_rp_filter;
    }

    public void setDisable_rp_filter(final String disable_rp_filter) {
        this.disable_rp_filter = disable_rp_filter;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(final String dns1) {
        this.dns1 = dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(final String dns2) {
        this.dns2 = dns2;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(final String instance) {
        this.instance = instance;
    }

    public String getInternaldns1() {
        return internaldns1;
    }

    public void setInternaldns1(final String internaldns1) {
        this.internaldns1 = internaldns1;
    }

    public String getInternaldns2() {
        return internaldns2;
    }

    public void setInternaldns2(final String internaldns2) {
        this.internaldns2 = internaldns2;
    }

    public String getLocalgw() {
        return localgw;
    }

    public void setLocalgw(final String localgw) {
        this.localgw = localgw;
    }

    public String getMgmtcidr() {
        return mgmtcidr;
    }

    public void setMgmtcidr(final String mgmtcidr) {
        this.mgmtcidr = mgmtcidr;
    }

    public String getMgtip() {
        return mgtip;
    }

    public void setMgtip(final String mgtip) {
        this.mgtip = mgtip;
    }

    public String getMgtmac() {
        return mgtmac;
    }

    public void setMgtmac(final String mgtmac) {
        this.mgtmac = mgtmac;
    }

    public String getMgtmask() {
        return mgtmask;
    }

    public void setMgtmask(final String mgtmask) {
        this.mgtmask = mgtmask;
    }

    public String getMgtnic() {
        return mgtnic;
    }

    public void setMgtnic(final String mgtnic) {
        this.mgtnic = mgtnic;
    }

    public String getMtu() {
        return mtu;
    }

    public void setMtu(final String mtu) {
        this.mtu = mtu;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProxy_vm() {
        return proxy_vm;
    }

    public void setProxy_vm(final String proxy_vm) {
        this.proxy_vm = proxy_vm;
    }

    public String getPublicip() {
        return publicip;
    }

    public void setPublicip(final String publicip) {
        this.publicip = publicip;
    }

    public String getPublicmac() {
        return publicmac;
    }

    public void setPublicmac(final String publicmac) {
        this.publicmac = publicmac;
    }

    public String getPublicmask() {
        return publicmask;
    }

    public void setPublicmask(final String publicmask) {
        this.publicmask = publicmask;
    }

    public String getPublicnic() {
        return publicnic;
    }

    public void setPublicnic(final String publicnic) {
        this.publicnic = publicnic;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getSslcopy() {
        return sslcopy;
    }

    public void setSslcopy(final String sslcopy) {
        this.sslcopy = sslcopy;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public static class Cmds {
        private Integer timeout;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(final Integer timeout) {
            this.timeout = timeout;
        }
    }

    public static class Domr {
        private Scripts scripts;

        public Scripts getScripts() {
            return scripts;
        }

        public void setScripts(final Scripts scripts) {
            this.scripts = scripts;
        }

        public static class Scripts {
            private String dir;

            public String getDir() {
                return dir;
            }

            public void setDir(final String dir) {
                this.dir = dir;
            }
        }
    }

    public static class Guest {
        private Cpu cpu;

        public Cpu getCpu() {
            return cpu;
        }

        public void setCpu(final Cpu cpu) {
            this.cpu = cpu;
        }

        public static class Cpu {
            private String mode;
            private String model;

            public String getMode() {
                return mode;
            }

            public void setMode(final String mode) {
                this.mode = mode;
            }

            public String getModel() {
                return model;
            }

            public void setModel(final String model) {
                this.model = model;
            }
        }
    }

    public static class Hypervisor {
        private String type;
        private String uri;

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(final String uri) {
            this.uri = uri;
        }
    }

    public static class Libvirt {
        private String vifDriver;

        public String getVifDriver() {
            return vifDriver;
        }

        public void setVifDriver(final String vifDriver) {
            this.vifDriver = vifDriver;
        }
    }

    public static class Localstorage {
        private String path;
        private String uuid;

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(final String uuid) {
            this.uuid = uuid;
        }
    }

    public static class Network {
        private Bridge bridge;
        private Device device;

        public Bridge getBridge() {
            return bridge;
        }

        public void setBridge(final Bridge bridge) {
            this.bridge = bridge;
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(final Device device) {
            this.device = device;
        }

        public static class Bridge {
            private String type;

            public String getType() {
                return type;
            }

            public void setType(final String type) {
                this.type = type;
            }
        }

        public static class Device {
            private String management;
            private String pub;
            private String guest;

            public String getManagement() {
                return management;
            }

            public void setManagement(final String management) {
                this.management = management;
            }

            public String getPub() {
                return pub;
            }

            public void setPub(final String pub) {
                this.pub = pub;
            }

            public String getGuest() {
                return guest;
            }

            public void setGuest(final String guest) {
                this.guest = guest;
            }
        }
    }

    public static class Systemvm {
        private String isoPath;

        public String getIsoPath() {
            return isoPath;
        }

        public void setIsoPath(final String isoPath) {
            this.isoPath = isoPath;
        }
    }

    public static class Termpolicy {
        private System system;
        private Vm vm;

        public System getSystem() {
            return system;
        }

        public void setSystem(final System system) {
            this.system = system;
        }

        public Vm getVm() {
            return vm;
        }

        public void setVm(final Vm vm) {
            this.vm = vm;
        }

        public static class System {
            private String oncrash;
            private String onpoweroff;
            private String onreboot;

            public String getOncrash() {
                return oncrash;
            }

            public void setOncrash(final String oncrash) {
                this.oncrash = oncrash;
            }

            public String getOnpoweroff() {
                return onpoweroff;
            }

            public void setOnpoweroff(final String onpoweroff) {
                this.onpoweroff = onpoweroff;
            }

            public String getOnreboot() {
                return onreboot;
            }

            public void setOnreboot(final String onreboot) {
                this.onreboot = onreboot;
            }
        }

        public static class Vm {
            private String oncrash;
            private String onpoweroff;
            private String onreboot;

            public String getOncrash() {
                return oncrash;
            }

            public void setOncrash(final String oncrash) {
                this.oncrash = oncrash;
            }

            public String getOnpoweroff() {
                return onpoweroff;
            }

            public void setOnpoweroff(final String onpoweroff) {
                this.onpoweroff = onpoweroff;
            }

            public String getOnreboot() {
                return onreboot;
            }

            public void setOnreboot(final String onreboot) {
                this.onreboot = onreboot;
            }
        }
    }

    public static class Vm {
        private Diskactivity diskactivity;
        private Memballoon memballoon;
        private Migrate migrate;

        public Diskactivity getDiskactivity() {
            return diskactivity;
        }

        public void setDiskactivity(final Diskactivity diskactivity) {
            this.diskactivity = diskactivity;
        }

        public Memballoon getMemballoon() {
            return memballoon;
        }

        public void setMemballoon(final Memballoon memballoon) {
            this.memballoon = memballoon;
        }

        public Migrate getMigrate() {
            return migrate;
        }

        public void setMigrate(final Migrate migrate) {
            this.migrate = migrate;
        }

        public static class Diskactivity {
            private Boolean checkenabled;
            private Integer checktimeout_s;
            private Integer inactivetime_ms;

            public Boolean getCheckenabled() {
                return checkenabled;
            }

            public void setCheckenabled(final Boolean checkenabled) {
                this.checkenabled = checkenabled;
            }

            public Integer getChecktimeout_s() {
                return checktimeout_s;
            }

            public void setChecktimeout_s(final Integer checktimeout_s) {
                this.checktimeout_s = checktimeout_s;
            }

            public Integer getInactivetime_ms() {
                return inactivetime_ms;
            }

            public void setInactivetime_ms(final Integer inactivetime_ms) {
                this.inactivetime_ms = inactivetime_ms;
            }
        }

        public static class Memballoon {
            private Boolean disable;

            public Boolean isDisable() {
                return disable;
            }

            public void setDisable(final Boolean disable) {
                this.disable = disable;
            }
        }

        public static class Migrate {
            private Integer downtime;
            private Integer pauseafter;
            private Integer speed;

            public Integer getDowntime() {
                return downtime;
            }

            public void setDowntime(final Integer downtime) {
                this.downtime = downtime;
            }

            public Integer getPauseafter() {
                return pauseafter;
            }

            public void setPauseafter(final Integer pauseafter) {
                this.pauseafter = pauseafter;
            }

            public Integer getSpeed() {
                return speed;
            }

            public void setSpeed(final Integer speed) {
                this.speed = speed;
            }
        }
    }
}
