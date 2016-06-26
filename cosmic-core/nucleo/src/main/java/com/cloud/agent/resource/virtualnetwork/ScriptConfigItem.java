//

//

package com.cloud.agent.resource.virtualnetwork;

public class ScriptConfigItem extends ConfigItem {
    private String script;
    private String args;

    public ScriptConfigItem(final String script, final String args) {
        this.script = script;
        this.args = args;
    }

    public String getScript() {
        return script;
    }

    public void setScript(final String script) {
        this.script = script;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(final String args) {
        this.args = args;
    }

    @Override
    public String getAggregateCommand() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<script>\n");
        sb.append("/opt/cloud/bin/");
        sb.append(script);
        sb.append(' ');
        sb.append(args);
        sb.append("\n</script>\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ScriptConfigItem, executing ");
        sb.append(script);
        sb.append(' ');
        sb.append(args);
        return sb.toString();
    }
}
