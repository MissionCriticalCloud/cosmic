package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.LdapUtils

import javax.naming.directory.Attribute
import javax.naming.directory.Attributes

class LdapUtilsSpec extends spock.lang.Specification {
    def "Testing than an attribute is not successfully returned"() {
        given: "You have an attributes object with some attribute"
        def attributes = Mock(Attributes)
        attributes.get("uid") >> null

        when: "You get the attribute"
        String foundValue = LdapUtils.getAttributeValue(attributes, "uid")

        then: "Its value equals uid"
        foundValue == null
    }

    def "Testing than an attribute is successfully returned"() {
        given: "You have an attributes object with some attribute"
        def attributes = Mock(Attributes)
        def attribute = Mock(Attribute)
        attribute.getId() >> name
        attribute.get() >> value
        attributes.get(name) >> attribute

        when: "You get the attribute"
        String foundValue = LdapUtils.getAttributeValue(attributes, name)

        then: "Its value equals uid"
        foundValue == value

        where:
        name    | value
        "uid"   | "rmurphy"
        "email" | "rmurphy@test.com"
    }

    def "Testing that a Ldap Search Filter is correctly escaped"() {
        given: "You have some input from a user"

        expect: "That the input is escaped"
        LdapUtils.escapeLDAPSearchFilter(input) == result

        where: "The following inputs are given "
        input                                       | result
        "Hi This is a test #çà"                     | "Hi This is a test #çà"
        "Hi (This) = is * a \\ test # ç à ô \u0000" | "Hi \\28This\\29 = is \\2a a \\5c test # ç à ô \\00"
    }
}
