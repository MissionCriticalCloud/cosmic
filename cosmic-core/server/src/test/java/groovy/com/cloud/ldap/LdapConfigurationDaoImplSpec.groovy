package groovy.com.cloud.ldap

import com.cloud.ldap.dao.LdapConfigurationDaoImpl
import spock.lang.Specification


class LdapConfigurationDaoImplSpec extends Specification {
    def "Test setting up of a LdapConfigurationDao"() {
        given: "We have an LdapConfigurationDao implementation"
        def ldapConfigurationDaoImpl = new LdapConfigurationDaoImpl();
        expect: "that hostnameSearch and listAllConfigurationsSearch is configured"
        ldapConfigurationDaoImpl.hostnameSearch != null;
        ldapConfigurationDaoImpl.listAllConfigurationsSearch != null
    }
}
