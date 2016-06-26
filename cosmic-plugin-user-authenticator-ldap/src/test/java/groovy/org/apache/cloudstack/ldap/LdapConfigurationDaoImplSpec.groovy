package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.dao.LdapConfigurationDaoImpl

class LdapConfigurationDaoImplSpec extends spock.lang.Specification {
    def "Test setting up of a LdapConfigurationDao"() {
        given: "We have an LdapConfigurationDao implementation"
        def ldapConfigurationDaoImpl = new LdapConfigurationDaoImpl();
        expect: "that hostnameSearch and listAllConfigurationsSearch is configured"
        ldapConfigurationDaoImpl.hostnameSearch != null;
        ldapConfigurationDaoImpl.listAllConfigurationsSearch != null
    }
}
