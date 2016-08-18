package com.cloud.storage.secondary;

class SecondaryStorageCapacityCalculator {

    public int calculateRequiredCapacity(final int commandsStandBy, final int commandsPerVm) {
        final double ratio = (double) commandsStandBy / (double) commandsPerVm;
        return new Double(Math.ceil(ratio)).intValue();
    }
}
