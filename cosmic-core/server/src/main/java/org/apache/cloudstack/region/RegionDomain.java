package org.apache.cloudstack.region;

import com.cloud.domain.DomainVO;

public class RegionDomain extends DomainVO {
    String accountUuid;
    String parentUuid;
    String parentdomainname;
    Boolean haschild;

    public RegionDomain() {
    }

    public String getAccountuuid() {
        return accountUuid;
    }

    public void setAccountuuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(final String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getParentdomainname() {
        return parentdomainname;
    }

    public void setParentdomainname(final String parentdomainname) {
        this.parentdomainname = parentdomainname;
    }

    public Boolean getHaschild() {
        return haschild;
    }

    public void setHaschild(final Boolean haschild) {
        this.haschild = haschild;
    }
}
