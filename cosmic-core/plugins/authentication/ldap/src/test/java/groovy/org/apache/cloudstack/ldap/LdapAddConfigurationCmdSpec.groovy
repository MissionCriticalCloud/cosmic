package groovy.org.apache.cloudstack.ldap

import com.cloud.exception.InvalidParameterValueException
import org.apache.cloudstack.api.ServerApiException
import org.apache.cloudstack.api.command.LdapAddConfigurationCmd
import org.apache.cloudstack.api.response.LdapConfigurationResponse
import org.apache.cloudstack.ldap.LdapManager

class LdapAddConfigurationCmdSpec extends spock.lang.Specification {

    def "Test failed response from execute"() {
        given: "We have an LDAP manager, no configuration and a LdapAddConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        ldapManager.addConfiguration(_, _) >> { throw new InvalidParameterValueException() }
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when: "LdapAddCofnigurationCmd is executed"
        ldapAddConfigurationCmd.execute()
        then: "an exception is thrown"
        thrown ServerApiException
    }

    def "Test getEntityOwnerId is 1"() {
        given: "We have an LdapManager and LdapConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when: "Get Entity Owner Id is called"
        long ownerId = ldapAddConfigurationCmd.getEntityOwnerId()
        then: "1 is returned"
        ownerId == 1
    }

    def "Test successful response from execute"() {
        given: "We have an LDAP Manager that has a configuration and a LdapAddConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        ldapManager.addConfiguration(_, _) >> new LdapConfigurationResponse("localhost", 389)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when: "LdapAddConfigurationCmd is executed"
        ldapAddConfigurationCmd.execute()
        then: "the responseObject should have the hostname localhost and port 389"
        ldapAddConfigurationCmd.responseObject.hostname == "localhost"
        ldapAddConfigurationCmd.responseObject.port == 389
    }

    def "Test successful return of getCommandName"() {
        given: "We have an LdapManager and LdapConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when: "Get Command Name is called"
        String commandName = ldapAddConfigurationCmd.getCommandName()
        then: "ldapconfigurationresponse is returned"
        commandName == "ldapconfigurationresponse"
    }

    def "Test successful setting of hostname"() {
        given: "We have an LdapManager and LdapAddConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when: "The hostname is set"
        ldapAddConfigurationCmd.setHostname("localhost")
        then: "Get hostname returns the set hostname"
        ldapAddConfigurationCmd.getHostname() == "localhost"
    }

    def "Test successful setting of port"() {
        given: "We have an LdapManager and LdapAddConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when: "The port is set"
        ldapAddConfigurationCmd.setPort(389)
        then: "Get port returns the port"
        ldapAddConfigurationCmd.getPort() == 389
    }
}
