package com.cloud.agent.api.to.overviews;

import com.cloud.uservm.UserVm;
import com.cloud.vm.Nic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VMOverviewTO {
    private VMTO[] vms;

    public VMOverviewTO() {
    }

    public VMOverviewTO(final Map<UserVm, List<Nic>> vmsAndNicsMap) {
        final List<VMTO> vmsTO = new ArrayList<>();
        vmsAndNicsMap.forEach((vm, nics) -> vmsTO.add(new VMTO(vm, nics)));
        vms = vmsTO.toArray(new VMTO[vmsTO.size()]);
    }

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

        public VMTO(final UserVm vm, final List<Nic> nics) {
            hostName = vm.getHostName();
            final List<InterfaceTO> interfacesTO = new ArrayList<>();
            nics.forEach(nic -> interfacesTO.add(new InterfaceTO(nic.getIPv4Address(), nic.getMacAddress(), nic.isDefaultNic())));
            interfaces = interfacesTO.toArray(new InterfaceTO[interfacesTO.size()]);
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
        }
    }
}
