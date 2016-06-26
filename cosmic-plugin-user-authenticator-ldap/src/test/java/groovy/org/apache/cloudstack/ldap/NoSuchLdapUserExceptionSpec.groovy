package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.NoSuchLdapUserException;

class NoSuchLdapUserExceptionSpec extends spock.lang.Specification {
    def "Test that the username is correctly set within the No such LDAP user exception object"() {
        given: "You have created an No such LDAP user exception object with the username set"
        def exception = new NoSuchLdapUserException(username)
        expect: "The username is equal to the given data source"
        exception.getUsername() == username
        where: "The username is set to "
        username << ["", null, "rmurphy"]
    }
}
