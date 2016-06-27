package com.cloud.hypervisor;

import com.cloud.storage.Storage.ImageFormat;

public class Hypervisor {

    public static enum HypervisorType {
        None, //for storage hosts
        XenServer,
        KVM,
        VirtualBox,
        Parralels,
        Ovm3,

        Any; /*If you don't care about the hypervisor type*/

        public static HypervisorType getType(final String hypervisor) {
            if (hypervisor == null) {
                return HypervisorType.None;
            }
            if (hypervisor.equalsIgnoreCase("XenServer")) {
                return HypervisorType.XenServer;
            } else if (hypervisor.equalsIgnoreCase("KVM")) {
                return HypervisorType.KVM;
            } else if (hypervisor.equalsIgnoreCase("VirtualBox")) {
                return HypervisorType.VirtualBox;
            } else if (hypervisor.equalsIgnoreCase("Parralels")) {
                return HypervisorType.Parralels;
            } else if (hypervisor.equalsIgnoreCase("Any")) {
                return HypervisorType.Any;
            } else if (hypervisor.equalsIgnoreCase("Ovm3")) {
                return HypervisorType.Ovm3;
            } else {
                return HypervisorType.None;
            }
        }

        /**
         * This method really needs to be part of the properties of the hypervisor type itself.
         *
         * @param hyperType
         * @return
         */
        public static ImageFormat getSupportedImageFormat(final HypervisorType hyperType) {
            if (hyperType == HypervisorType.XenServer) {
                return ImageFormat.VHD;
            } else if (hyperType == HypervisorType.KVM) {
                return ImageFormat.QCOW2;
            } else if (hyperType == HypervisorType.Ovm3) {
                return ImageFormat.RAW;
            } else {
                return null;
            }
        }
    }
}
