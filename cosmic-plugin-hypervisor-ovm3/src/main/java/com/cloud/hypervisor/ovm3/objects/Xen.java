package com.cloud.hypervisor.ovm3.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xen extends OvmObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(Xen.class);
    private static final String VNCLISTEN = "vnclisten";
    private static final String MEMORY = "memory";
    private static final String MAXVCPUS = "maxvcpus";
    private static final String VCPUS = "vcpus";
    private static final String DOMTYPE = "OVM_domain_type";
    private static final String EXTRA = "extra";
    private Map<String, Vm> vmList = null;
    private Vm defVm = new Vm();

    public Xen(final Connection connection) {
        setClient(connection);
    }

    /*
     * delete_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
     * None argument: vm_id - default: None
     */
    public Boolean deleteVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("delete_vm", repoId, vmId);
    }

  /*
   * delete_assembly, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None
   */

  /*
   * unconfigure_template, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: template_id - default: None argument: params - default: None
   */

  /*
   * sysrq_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default: None
   * argument: vm_id - default: None argument: letter - default: None
   */

    public Boolean listVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        defVm.setVmParams((Map<String, Object>) callWrapper("list_vm", repoId,
                vmId));
        if (defVm.getVmParams() == null) {
            LOGGER.debug("no vm results on list_vm");
            return false;
        }
        return true;
    }

    public Boolean configureVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        return configureVm(repoId, vmId, defVm.getVmParams());
    }

    /*
     * configure_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
     * None argument: vm_id - default: None argument: params - default: None
     */
    private Boolean configureVm(final String repoId, final String vmId,
                                final Map<String, Object> params) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("configure_vm", repoId, vmId, params);
    }

  /*
   * delete_vm_core, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: vm_id - default: None argument: core_date - default: None
   */

    /*
     * pause_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default: None
     * argument: vm_id - default: None
     */
    public Boolean pauseVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("pause_vm", repoId, vmId);
    }

    /*
     * stop_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default: None
     * argument: vm_id - default: None argument: force - default: None
     */
    public Boolean stopVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        final Object x = callWrapper("stop_vm", repoId, vmId, false);
        if (x == null) {
            return true;
        }
        return false;
    }

  /*
   * dump_vm_core, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None argument: live - default: None argument: crash - default: None argument: reset
   * - default: None
   */

  /*
   * assembly_del_file, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: filename - default: None
   */

  /*
   * get_template_config, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: template_id - default: None
   */

  /*
   * set_assembly_config_xml, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id
   * - default: None argument: assembly_id - default: None argument: cfg - default: None
   */

  /*
   * assembly_add_file, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: url - default: None argument: filename - default:
   * None argument: option - default: None
   */

  /*
   * send_to_guest, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None argument: params - default: None
   */

  /*
   * set_assembly_config, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: cfg - default: None
   */

    public Boolean stopVm(final String repoId, final String vmId, final Boolean force)
            throws Ovm3ResourceException {
        final Object x = callWrapper("stop_vm", repoId, vmId, force);
        if (x == null) {
            return true;
        }
        return false;
    }

    /*
     * migrate_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
     * None argument: vm_id - default: None argument: dest - default: None argument: live - default: None argument: ssl -
     * default: None
     */
    public Boolean migrateVm(final String repoId, final String vmId, final String dest)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("migrate_vm", repoId, vmId, dest);
    }

  /*
   * cleanup_migration_target, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id
   * - default: None argument: vm_id - default: None
   */

    public Boolean migrateVm(final String repoId, final String vmId, final String dest,
                             final boolean live, final boolean ssl) throws Ovm3ResourceException {
        final Object x = callWrapper("migrate_vm", repoId, vmId, dest, live, ssl);
        if (x == null) {
            return true;
        }
        return false;
    }

  /*
   * setup_migration_target, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: vm_id - default: None
   */

  /*
   * deploy_assembly, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: to_deploy - default: None argument: target_repo_id -
   * default: None argument: option - default: None
   */

    /*
     * configure_vm_ha, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self default: None argument: repo_id - default:
     * None argument: vm_id - default: None argument: enable_ha - default: None
     */
    public Boolean configureVmHa(final String repoId, final String vmId, final Boolean ha)
            throws Ovm3ResourceException {
        final Object x = callWrapper("configure_vm_ha", repoId, vmId, ha);
        if (x == null) {
            return true;
        }
        return false;
    }

    /*
     * create_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
     * None argument: vm_id - default: None argument: params - default: None
     */
    public Boolean createVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("create_vm", repoId, vmId,
                defVm.getVmParams());
    }

  /*
   * set_template_config, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: template_id - default: None argument: params - default: None
   */

  /*
   * assembly_rename_file, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: filename - default: None argument: new_filename -
   * default: None
   */

    public Boolean createVm(final String repoId, final String vmId,
                            final Map<String, Object> vmParams) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("create_vm", repoId, vmId, vmParams);
    }

    /*
     * start_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default: None
     * argument: vm_id - default: None
     */
    public Boolean startVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("start_vm", repoId, vmId);
    }

    /*
     * reboot_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
     * None argument: vm_id - default: None argument: wait - default: None
     */
    public Boolean rebootVm(final String repoId, final String vmId, final int wait)
            throws Ovm3ResourceException {
        final Object x = callWrapper("reboot_vm", repoId, vmId, wait);
        if (x == null) {
            return true;
        }
        return false;
    }

    public Boolean rebootVm(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        final Object x = callWrapper("reboot_vm", repoId, vmId, 3);
        if (x == null) {
            return true;
        }
        return false;
    }

    /*
     * get_vm_config, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
     * None argument: vm_id - default: None
     */
    public Vm getVmConfig(final String vmName) throws Ovm3ResourceException {
        defVm = getRunningVmConfig(vmName);
        if (defVm == null) {
            LOGGER.debug("Unable to retrieve running config for " + vmName);
            return defVm;
        }
        return getVmConfig(defVm.getVmRootDiskPoolId(), defVm.getVmUuid());
    }

  /*
   * pack_assembly, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: assembly_id - default: None
   */

  /*
   * restore_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None argument: paused - default: None
   */

    /*
     * this should become getVmConfig later... getVmConfig returns the configuration file, while getVm returns the "live"
     * configuration. It makes perfect sense if you think about it..... ....long enough
     */
    public Vm getRunningVmConfig(final String name) throws Ovm3ResourceException {
        return getRunningVmConfigs().get(name);
    }

  /*
   * unpause_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None
   */

  /*
   * trigger_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None argument: name - default: None argument: vcpu - default: None
   */

  /*
   * set_vm_config, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None argument: params - default: None
   */

  /*
   * delete_template, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: template_id - default: None
   */

    /*
     * returns the configuration file contents, so we parse it for configuration alterations we might want to do
     * (/$repo/VirtualMachines/$uuid/vm.cfg)
     */
    public Vm getVmConfig(final String repoId, final String vmId)
            throws Ovm3ResourceException {
        try {
            final Xen.Vm nVm = new Xen.Vm();
            final Map<String, Object[]> x = (Map<String, Object[]>) callWrapper(
                    "get_vm_config", repoId, vmId);
            if (x == null) {
                LOGGER.debug("Unable to find vm with id:" + vmId + " on repoId:" + repoId);
                return nVm;
            }
            nVm.setVmVifs(Arrays.asList(Arrays.copyOf(x.get("vif"),
                    x.get("vif").length, String[].class)));
            x.remove("vif");
            nVm.setVmDisks(Arrays.asList(Arrays.copyOf(x.get("disk"),
                    x.get("disk").length, String[].class)));
            x.remove("disk");
            nVm.setVmVncs(Arrays.asList(Arrays.copyOf(x.get("vfb"),
                    x.get("vfb").length, String[].class)));
            x.remove("vfb");
            final Map<String, Object> remains = new HashMap<>();
            for (final Map.Entry<String, Object[]> not : x.entrySet()) {
                remains.put(not.getKey(), not.getValue());
            }
            nVm.setVmParams(remains);
            nVm.setPrimaryPoolUuid(repoId);
      /* to make sure stuff doesn't blow up in our face... */
            defVm = nVm;
            return nVm;
        } catch (final Ovm3ResourceException e) {
            throw e;
        }
    }

    public Map<String, Vm> getRunningVmConfigs() throws Ovm3ResourceException {
        return listVms();
    }

  /*
   * unpack_assembly, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None
   */

    /*
     * list_vms, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None
     */
    public Map<String, Vm> listVms() throws Ovm3ResourceException {
        final Object[] result = (Object[]) callWrapper("list_vms");
        if (result == null) {
            LOGGER.debug("no vm results on list_vms");
            return null;
        }

        try {
            vmList = new HashMap<>();
            for (final Object x : result) {
        /* put the vmparams in, as x is a hashmap */
                final Vm vm = new Vm();
                vm.setVmParams((Map<String, Object>) x);
                vmList.put((String) vm.get("name"), vm);
            }
        } catch (final Exception e) {
            final String msg = "Unable to list VMs: " + e.getMessage();
            throw new Ovm3ResourceException(msg, e);
        }
        return vmList;
    }

    public Vm getVmConfig() {
        return defVm;
    }

    /*
     * a vm class.... Setting up a VM is different than retrieving one from OVM. It's either a list retrieval or
     * /usr/lib64/python2.4/site-packages/agent/lib/xenvm.py
     */
    public class Vm {
        private final List<String> vmVncElement = new ArrayList<>();
        /*
         * 'disk': [
         * 'file:/OVS/Repositories/0004fb0000030000aeaca859e4a8f8c0/VirtualDisks/0004fb0000120000c444117fd87ea251.img,xvda,w
         * ']
         */
        private final List<String> vmDisks = new ArrayList<>();
        /* 'vif': [ 'mac=00:21:f6:00:00:00,bridge=c0a80100'] */
        private final ArrayList<String> vmVifs = new ArrayList<>();
        private final Integer maxVifs = 7;
        private final String[] xvmVifs = new String[maxVifs - 1];
        private final String vmSimpleName = "";
        private final String vmName = "";
        private final String vmUuid = "";
        private final String vmOnReboot = "restart";
        /* weight is relative for all VMs compared to each other */
        private final int vmCpuWeight = 27500;
        /* minimum memory allowed */
        private final int vmMemory = 256;
        private final int vmCpuCap = 0;
        /* dynam scaling for cpus */
        private final int vmMaxVcpus = 0;
        /* default to 1, can't be higher than maxvCpus */
        private final int vmVcpus = 1;
        /* high available */
        private final Boolean vmHa = false;
        private final String vmDescription = "";
        private final String vmOnPoweroff = "destroy";
        private final String vmOnCrash = "restart";
        private final String vmBootloader = "/usr/bin/pygrub";
        private final String vmBootArgs = "";
        private final String vmExtra = "";
        /* default to linux */
        private final String vmOs = "Other Linux";
        private final String vmCpuCompatGroup = "";
        /* pv is default */
        private final String vmDomainType = "xen_pvm";
        /* start counting disks at A -> 0 */
        private final int diskZero = 97;
        private Map<String, String> vmVnc = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("type", "vnc");
                put("vncunused", "1");
                put(VNCLISTEN, "127.0.0.1");
                put("keymap", "en-us");
            }
        };
        private Map<String, String> vmDisk = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("id", "");
                put("uuid", "");
                put("dev", "");
                put("bootable", "1");
                put("mode", "w");
                put("VDI", "");
                put("backend", "0");
                put("protocol", "x86_32-abi");
                put("uname", "");
            }
        };
        /*
         * the pool the vm.cfg will live on, this is the same as the primary storage pool (should be unified with disk pool
         * ?)
         */
        private String vmPrimaryPoolUuid = "";
        private int diskCount = diskZero;

        private Map<String, Object> vmParams = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("vif", vmVifs);
                put("OVM_simple_name", vmSimpleName);
                put("disk", vmDisks);
                put("bootargs", vmBootArgs);
                put("uuid", vmUuid);
                put("on_reboot", vmOnReboot);
                put("cpu_weight", vmCpuWeight);
                put(MEMORY, vmMemory);
                put("cpu_cap", vmCpuCap);
                put(MAXVCPUS, vmMaxVcpus);
                put("OVM_high_availability", vmHa);
                put("OVM_description", vmDescription);
                put("on_poweroff", vmOnPoweroff);
                put("on_crash", vmOnCrash);
                put("bootloader", vmBootloader);
                put("name", vmName);
                put("guest_os_type", vmOs);
                put("vfb", vmVncElement);
                put(VCPUS, vmVcpus);
                put("OVM_cpu_compat_group", vmCpuCompatGroup);
                put(DOMTYPE, vmDomainType);
                put(EXTRA, vmExtra);
            }
        };

        public boolean isControlDomain() {
            if ("Domain-0".equals(getVmName())) {
                return true;
            }
            return false;
        }

        public String getVmName() {
            return (String) vmParams.get("name");
        }

        public String getPrimaryPoolUuid() throws Ovm3ResourceException {
            if ("".equals(vmPrimaryPoolUuid)) {
                return getVmRootDiskPoolId();
            } else {
                return vmPrimaryPoolUuid;
            }
        }

        /* The conflict between getVm and getVmConfig becomes clear */
        public String getVmRootDiskPoolId() throws Ovm3ResourceException {
            final String poolId = getVmDiskPoolId(0);
            setPrimaryPoolUuid(poolId);
            return poolId;
        }

        private String getVmDiskPoolId(final int disk) throws Ovm3ResourceException {
            final int fi = 3;
            String diskPath = "";
            try {
                diskPath = getVmDiskDetailFromMap(disk, "uname");
            } catch (final NullPointerException e) {
                throw new Ovm3ResourceException("No valid disk found for id: "
                        + disk);
            }
            final String[] st = diskPath.split("/");
            return st[fi];
        }

        public boolean setPrimaryPoolUuid(final String poolId) {
            vmPrimaryPoolUuid = poolId;
            return true;
        }

        private String getVmDiskDetailFromMap(final int disk, final String dest) {
            final Map<String, Object[]> o = (Map<String, Object[]>) vmParams.get("device");
            if (o == null) {
                LOGGER.info("No devices found" + vmName);
                return null;
            }
            vmDisk = (Map<String, String>) o.get("vbd")[disk];
            return vmDisk.get(dest);
        }

        public Map<String, Object> getVmParams() {
            return vmParams;
        }

        public void setVmParams(final Map<String, Object> params) {
            vmParams = params;
        }

        public boolean setVmExtra(final String args) {
            vmParams.put(EXTRA, args);
            return true;
        }

        public String getVmExtra() {
            return (String) vmParams.get(EXTRA);
        }

        public String getVmBootArgs() {
            return (String) vmParams.get("bootloader_args");
        }

        public Integer getVmMaxCpus() {
            return (Integer) vmParams.get(MAXVCPUS);
        }

        public void setVmMaxCpus(final Integer val) {
            vmParams.put(MAXVCPUS, val);
        }

        public Integer getVmCpus() {
            return (Integer) vmParams.get(VCPUS);
        }

        public void setVmCpus(final Integer val) {
            if (getVmMaxCpus() == 0 || getVmMaxCpus() >= val) {
                vmParams.put(VCPUS, val);
            } else if (getVmMaxCpus() < val) {
                setVmCpus(getVmMaxCpus());
            }
        }

        public Boolean setVmMemory(final long memory) {
            vmParams.put(MEMORY, Long.toString(memory));
            return true;
        }

        public long getVmMemory() {
            return Integer.parseInt((String) vmParams.get(MEMORY));
        }

        public Boolean setVmDomainType(final String domtype) {
            vmParams.put(DOMTYPE, domtype);
            return true;
        }

        public String getVmState() {
            return (String) vmParams.get("state");
        }

        public Boolean setVmName(final String name) {
            vmParams.put("name", name);
            vmParams.put("OVM_simple_name", name);
            return true;
        }

        public Boolean setVmUuid(final String uuid) {
            vmParams.put("uuid", uuid);
            return true;
        }

        public String getVmUuid() {
            return (String) vmParams.get("uuid");
        }

        public List<String> getVmVncs() {
            return vmVncElement;
        }

        public void setVmVncs(final List<String> vncs) {
            vmVncElement.addAll(vncs);
        }

        public List<String> getVmDisks() {
            return vmDisks;
        }

        public void setVmDisks(final List<String> disks) {
            vmDisks.addAll(disks);
        }

        public Integer getVifIdByIp(final String ip) {
            Integer counter = 0;
            for (final String entry : vmVifs) {
                final String[] parts = entry.split(",");
                final String[] ippart = parts[1].split("=");
                assert ippart.length == 2 : "Invalid entry: " + entry;
                if ("mac".equals(ippart[0]) && ippart[1].equals(ip)) {
                    return counter;
                }
                counter += 1;
            }
            LOGGER.debug("No vif matched ip: " + ip + " in " + vmVifs);
            return -1;
        }

        public Boolean addVif(final Integer id, final String bridge, final String mac) {
            if (getVifIdByMac(mac) > 0) {
                LOGGER.debug("Already nic with mac present: " + mac);
                return false;
            }
            final String vif = "mac=" + mac + ",bridge=" + bridge;
            xvmVifs[id] = vif;
            return true;
        }

        public Integer getVifIdByMac(final String mac) {
            Integer counter = 0;
            for (final String entry : vmVifs) {
                final String[] parts = entry.split(",");
                final String[] macpart = parts[0].split("=");
                assert macpart.length == 2 : "Invalid entry: " + entry;
                if ("mac".equals(macpart[0]) && macpart[1].equals(mac)) {
                    return counter;
                }
                counter += 1;
            }
            LOGGER.debug("No vif matched mac: " + mac + " in " + vmVifs);
            return -1;
        }

        public boolean setupVifs() {
            for (final String vif : xvmVifs) {
                if (vif != null && !vmVifs.contains(vif)) {
                    vmVifs.add(vif);
                }
            }
            vmParams.put("vif", vmVifs);
            return true;
        }

        public Boolean removeVif(final String bridge, final String mac) {
            final List<String> newVifs = new ArrayList<>();
            try {
                final String remove = "mac=" + mac + ",bridge=" + bridge;
                for (final String vif : getVmVifs()) {
                    if (vif.equals(remove)) {
                        LOGGER.debug("leaving out vif: " + remove);
                    } else {
                        LOGGER.debug("keeping vif: " + vif);
                        newVifs.add(vif);
                    }
                }
                vmParams.put("vif", newVifs);
            } catch (final Exception e) {
                LOGGER.debug(e.toString());
            }
            return true;
        }

        public List<String> getVmVifs() {
            return vmVifs;
        }

        public void setVmVifs(final List<String> vifs) {
            vmVifs.addAll(vifs);
        }

        /*
         * 'file:/OVS/Repositories/d5f5a4480515467ca1638554f085b278/ISOs/e14c811ebbf84f0b8221e5b7404a554e.iso,hdc:cdrom,r'
         */
    /* device is coupled with vmtype enumerate and cdboot ? */
        public Boolean addRootDisk(final String image) {
            Boolean ret = false;
            if (diskCount > diskZero) {
                final Integer oVmDisk = diskCount;
                diskCount = diskZero;
                ret = addDisk(image, "w");
                diskCount = oVmDisk;
            } else {
                ret = addDisk(image, "w");
            }
            return ret;
        }

        private Boolean addDisk(final String image, String mode) {
            String devName = null;
      /* better accounting then diskCount += 1 */
            diskCount = diskZero + vmDisks.size();
            if (getVmDomainType().contains("hvm")) {
                diskCount += 2;
                devName = Character.toString((char) diskCount);
            } else {
                devName = "xvd" + Character.toString((char) diskCount);
            }

      /* check for iso, force mode and additions */
            if (image.endsWith(".iso")) {
                devName = devName + ":cdrom";
                mode = "r";
            }
            return addDiskToDisks(image, devName, mode);
        }

        /* iiiis this a good idea ? */
        public String getVmDomainType() {
            String domType = (String) vmParams.get(DOMTYPE);
            if (domType.equals(vmDomainType)) {
                final String builder = (String) vmParams.get("builder");
                if (builder == null || builder.contains("linux")) {
                    domType = "xen_pvm";
                } else {
                    domType = "hvm";
                }
            }
            return domType;
        }

        /* should be on device id too, or else we get random attaches... */
        private Boolean addDiskToDisks(final String image, final String devName, final String mode) {
            for (final String disk : vmDisks) {
                if (disk.contains(image)) {
                    LOGGER.debug(vmName + " already has disk " + image + ":" + devName + ":" + mode);
                    return true;
                }
            }
            vmDisks.add("file:" + image + "," + devName + "," + mode);
            vmParams.put("disk", vmDisks);
            return true;
        }

        public Boolean addDataDisk(final String image) {
      /*
       * w! means we're able to share the disk nice for clustered FS?
       */
            return addDisk(image, "w!");
        }

        public Boolean addIso(final String image) {
            return addDisk(image, "r!");
        }

        public Boolean removeDisk(final String image) {
            for (final String disk : vmDisks) {
                if (disk.contains(image)) {
                    vmDisks.remove(disk);
                    vmParams.put("disk", vmDisks);
                    return true;
                }
            }
            LOGGER.debug("No disk found corresponding to image: " + image);
            return false;
        }

        public Boolean setVnc(final String address, final String password) {
            setVncAddress(address);
            setVncPassword(password);
            return setVnc();
        }

        private boolean setVnc() {
            final List<String> vfb = new ArrayList<>();
            for (final String key : vmVnc.keySet()) {
                vfb.add(key + "=" + vmVnc.get(key));
            }
            vmVncElement.add(StringUtils.join(vfb, ","));
            return true;
        }

        public String getVncUsed() {
            return vmVnc.get("vncused");
        }

        public void setVncUsed(final String used) {
            vmVnc.put("vncused", used);
        }

        public String getVncPassword() {
            return vmVnc.get("vncpasswd");
        }

        public void setVncPassword(final String pass) {
            vmVnc.put("vncpasswd", pass);
        }

        public String getVncAddress() throws Ovm3ResourceException {
            final Integer port = getVncPort();
            if (port == null) {
                return null;
            }
            return vmVnc.get(VNCLISTEN);
        }

        public void setVncAddress(final String address) {
            vmVnc.put(VNCLISTEN, address);
        }

        public Integer getVncPort() throws Ovm3ResourceException {
            if (getFromVncMap("port") != null) {
                return Integer.parseInt(getFromVncMap("port"));
            }
            final String vnc = getVncLocation();
            if (vnc != null && vnc.contains(":")) {
                final String[] res = vnc.split(":");
                vmVnc.put(VNCLISTEN, res[0]);
                vmVnc.put("port", res[1]);
                return Integer.parseInt(res[1]);
            }
            throw new Ovm3ResourceException("No VNC port found");
        }

        private String getFromVncMap(final String el) {
            final Map<String, Object[]> o = (Map<String, Object[]>) vmParams.get("device");
            if (o == null) {
                return null;
            }
            vmVnc = (Map<String, String>) o.get("vfb")[0];
            if (vmVnc.containsKey(el)) {
                return vmVnc.get(el);
            } else {
                return null;
            }
        }

        public String getVncLocation() {
            return getFromVncMap("location");
        }

        private Object get(final String key) {
            return vmParams.get(key);
        }
    }

  /*
   * get_assembly_config_xml, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id
   * - default: None argument: assembly_id - default: None
   */

  /*
   * import_assembly, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: url - default: None argument: option - default: None
   */

  /*
   * create_assembly, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None argument: templates - default: None
   */

  /*
   * get_assembly_config, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: assembly_id - default: None
   */

  /*
   * unconfigure_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: vm_id - default: None argument: params - default: None
   */

  /*
   * import_template, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id -
   * default: None argument: template_id - default: None argument: url_list - default: None argument: option - default:
   * None
   */

  /*
   * import_vm, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None argument: url_list - default: None argument: option - default: None
   */

  /*
   * list_vm_core, <class 'agent.api.hypervisor.xenxm.Xen'> argument: self - default: None argument: repo_id - default:
   * None argument: vm_id - default: None
   */
}
