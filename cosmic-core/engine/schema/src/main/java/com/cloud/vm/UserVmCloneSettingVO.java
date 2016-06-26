package com.cloud.vm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_vm_clone_setting")
public class UserVmCloneSettingVO {

    @Column(name = "vm_id")
    private Long vmId;

    @Column(name = "clone_type")
    private String cloneType;

    public UserVmCloneSettingVO() {

    }

    public UserVmCloneSettingVO(final long id, final String cloneType) {
        this.vmId = id;
        this.cloneType = cloneType;
    }

    public long getVmId() {
        return this.vmId;
    }

    public String getCloneType() {
        return this.cloneType;
    }
}
