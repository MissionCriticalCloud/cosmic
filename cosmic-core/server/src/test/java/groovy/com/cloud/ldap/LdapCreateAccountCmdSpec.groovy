package groovy.com.cloud.ldap

import com.cloud.api.ServerApiException
import com.cloud.api.command.LdapCreateAccountCmd
import com.cloud.context.CallContext
import com.cloud.ldap.LdapManager
import com.cloud.ldap.LdapUser
import com.cloud.ldap.NoLdapUserMatchingQueryException
import com.cloud.user.AccountService
import spock.lang.Specification

class LdapCreateAccountCmdSpec extends Specification {

    def "Test failure to retrive LDAP user"() {
        given: "We have an LdapManager, AccountService and LdapCreateAccountCmd and LDAP user that doesn't exist"
        LdapManager ldapManager = Mock(LdapManager)
        ldapManager.getUser(_) >> { throw new NoLdapUserMatchingQueryException() }
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = Spy(LdapCreateAccountCmd, constructorArgs: [ldapManager, accountService])
        ldapCreateAccountCmd.getCurrentContext() >> Mock(CallContext)
        CallContext context = ldapCreateAccountCmd.getCurrentContext()
        when: "An an account is created"
        ldapCreateAccountCmd.execute()
        then: "It fails and an exception is thrown"
        thrown ServerApiException
    }

    def "Test failed creation due to a null response from cloudstack account creater"() {
        given: "We have an LdapManager, AccountService and LdapCreateAccountCmd"
        LdapManager ldapManager = Mock(LdapManager)
        ldapManager.getUser(_) >> new LdapUser("rmurphy", "rmurphy@cloudstack.org", "Ryan", "Murphy", "cn=rmurphy,ou=engineering,dc=cloudstack,dc=org", "engineering", false)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = Spy(LdapCreateAccountCmd, constructorArgs: [ldapManager, accountService])
        ldapCreateAccountCmd.getCurrentContext() >> Mock(CallContext)
        ldapCreateAccountCmd.createCloudstackUserAccount(_, _, _) >> null
        when: "Cloudstack fail to create the user"
        ldapCreateAccountCmd.execute()
        then: "An exception is thrown"
        thrown ServerApiException
    }

    def "Test command name"() {
        given: "We have an LdapManager, AccountService and LdapCreateAccountCmd"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService)
        when: "Get command name is called"
        def result = ldapCreateAccountCmd.getCommandName()
        then: "createaccountresponse is returned"
        result == "createaccountresponse"
    }

    def "Test getEntityOwnerId is 1"() {
        given: "We have an LdapManager, AccountService andL dapCreateAccount"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)

        def ldapCreateAccountCmd = Spy(LdapCreateAccountCmd, constructorArgs: [ldapManager, accountService])
        when: "Get entity owner id is called"
        long ownerId = ldapCreateAccountCmd.getEntityOwnerId()
        then: "1 is returned"
        ownerId == 1
    }

    def "Test password generation"() {
        given: "We have an LdapManager, AccountService and LdapCreateAccountCmd"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService)
        when: "A password is generated"
        def result = ldapCreateAccountCmd.generatePassword()
        then: "The result shouldn't be null or empty"
        result != ""
        result != null
    }

    def "Test validate User"() {
        given: "We have an LdapManager, AccountService andL dapCreateAccount"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService);
        when: "a user with an username, email, firstname and lastname is validated"
        def result = ldapCreateAccountCmd.validateUser(new LdapUser("username", "email", "firstname", "lastname", "principal", "domain", false))
        then: "the result is true"
        result == true
    }

    def "Test validate User empty email"() {
        given: "We have an LdapManager, AccountService andL dapCreateAccount"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService)
        when: "A user with no email address attempts to validate"
        ldapCreateAccountCmd.validateUser(new LdapUser("username", null, "firstname", "lastname", "principal", "domain", false))
        then: "An exception is thrown"
        thrown Exception
    }

    def "Test validate User empty firstname"() {
        given: "We have an LdapManager, AccountService andL dapCreateAccount"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService)
        when: "A user with no firstname attempts to validate"
        ldapCreateAccountCmd.validateUser(new LdapUser("username", "email", null, "lastname", "principal", false))
        then: "An exception is thrown"
        thrown Exception
    }

    def "Test validate User empty lastname"() {
        given: "We have an LdapManager, AccountService and LdapCreateAccountCmd"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService)
        when: "A user with no lastname attempts to validate"
        ldapCreateAccountCmd.validateUser(new LdapUser("username", "email", "firstname", null, "principal", "domain", false))
        then: "An exception is thown"
        thrown Exception
    }

    def "Test validation of a user"() {
        given: "We have an LdapManager, AccountService andL dapCreateAccount"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = Spy(LdapCreateAccountCmd, constructorArgs: [ldapManager, accountService])
        when: "Get command name is called"
        def commandName = ldapCreateAccountCmd.getCommandName()
        then: "createaccountresponse is returned"
        commandName == "createaccountresponse"
    }

    def "Test generate password"() {
        given: "We have an LdapManager, AccountService and LdapCreateAccount"
        LdapManager ldapManager = Mock(LdapManager)
        AccountService accountService = Mock(AccountService)
        def ldapCreateAccountCmd = new LdapCreateAccountCmd(ldapManager, accountService)
        when: "A random password is generated for a new account"
        String password = ldapCreateAccountCmd.generatePassword()
        then: "password should not be the array address but an actual encoded string. verifying length > 20 as the byte array size is 20"
        password.length() > 20
    }
}
