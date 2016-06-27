package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.api.response.LdapConfigurationResponse

class LdapConfigurationResponseSpec extends spock.lang.Specification {
    def "Testing succcessful setting of LdapConfigurationResponse hostname"() {
        given: "We have a LdapConfigurationResponse"
        LdapConfigurationResponse response = new LdapConfigurationResponse();
        when: "The hostname is set"
        response.setHostname("localhost");
        then: "Get hostname should return the set value"
        response.getHostname() == "localhost";
    }

    def "Testing successful setting of LdapConfigurationResponse hostname and port via constructor"() {
        given: "We have a LdapConfiguration response"
        LdapConfigurationResponse response
        when: "both hostname and port are set by constructor"
        response = new LdapConfigurationResponse("localhost", 389)
        then: "Get hostname and port should return the set values."
        response.getHostname() == "localhost"
        response.getPort() == 389
    }

    def "Testing successful setting of LdapConfigurationResponse port"() {
        given: "We have a LdapConfigurationResponse"
        LdapConfigurationResponse response = new LdapConfigurationResponse()
        when: "The port is set"
        response.setPort(389)
        then: "Get port should return the set value"
        response.getPort() == 389
    }
}
