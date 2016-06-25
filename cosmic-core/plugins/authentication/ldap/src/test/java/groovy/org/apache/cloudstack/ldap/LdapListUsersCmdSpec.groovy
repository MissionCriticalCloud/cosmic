package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.api.command.LdapListUsersCmd
import org.apache.cloudstack.api.response.LdapUserResponse
import org.apache.cloudstack.api.response.ListResponse
import org.apache.cloudstack.api.response.UserResponse
import org.apache.cloudstack.ldap.LdapManager
import org.apache.cloudstack.ldap.LdapUser
import org.apache.cloudstack.ldap.NoLdapUserMatchingQueryException
import org.apache.cloudstack.query.QueryService

class LdapListUsersCmdSpec extends spock.lang.Specification {
    def "Test getEntityOwnerId is 1"() {
        given: "We have an LdapManager, QueryService and LdapListUsersCmd"
        def ldapManager = Mock(LdapManager)
        def queryService = Mock(QueryService)
        def ldapListUsersCmd = new LdapListUsersCmd(ldapManager, queryService)
        when: "Get entity owner id is called"
        long ownerId = ldapListUsersCmd.getEntityOwnerId()
        then: "a 1 should be returned"
        ownerId == 1
    }

    def "Test successful empty response from execute"() {
        given: "We have a LdapManager with no users, QueryService and a LdapListUsersCmd"
        def ldapManager = Mock(LdapManager)
        ldapManager.getUsers() >> { throw new NoLdapUserMatchingQueryException() }
        def queryService = Mock(QueryService)
        def ldapListUsersCmd = new LdapListUsersCmd(ldapManager, queryService)
        when: "LdapListUsersCmd is executed"
        ldapListUsersCmd.execute()
        then: "An array of size 0 is returned"
        ldapListUsersCmd.responseObject.getResponses().size() == 0
    }

    def "Test successful response from execute"() {
        given: "We have an LdapManager, one user, QueryService and a LdapListUsersCmd"
        def ldapManager = Mock(LdapManager)
        List<LdapUser> users = new ArrayList()
        users.add(new LdapUser("rmurphy", "rmurphy@test.com", "Ryan", "Murphy", "cn=rmurphy,dc=cloudstack,dc=org", null, false))
        ldapManager.getUsers() >> users
        LdapUserResponse response = new LdapUserResponse("rmurphy", "rmurphy@test.com", "Ryan", "Murphy", "cn=rmurphy,dc=cloudstack,dc=org", null)
        ldapManager.createLdapUserResponse(_) >> response
        def queryService = Mock(QueryService)
        def ldapListUsersCmd = new LdapListUsersCmd(ldapManager, queryService)
        when: "LdapListUsersCmd is executed"
        ldapListUsersCmd.execute()
        then: "a list of size not 0 is returned"
        ldapListUsersCmd.responseObject.getResponses().size() != 0
    }

    def "Test successful return of getCommandName"() {
        given: "We have an LdapManager, QueryService and a LdapListUsersCmd"
        def ldapManager = Mock(LdapManager)
        def queryService = Mock(QueryService)
        def ldapListUsersCmd = new LdapListUsersCmd(ldapManager, queryService)
        when: "Get command name is called"
        String commandName = ldapListUsersCmd.getCommandName()
        then: "ldapuserresponse is returned"
        commandName == "ldapuserresponse"
    }

    def "Test successful result from isACloudstackUser"() {
        given: "We have an LdapManager and a LdapListUsersCmd"
        def ldapManager = Mock(LdapManager)
        def queryService = Mock(QueryService)

        UserResponse userResponse = new UserResponse()
        userResponse.setUsername("rmurphy")

        ArrayList<UserResponse> responses = new ArrayList<UserResponse>()
        responses.add(userResponse);

        ListResponse<UserResponse> queryServiceResponse = new ListResponse<UserResponse>()
        queryServiceResponse.setResponses(responses)

        queryService.searchForUsers(_) >> queryServiceResponse

        def ldapUser = new LdapUser("rmurphy", "rmurphy@cloudstack.org", "Ryan", "Murphy", "cn=rmurphy,dc=cloudstack,dc=org", null, false)
        def ldapListUsersCmd = new LdapListUsersCmd(ldapManager, queryService)

        when: "isACloudstackUser is executed"
        def result = ldapListUsersCmd.isACloudstackUser(ldapUser);

        then: "The result is true"
        result == true;
    }

    def "Test failed result from isACloudstackUser"() {
        given: "We have an LdapManager and a LdapListUsersCmd"
        def ldapManager = Mock(LdapManager)
        def queryService = Mock(QueryService)

        queryService.searchForUsers(_) >> new ListResponse<UserResponse>()

        def ldapUser = new LdapUser("rmurphy", "rmurphy@cloudstack.org", "Ryan", "Murphy", "cn=rmurphy,dc=cloudstack,dc=org", null, false)
        def ldapListUsersCmd = new LdapListUsersCmd(ldapManager, queryService)

        when: "isACloudstackUser is executed"
        def result = ldapListUsersCmd.isACloudstackUser(ldapUser);

        then: "The result is true"
        result == false;
    }
}
