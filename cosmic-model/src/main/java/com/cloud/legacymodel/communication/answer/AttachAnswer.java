package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.to.DiskTO;

public class AttachAnswer extends Answer {
    private DiskTO disk;

    public AttachAnswer() {
        super(null);
    }

    public AttachAnswer(final DiskTO disk) {
        super(null);
        setDisk(disk);
    }

    public AttachAnswer(final String errMsg) {
        super(null, false, errMsg);
    }

    public DiskTO getDisk() {
        return disk;
    }

    public void setDisk(final DiskTO disk) {
        this.disk = disk;
    }
}
