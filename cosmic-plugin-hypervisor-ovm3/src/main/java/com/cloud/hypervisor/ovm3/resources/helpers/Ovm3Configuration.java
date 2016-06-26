package com.cloud.hypervisor.ovm3.resources.helpers;

import com.cloud.hypervisor.ovm3.objects.Network;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.net.NetUtils;

import javax.naming.ConfigurationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* holds config data for the Ovm3 Hypervisor */
public class Ovm3Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ovm3Configuration.class);
    private final String virtualdiskdir = "VirtualDisks";
    private final String templatedir = "Templates";
    private final Map<String, Object> rawParams = new HashMap<>();
    private String agentIp;
    private Long agentZoneId;
    private Long agentPodId;
    private String agentPoolId;
    private Long agentClusterId;
    private String agentHostname;
    private String csHostGuid;
    private String agentSshUserName = "root";
    private String agentSshPassword;
    private String agentOvsAgentUser = "oracle";
    private String agentOvsAgentPassword;
    private Integer agentOvsAgentPort = 8899;
    private Boolean agentOvsAgentSsl = false;
    private String agentSshKeyFile = "id_rsa.cloud";
    private String agentOwnedByUuid = "d1a749d4295041fb99854f52ea4dea97";
    private Boolean agentIsMaster = false;
    private Boolean agentHasMaster = false;
    private Boolean agentInOvm3Pool = false;
    private Boolean agentInOvm3Cluster = false;
    private String ovm3PoolVip = "";
    private String agentPrivateNetworkName;
    private String agentPublicNetworkName;
    private String agentGuestNetworkName;
    private String agentStorageNetworkName;
    private String agentControlNetworkName = "control0";
    private String agentOvmRepoPath = "/OVS/Repositories";
    private String agentSecStoragePath = "/nfsmnt";
    private String agentScript = "cloudstack.py";
    private String agentCheckStorageScript = "storagehealth.py";
    private Integer agentStorageCheckTimeout = 120;
    private Integer agentStorageCheckInterval = 1;
    private List<String> agentScripts = Arrays.asList(agentCheckStorageScript, agentScript);
    private String agentScriptsDir = "/opt/cloudstack/bin";
    private int domRSshPort = 3922;
    private String domRCloudPath = "/opt/cloud/bin/";
    private Map<String, Network.Interface> agentInterfaces = null;
    private Boolean istest = false;

    public Ovm3Configuration(final Map<String, Object> params)
            throws ConfigurationException {
        setAgentZoneId(Long.parseLong((String) params.get("zone")));
        setAgentPodId(Long.parseLong(validateParam("PodId",
                (String) params.get("pod"))));
        setAgentClusterId(Long.parseLong((String) params.get("cluster")));
        setOvm3PoolVip(String.valueOf(params.get("ovm3vip")));
        setAgentInOvm3Pool(BooleanUtils.toBoolean((String) params.get("ovm3pool")));
        setAgentInOvm3Cluster(BooleanUtils.toBoolean((String) params.get("ovm3cluster")));
        setAgentHostname(validateParam("Hostname", (String) params.get("host")));
        setAgentIp((String) params.get("ip"));
        if (params.get("agentport") != null) {
            setAgentOvsAgentPort(Integer.parseInt((String) params.get("agentport")));
        }
        setAgentSshUserName(validateParam("Username",
                (String) params.get("username")));
        setAgentSshPassword(validateParam("Password",
                (String) params.get("password")));
        setCsHostGuid(validateParam("Cloudstack Host GUID", (String) params.get("guid")));
        setAgentOvsAgentUser(validateParam("OVS Username",
                (String) params.get("agentusername")));
        setAgentOvsAgentPassword(validateParam("OVS Password",
                (String) params.get("agentpassword")));
        setAgentPrivateNetworkName((String) params.get("private.network.device"));
        setAgentPublicNetworkName((String) params.get("public.network.device"));
        setAgentGuestNetworkName((String) params.get("guest.network.device"));
        setAgentStorageNetworkName((String) params.get("storage.network.device1"));
        setAgentStorageCheckTimeout(NumbersUtil.parseInt(
                (String) params.get("ovm3.heartbeat.timeout"),
                agentStorageCheckTimeout));
        setAgentStorageCheckInterval(NumbersUtil.parseInt(
                (String) params.get("ovm3.heartbeat.interval"),
                agentStorageCheckInterval));
        validatePoolAndCluster();
        if (params.containsKey("istest")) {
            setIsTest((Boolean) params.get("istest"));
        }
    }

    private String validateParam(final String name, final String param) throws ConfigurationException {
        if (param == null) {
            final String msg = "Unable to get " + name + " params are null";
            LOGGER.debug(msg);
            throw new ConfigurationException(msg);
        }
        return param;
    }

    /**
     * validatePoolAndCluster: A cluster is impossible with a pool. A pool is impossible without a vip.
     */
    private void validatePoolAndCluster() {
        if (agentInOvm3Cluster) {
            LOGGER.debug("Clustering requires a pool, setting pool to true");
            agentInOvm3Pool = true;
        }
        if (!NetUtils.isValidIp(ovm3PoolVip)) {
            LOGGER.debug("No VIP, Setting ovm3pool and ovm3cluster to false");
            agentInOvm3Pool = false;
            agentInOvm3Cluster = false;
            ovm3PoolVip = "";
        }
    }

    public String getAgentName() {
        return agentHostname;
    }

    public void setAgentName(final String agentName) {
        agentHostname = agentName;
    }

    public String getAgentIp() {
        return agentIp;
    }

    public void setAgentIp(final String agentIp) {
        this.agentIp = agentIp;
    }

    public Long getAgentZoneId() {
        return agentZoneId;
    }

    public void setAgentZoneId(final Long agentZoneId) {
        this.agentZoneId = agentZoneId;
    }

    public Long getAgentPodId() {
        return agentPodId;
    }

    public void setAgentPodId(final Long agentPodId) {
        this.agentPodId = agentPodId;
    }

    public String getAgentPoolId() {
        return agentPoolId;
    }

    public void setAgentPoolId(final String agentPoolId) {
        this.agentPoolId = agentPoolId;
    }

    public Long getAgentClusterId() {
        return agentClusterId;
    }

    public void setAgentClusterId(final Long agentClusterId) {
        this.agentClusterId = agentClusterId;
    }

    public String getAgentHostname() {
        return agentHostname;
    }

    public void setAgentHostname(final String agentHostname) {
        this.agentHostname = agentHostname;
    }

    public String getCsHostGuid() {
        return csHostGuid;
    }

    public void setCsHostGuid(final String csHostGuid) {
        this.csHostGuid = csHostGuid;
    }

    public String getAgentSshUserName() {
        return agentSshUserName;
    }

    public void setAgentSshUserName(final String agentSshUserName) {
        this.agentSshUserName = agentSshUserName;
    }

    public String getAgentSshPassword() {
        return agentSshPassword;
    }

    public void setAgentSshPassword(final String agentSshPassword) {
        this.agentSshPassword = agentSshPassword;
    }

    public String getAgentOvsAgentUser() {
        return agentOvsAgentUser;
    }

    public void setAgentOvsAgentUser(final String agentOvsAgentUser) {
        this.agentOvsAgentUser = agentOvsAgentUser;
    }

    public String getAgentOvsAgentPassword() {
        return agentOvsAgentPassword;
    }

    public void setAgentOvsAgentPassword(final String agentOvsAgentPassword) {
        this.agentOvsAgentPassword = agentOvsAgentPassword;
    }

    public Integer getAgentOvsAgentPort() {
        return agentOvsAgentPort;
    }

    public void setAgentOvsAgentPort(final Integer agentOvsAgentPort) {
        this.agentOvsAgentPort = agentOvsAgentPort;
    }

    public Boolean getAgentOvsAgentSsl() {
        return agentOvsAgentSsl;
    }

    public void setAgentOvsAgentSsl(final Boolean agentOvsAgentSsl) {
        this.agentOvsAgentSsl = agentOvsAgentSsl;
    }

    public String getAgentSshKeyFileName() {
        return agentSshKeyFile;
    }

    public void setAgentSshKeyFileName(final String agentSshFile) {
        agentSshKeyFile = agentSshFile;
    }

    public String getAgentOwnedByUuid() {
        return agentOwnedByUuid;
    }

    public void setAgentOwnedByUuid(final String agentOwnedByUuid) {
        this.agentOwnedByUuid = agentOwnedByUuid;
    }

    public Boolean getAgentIsMaster() {
        return agentIsMaster;
    }

    public void setAgentIsMaster(final Boolean agentIsMaster) {
        this.agentIsMaster = agentIsMaster;
    }

    public Boolean getAgentHasMaster() {
        return agentHasMaster;
    }

    public void setAgentHasMaster(final Boolean agentHasMaster) {
        this.agentHasMaster = agentHasMaster;
    }

    public Boolean getAgentInOvm3Pool() {
        return agentInOvm3Pool;
    }

    public void setAgentInOvm3Pool(final Boolean agentInOvm3Pool) {
        this.agentInOvm3Pool = agentInOvm3Pool;
    }

    public Boolean getAgentInOvm3Cluster() {
        return agentInOvm3Cluster;
    }

    public void setAgentInOvm3Cluster(final Boolean agentInOvm3Cluster) {
        this.agentInOvm3Cluster = agentInOvm3Cluster;
    }

    public String getOvm3PoolVip() {
        return ovm3PoolVip;
    }

    public void setOvm3PoolVip(final String ovm3PoolVip) {
        this.ovm3PoolVip = ovm3PoolVip;
    }

    public String getAgentPrivateNetworkName() {
        return agentPrivateNetworkName;
    }

    public void setAgentPrivateNetworkName(final String agentPrivateNetworkName) {
        this.agentPrivateNetworkName = agentPrivateNetworkName;
    }

    public String getAgentPublicNetworkName() {
        return agentPublicNetworkName;
    }

    public void setAgentPublicNetworkName(final String agentPublicNetworkName) {
        this.agentPublicNetworkName = agentPublicNetworkName;
    }

    public String getAgentGuestNetworkName() {
        return agentGuestNetworkName;
    }

    public void setAgentGuestNetworkName(final String agentGuestNetworkName) {
        this.agentGuestNetworkName = agentGuestNetworkName;
    }

    public String getAgentStorageNetworkName() {
        return agentStorageNetworkName;
    }

    public void setAgentStorageNetworkName(final String agentStorageNetworkName) {
        this.agentStorageNetworkName = agentStorageNetworkName;
    }

    public String getAgentControlNetworkName() {
        return agentControlNetworkName;
    }

    public void setAgentControlNetworkName(final String agentControlNetworkName) {
        this.agentControlNetworkName = agentControlNetworkName;
    }

    public String getAgentOvmRepoPath() {
        return agentOvmRepoPath;
    }

    public void setAgentOvmRepoPath(final String agentOvmRepoPath) {
        this.agentOvmRepoPath = agentOvmRepoPath;
    }

    public String getAgentSecStoragePath() {
        return agentSecStoragePath;
    }

    public void setAgentSecStoragePath(final String agentSecStoragePath) {
        this.agentSecStoragePath = agentSecStoragePath;
    }

    public int getDomRSshPort() {
        return domRSshPort;
    }

    public void setDomRSshPort(final int domRSshPort) {
        this.domRSshPort = domRSshPort;
    }

    public String getDomRCloudPath() {
        return domRCloudPath;
    }

    public void setDomRCloudPath(final String domRCloudPath) {
        this.domRCloudPath = domRCloudPath;
    }

    public Boolean getIsTest() {
        return istest;
    }

    public void setIsTest(final Boolean isTest) {
        istest = isTest;
    }

    public String getAgentScript() {
        return agentScript;
    }

    public void setAgentScript(final String agentScript) {
        this.agentScript = agentScript;
    }

    public String getAgentScriptsDir() {
        return agentScriptsDir;
    }

    public void setAgentScriptsDir(final String agentScriptsDir) {
        this.agentScriptsDir = agentScriptsDir;
    }

    public Map<String, Network.Interface> getAgentInterfaces() {
        return agentInterfaces;
    }

    public void setAgentInterfaces(final Map<String, Network.Interface> agentInterfaces) {
        this.agentInterfaces = agentInterfaces;
    }

    public List<String> getAgentScripts() {
        return agentScripts;
    }

    public void setAgentScripts(final List<String> agentScripts) {
        this.agentScripts = agentScripts;
    }

    public String getAgentCheckStorageScript() {
        return agentCheckStorageScript;
    }

    public void setAgentCheckStorageScript(final String agentCheckStorageScript) {
        this.agentCheckStorageScript = agentCheckStorageScript;
    }

    public Integer getAgentStorageCheckTimeout() {
        return agentStorageCheckTimeout;
    }

    public void setAgentStorageCheckTimeout(final Integer agentStorageCheckTimeout) {
        this.agentStorageCheckTimeout = agentStorageCheckTimeout;
    }

    public Integer getAgentStorageCheckInterval() {
        return agentStorageCheckInterval;
    }

    public void setAgentStorageCheckInterval(final Integer agentStorageCheckInterval) {
        this.agentStorageCheckInterval = agentStorageCheckInterval;
    }

    public String getVirtualDiskDir() {
        return virtualdiskdir;
    }

    public String getTemplateDir() {
        return templatedir;
    }

    public Map<String, Object> getRawParams() {
        return rawParams;
    }

    public void setRawParams(final Map<String, Object> params) {
        rawParams.putAll(params);
    }
}
