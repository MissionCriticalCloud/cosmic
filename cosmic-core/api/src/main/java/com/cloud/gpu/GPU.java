package com.cloud.gpu;

public class GPU {

    public enum Keys {
        pciDevice,
        vgpuType
    }

    public enum GPUType {
        GRID_K100("GRID K100"),
        GRID_K120Q("GRID K120Q"),
        GRID_K140Q("GRID K140Q"),
        GRID_K200("GRID K200"),
        GRID_K220Q("GRID K220Q"),
        GRID_K240Q("GRID K240Q"),
        GRID_K260("GRID K260Q"),
        passthrough("passthrough");

        private final String type;

        GPUType(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
