package com.cloud.agent.api;

import com.cloud.utils.net.NetUtils;

import java.net.URI;

public class PvlanSetupCommand extends Command {
    private String op;
    private String primary;
    private String isolated;
    private String vmMac;
    private String dhcpName;
    private String dhcpMac;
    private String dhcpIp;
    private Type type;
    private String networkTag;

    protected PvlanSetupCommand() {
    }

    protected PvlanSetupCommand(final Type type, final String op, final URI uri, final String networkTag) {
        this.type = type;
        this.op = op;
        this.primary = NetUtils.getPrimaryPvlanFromUri(uri);
        this.isolated = NetUtils.getIsolatedPvlanFromUri(uri);
        this.networkTag = networkTag;
    }

    static public PvlanSetupCommand createDhcpSetup(final String op, final URI uri, final String networkTag, final String dhcpName, final String dhcpMac, final String dhcpIp) {
        final PvlanSetupCommand cmd = new PvlanSetupCommand(Type.DHCP, op, uri, networkTag);
        cmd.setDhcpName(dhcpName);
        cmd.setDhcpMac(dhcpMac);
        cmd.setDhcpIp(dhcpIp);
        return cmd;
    }

    static public PvlanSetupCommand createVmSetup(final String op, final URI uri, final String networkTag, final String vmMac) {
        final PvlanSetupCommand cmd = new PvlanSetupCommand(Type.VM, op, uri, networkTag);
        cmd.setVmMac(vmMac);
        return cmd;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getOp() {
        return op;
    }

    public String getPrimary() {
        return primary;
    }

    public String getIsolated() {
        return isolated;
    }

    public String getVmMac() {
        return vmMac;
    }

    protected void setVmMac(final String vmMac) {
        this.vmMac = vmMac;
    }

    public String getDhcpMac() {
        return dhcpMac;
    }

    protected void setDhcpMac(final String dhcpMac) {
        this.dhcpMac = dhcpMac;
    }

    public String getDhcpIp() {
        return dhcpIp;
    }

    protected void setDhcpIp(final String dhcpIp) {
        this.dhcpIp = dhcpIp;
    }

    public Type getType() {
        return type;
    }

    public String getDhcpName() {
        return dhcpName;
    }

    public void setDhcpName(final String dhcpName) {
        this.dhcpName = dhcpName;
    }

    public String getNetworkTag() {
        return networkTag;
    }

    public enum Type {
        DHCP, VM
    }
}
