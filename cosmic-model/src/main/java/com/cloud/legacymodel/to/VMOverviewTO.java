package com.cloud.legacymodel.to;

import java.util.HashMap;
import java.util.Map;

public class VMOverviewTO {
    private VMTO[] vms;

    public VMTO[] getVms() {
        return vms;
    }

    public void setVms(final VMTO[] vms) {
        this.vms = vms;
    }

    public static class VMTO {
        private String hostName;
        private InterfaceTO[] interfaces;

        public VMTO() {
        }

        public VMTO(final String hostName) {
            this.hostName = hostName;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(final String hostName) {
            this.hostName = hostName;
        }

        public InterfaceTO[] getInterfaces() {
            return interfaces;
        }

        public void setInterfaces(final InterfaceTO[] interfaces) {
            this.interfaces = interfaces;
        }

        public static class InterfaceTO {
            private String ipv4Address;
            private String macAddress;
            private boolean isDefault;
            private Map<String, String> metadata;
            private Map<String, String> userData;

            public InterfaceTO() {
            }

            public InterfaceTO(final String ipv4Address, final String macAddress, final boolean isDefault) {
                this.ipv4Address = ipv4Address;
                this.macAddress = macAddress;
                this.isDefault = isDefault;
            }

            public String getIpv4Address() {
                return ipv4Address;
            }

            public void setIpv4Address(final String ipv4Address) {
                this.ipv4Address = ipv4Address;
            }

            public String getMacAddress() {
                return macAddress;
            }

            public void setMacAddress(final String macAddress) {
                this.macAddress = macAddress;
            }

            public boolean isDefault() {
                return isDefault;
            }

            public void setDefault(final boolean aDefault) {
                isDefault = aDefault;
            }

            public Map<String, String> getMetadata() {
                if (metadata == null) {
                    metadata = new HashMap<>();
                }

                return metadata;
            }

            public Map<String, String> getUserData() {
                if (userData == null) {
                    userData = new HashMap<>();
                }

                return userData;
            }
        }
    }
}
