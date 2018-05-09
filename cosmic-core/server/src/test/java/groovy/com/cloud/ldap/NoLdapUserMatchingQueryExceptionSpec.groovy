package groovy.com.cloud.ldap

import com.cloud.ldap.NoLdapUserMatchingQueryException
import spock.lang.Specification


class NoLdapUserMatchingQueryExceptionSpec extends Specification {
    def "Test that the query is correctly set within the No LDAP user matching query exception object"() {
        given: "You have created an No LDAP user matching query exception object with a query set"
        def exception = new NoLdapUserMatchingQueryException(query)
        expect: "The username is equal to the given data source"
        exception.getQuery() == query
        where: "The username is set to "
        query << ["", null, "murp*"]
    }
}
