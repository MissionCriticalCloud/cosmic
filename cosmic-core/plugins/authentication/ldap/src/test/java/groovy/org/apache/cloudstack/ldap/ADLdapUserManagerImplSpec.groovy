package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.ADLdapUserManagerImpl
import org.apache.cloudstack.ldap.LdapConfiguration
import spock.lang.Shared

import javax.naming.directory.SearchControls
import javax.naming.ldap.LdapContext

class ADLdapUserManagerImplSpec extends spock.lang.Specification {

    @Shared
    ADLdapUserManagerImpl adLdapUserManager;

    @Shared
    LdapConfiguration ldapConfiguration;

    def setup() {
        adLdapUserManager = new ADLdapUserManagerImpl();
        ldapConfiguration = Mock(LdapConfiguration);
        adLdapUserManager._ldapConfiguration = ldapConfiguration;
    }

    def "test generate AD search filter with nested groups enabled"() {
        ldapConfiguration.getUserObject() >> "user"
        ldapConfiguration.getCommonNameAttribute() >> "CN"
        ldapConfiguration.getBaseDn() >> "DC=cloud,DC=citrix,DC=com"
        ldapConfiguration.isNestedGroupsEnabled() >> true

        def result = adLdapUserManager.generateADGroupSearchFilter(group);
        expect:
        assert result.contains("memberOf:1.2.840.113556.1.4.1941:=")
        result == "(&(objectClass=user)(memberOf:1.2.840.113556.1.4.1941:=CN=" + group + ",DC=cloud,DC=citrix,DC=com))"
        where:
        group << ["dev", "dev-hyd"]
    }

    def "test generate AD search filter with nested groups disabled"() {
        ldapConfiguration.getUserObject() >> "user"
        ldapConfiguration.getCommonNameAttribute() >> "CN"
        ldapConfiguration.getBaseDn() >> "DC=cloud,DC=citrix,DC=com"
        ldapConfiguration.isNestedGroupsEnabled() >> false

        def result = adLdapUserManager.generateADGroupSearchFilter(group);
        expect:
        assert result.contains("memberOf=")
        result == "(&(objectClass=user)(memberOf=CN=" + group + ",DC=cloud,DC=citrix,DC=com))"
        where:
        group << ["dev", "dev-hyd"]
    }

    def "test getUsersInGroup null group"() {
        ldapConfiguration.getScope() >> SearchControls.SUBTREE_SCOPE
        ldapConfiguration.getReturnAttributes() >> ["username", "firstname", "lastname", "email"]
        ldapConfiguration.getBaseDn() >>> [null, null, "DC=cloud,DC=citrix,DC=com"]

        LdapContext context = Mock(LdapContext);

        when:
        def result = adLdapUserManager.getUsersInGroup(group, context)
        then:
        thrown(IllegalArgumentException)
        where:
        group << [null, "group", null]

    }
}
