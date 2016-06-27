package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.NoLdapUserMatchingQueryException

class NoLdapUserMatchingQueryExceptionSpec extends spock.lang.Specification {
    def "Test that the query is correctly set within the No LDAP user matching query exception object"() {
        given: "You have created an No LDAP user matching query exception object with a query set"
        def exception = new NoLdapUserMatchingQueryException(query)
        expect: "The username is equal to the given data source"
        exception.getQuery() == query
        where: "The username is set to "
        query << ["", null, "murp*"]
    }
}
