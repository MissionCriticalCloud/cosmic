//

//

package com.cloud.network;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nicira_nvp_nic_map")
public class NiciraNvpNicMappingVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "logicalswitch")
    private String logicalSwitchUuid;

    @Column(name = "logicalswitchport")
    private String logicalSwitchPortUuid;

    @Column(name = "nic")
    private String nicUuid;

    public NiciraNvpNicMappingVO() {
    }

    public NiciraNvpNicMappingVO(final String logicalSwitchUuid, final String logicalSwitchPortUuid, final String nicUuid) {
        this.logicalSwitchUuid = logicalSwitchUuid;
        this.logicalSwitchPortUuid = logicalSwitchPortUuid;
        this.nicUuid = nicUuid;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }

    public void setLogicalSwitchUuid(final String logicalSwitchUuid) {
        this.logicalSwitchUuid = logicalSwitchUuid;
    }

    public String getLogicalSwitchPortUuid() {
        return logicalSwitchPortUuid;
    }

    public void setLogicalSwitchPortUuid(final String logicalSwitchPortUuid) {
        this.logicalSwitchPortUuid = logicalSwitchPortUuid;
    }

    public String getNicUuid() {
        return nicUuid;
    }

    public void setNicUuid(final String nicUuid) {
        this.nicUuid = nicUuid;
    }

    @Override
    public long getId() {
        return id;
    }
}
