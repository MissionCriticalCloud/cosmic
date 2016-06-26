package com.cloud.storage.secondary;

import com.cloud.utils.events.EventArgs;
import com.cloud.vm.SecondaryStorageVmVO;

public class SecStorageVmAlertEventArgs extends EventArgs {

    public static final int SSVM_CREATED = 1;
    public static final int SSVM_UP = 2;
    public static final int SSVM_DOWN = 3;
    public static final int SSVM_CREATE_FAILURE = 4;
    public static final int SSVM_START_FAILURE = 5;
    public static final int SSVM_FIREWALL_ALERT = 6;
    public static final int SSVM_STORAGE_ALERT = 7;
    public static final int SSVM_REBOOTED = 8;
    public static final String ALERT_SUBJECT = "ssvm-alert";
    private static final long serialVersionUID = 23773987551479885L;
    private final int type;
    private final long zoneId;
    private final long ssVmId;
    private final SecondaryStorageVmVO ssVm;
    private final String message;

    public SecStorageVmAlertEventArgs(final int type, final long zoneId, final long ssVmId, final SecondaryStorageVmVO ssVm, final String message) {

        super(ALERT_SUBJECT);
        this.type = type;
        this.zoneId = zoneId;
        this.ssVmId = ssVmId;
        this.ssVm = ssVm;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public long getZoneId() {
        return zoneId;
    }

    public long getSecStorageVmId() {
        return ssVmId;
    }

    public SecondaryStorageVmVO getSecStorageVm() {
        return ssVm;
    }

    public String getMessage() {
        return message;
    }
}
