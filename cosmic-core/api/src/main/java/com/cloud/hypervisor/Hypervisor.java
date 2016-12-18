package com.cloud.hypervisor;

import com.cloud.storage.Storage.ImageFormat;

public class Hypervisor {

    public static enum HypervisorType {
        None, //for storage hosts
        XenServer,
        KVM,

        Any; /*If you don't care about the hypervisor type*/

        public static HypervisorType getType(final String hypervisor) {
            if (hypervisor == null) {
                return HypervisorType.None;
            }
            if (hypervisor.equalsIgnoreCase("XenServer")) {
                return HypervisorType.XenServer;
            } else if (hypervisor.equalsIgnoreCase("KVM")) {
                return HypervisorType.KVM;
            } else if (hypervisor.equalsIgnoreCase("Any")) {
                return HypervisorType.Any;
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
            } else {
                return null;
            }
        }
    }
}
