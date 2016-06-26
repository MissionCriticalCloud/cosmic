package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.LdapConfigurationVO


class LdapConfigurationVOSpec extends spock.lang.Specification {
    def "Testing that the ID hostname and port is correctly set within the LDAP configuration VO"() {
        given: "You have created an LDAP Configuration VO"
        def configuration = new LdapConfigurationVO(hostname, port)
        configuration.setId(id)
        expect: "The id hostname and port is equal to the given data source"
        configuration.getId() == id
        configuration.getHostname() == hostname
        configuration.getPort() == port
        where: "The id, hostname and port is set to "
        hostname << ["", null, "localhost"]
        id << [0, 1000, -1000]
        port << [0, 1000, -1000]
    }
}
