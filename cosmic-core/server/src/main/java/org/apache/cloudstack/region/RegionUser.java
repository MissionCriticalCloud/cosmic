package org.apache.cloudstack.region;

import com.cloud.user.UserVO;

public class RegionUser extends UserVO {
    String accountUuid;
    String created;
    String account;
    String accounttype;
    String domainid;
    String domain;

    public RegionUser() {
    }

    public String getAccountuuid() {
        return accountUuid;
    }

    public void setAccountuuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public String getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(final String accounttype) {
        this.accounttype = accounttype;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(final String domainid) {
        this.domainid = domainid;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }
}
