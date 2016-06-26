//

//

package com.cloud.agent.api;

public class GetDomRVersionAnswer extends Answer {
    public static final String ROUTER_NAME = "router.name";
    public static final String ROUTER_IP = "router.ip";
    String templateVersion;
    String scriptsVersion;

    protected GetDomRVersionAnswer() {
    }

    public GetDomRVersionAnswer(final GetDomRVersionCmd cmd, final String details, final String templateVersion, final String scriptsVersion) {
        super(cmd, true, details);
        this.templateVersion = templateVersion;
        this.scriptsVersion = scriptsVersion;
    }

    public GetDomRVersionAnswer(final GetDomRVersionCmd cmd, final String details) {
        super(cmd, false, details);
    }

    public String getTemplateVersion() {
        return this.templateVersion;
    }

    public String getScriptsVersion() {
        return this.scriptsVersion;
    }
}
