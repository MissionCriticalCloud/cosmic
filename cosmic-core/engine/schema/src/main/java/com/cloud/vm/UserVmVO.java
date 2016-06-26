package com.cloud.vm;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.uservm.UserVm;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.HashMap;

@Entity
@Table(name = "user_vm")
@DiscriminatorValue(value = "User")
@PrimaryKeyJoinColumn(name = "id")
public class UserVmVO extends VMInstanceVO implements UserVm {

    @Column(name = "update_parameters", updatable = true)
    protected boolean updateParameters = true;
    transient String password;
    @Column(name = "iso_id", nullable = true, length = 17)
    private Long isoId = null;
    @Column(name = "user_data", updatable = true, nullable = true, length = 32768)
    @Basic(fetch = FetchType.LAZY)
    private String userData;
    @Column(name = "display_name", updatable = true, nullable = true)
    private String displayName;

    public UserVmVO(final long id, final String instanceName, final String displayName, final long templateId, final HypervisorType hypervisorType, final long guestOsId, final
    boolean haEnabled,
                    final boolean limitCpuUse, final long domainId, final long accountId, final long userId, final long serviceOfferingId, final String userData, final String
                            name, final Long diskOfferingId) {
        super(id, serviceOfferingId, name, instanceName, Type.User, templateId, hypervisorType, guestOsId, domainId, accountId, userId, haEnabled, limitCpuUse, diskOfferingId);
        this.userData = userData;
        this.displayName = displayName;
        this.details = new HashMap<>();
    }

    protected UserVmVO() {
        super();
    }

    @Override
    public Long getIsoId() {
        return isoId;
    }

    public void setIsoId(final Long id) {
        this.isoId = id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getUserData() {
        return userData;
    }

    @Override
    public void setUserData(final String userData) {
        this.userData = userData;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getDetail(final String name) {
        return details != null ? details.get(name) : null;
    }

    @Override
    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public boolean isUpdateParameters() {
        return updateParameters;
    }

    public void setUpdateParameters(final boolean updateParameters) {
        this.updateParameters = updateParameters;
    }
}
