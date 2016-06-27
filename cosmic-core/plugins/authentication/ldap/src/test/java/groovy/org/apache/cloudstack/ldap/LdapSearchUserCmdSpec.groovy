package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.api.command.LdapUserSearchCmd
import org.apache.cloudstack.api.response.LdapUserResponse
import org.apache.cloudstack.ldap.LdapManager
import org.apache.cloudstack.ldap.LdapUser
import org.apache.cloudstack.ldap.NoLdapUserMatchingQueryException

class LdapSearchUserCmdSpec extends spock.lang.Specification {
    def "Test getEntityOwnerId is 1"() {
        given: "We have an Ldap manager and ldap user search cmd"
        def ldapManager = Mock(LdapManager)
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when: "getEntityOwnerId is called"
        long ownerId = ldapUserSearchCmd.getEntityOwnerId()
        then: "1 is returned"
        ownerId == 1
    }

    def "Test successful empty response from execute"() {
        given: "We have an Ldap manager and ldap user search cmd"
        def ldapManager = Mock(LdapManager)
        ldapManager.searchUsers(_) >> { throw new NoLdapUserMatchingQueryException() }
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when: "The command is executed with no users found"
        ldapUserSearchCmd.execute()
        then: "An empty array is returned"
        ldapUserSearchCmd.responseObject.getResponses().size() == 0
    }

    def "Test successful response from execute"() {
        given: "We have an Ldap manager and ldap user search cmd"
        def ldapManager = Mock(LdapManager)
        List<LdapUser> users = new ArrayList()
        users.add(new LdapUser("rmurphy", "rmurphy@test.com", "Ryan", "Murphy", "cn=rmurphy,dc=cloudstack,dc=org", null, false))
        ldapManager.searchUsers(_) >> users
        LdapUserResponse response = new LdapUserResponse("rmurphy", "rmurphy@test.com", "Ryan", "Murphy", "cn=rmurphy,dc=cloudstack,dc=org", null)
        ldapManager.createLdapUserResponse(_) >> response
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when: "The command is executed"
        ldapUserSearchCmd.execute()
        then: "A array with length of atleast 1 is returned"
        ldapUserSearchCmd.responseObject.getResponses().size() > 0
    }

    def "Test successful return of getCommandName"() {
        given: "We have an Ldap manager and ldap user search cmd"
        def ldapManager = Mock(LdapManager)
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when: "When a request for the command name is made"
        String commandName = ldapUserSearchCmd.getCommandName()
        then: "ldapuserresponse is returned"
        commandName == "ldapuserresponse"
    }
}
