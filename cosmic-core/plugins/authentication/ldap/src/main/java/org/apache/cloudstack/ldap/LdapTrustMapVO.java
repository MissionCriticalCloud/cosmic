package org.apache.cloudstack.ldap;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ldap_trust_map")
public class LdapTrustMapVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type")
    private LdapManager.LinkType type;

    @Column(name = "name")
    private String name;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "account_type")
    private short accountType;

    public LdapTrustMapVO() {
    }

    public LdapTrustMapVO(final long domainId, final LdapManager.LinkType type, final String name, final short accountType) {
        this.domainId = domainId;
        this.type = type;
        this.name = name;
        this.accountType = accountType;
    }

    @Override
    public long getId() {
        return id;
    }

    public LdapManager.LinkType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public long getDomainId() {
        return domainId;
    }

    public short getAccountType() {
        return accountType;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (int) (domainId ^ (domainId >>> 32));
        result = 31 * result + (int) accountType;
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LdapTrustMapVO that = (LdapTrustMapVO) o;

        if (domainId != that.domainId) {
            return false;
        }
        if (accountType != that.accountType) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        return name.equals(that.name);
    }
}
