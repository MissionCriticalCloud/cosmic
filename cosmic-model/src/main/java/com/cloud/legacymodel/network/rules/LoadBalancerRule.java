package com.cloud.legacymodel.network.rules;

public class LoadBalancerRule {

    private String[] configuration;
    private String tmpCfgFilePath;
    private String tmpCfgFileName;

    private String[] addRules;
    private String[] removeRules;
    private String[] statRules;

    private String routerIp;

    public LoadBalancerRule() {
        // Empty constructor for (de)serialization
    }

    public LoadBalancerRule(final String[] configuration, final String tmpCfgFilePath, final String tmpCfgFileName, final String[] addRules, final String[] removeRules, final
    String[] statRules, final String routerIp) {
        this.configuration = configuration;
        this.tmpCfgFilePath = tmpCfgFilePath;
        this.tmpCfgFileName = tmpCfgFileName;
        this.addRules = addRules;
        this.removeRules = removeRules;
        this.statRules = statRules;
        this.routerIp = routerIp;
    }

    public String[] getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(final String[] configuration) {
        this.configuration = configuration;
    }

    public String getTmpCfgFilePath() {
        return this.tmpCfgFilePath;
    }

    public void setTmpCfgFilePath(final String tmpCfgFilePath) {
        this.tmpCfgFilePath = tmpCfgFilePath;
    }

    public String getTmpCfgFileName() {
        return this.tmpCfgFileName;
    }

    public void setTmpCfgFileName(final String tmpCfgFileName) {
        this.tmpCfgFileName = tmpCfgFileName;
    }

    public String[] getAddRules() {
        return this.addRules;
    }

    public void setAddRules(final String[] addRules) {
        this.addRules = addRules;
    }

    public String[] getRemoveRules() {
        return this.removeRules;
    }

    public void setRemoveRules(final String[] removeRules) {
        this.removeRules = removeRules;
    }

    public String[] getStatRules() {
        return this.statRules;
    }

    public void setStatRules(final String[] statRules) {
        this.statRules = statRules;
    }

    public String getRouterIp() {
        return this.routerIp;
    }

    public void setRouterIp(final String routerIp) {
        this.routerIp = routerIp;
    }
}
