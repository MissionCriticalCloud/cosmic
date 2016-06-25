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
@Table(name = "nicira_nvp_router_map")
public class NiciraNvpRouterMappingVO implements InternalIdentity {
    //FIXME the ddl for this table should be in one of the upgrade scripts
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "logicalrouter_uuid")
    private String logicalRouterUuid;

    @Column(name = "network_id")
    private long networkId;

    public NiciraNvpRouterMappingVO() {
    }

    public NiciraNvpRouterMappingVO(final String logicalRouterUuid, final long networkId) {
        this.logicalRouterUuid = logicalRouterUuid;
        this.networkId = networkId;
    }

    public NiciraNvpRouterMappingVO(final long id, final String logicalRouterUuid, final long networkId) {
        this.id = id;
        this.logicalRouterUuid = logicalRouterUuid;
        this.networkId = networkId;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }

    public void setLogicalRouterUuid(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(final long networkId) {
        this.networkId = networkId;
    }
}
