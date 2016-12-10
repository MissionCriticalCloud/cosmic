package groovy.org.apache.cloudstack.ldap

import com.cloud.ldap.ADLdapUserManagerImpl
import com.cloud.ldap.LdapUserManager
import com.cloud.ldap.LdapUserManagerFactory
import com.cloud.ldap.OpenLdapUserManagerImpl
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import spock.lang.Shared

class LdapUserManagerFactorySpec extends spock.lang.Specification {

    @Shared
    def LdapUserManagerFactory ldapUserManagerFactory;

    def setupSpec() {
        ldapUserManagerFactory = new LdapUserManagerFactory();
        ApplicationContext applicationContext = Mock(ApplicationContext);
        AutowireCapableBeanFactory autowireCapableBeanFactory = Mock(AutowireCapableBeanFactory);
        applicationContext.getAutowireCapableBeanFactory() >> autowireCapableBeanFactory;
        ldapUserManagerFactory.setApplicationContext(applicationContext);
    }

    def "Test getInstance() from factory"() {
        def result = ldapUserManagerFactory.getInstance(id);

        def expected;
        if (id == LdapUserManager.Provider.MICROSOFTAD) {
            expected = ADLdapUserManagerImpl.class;
        } else {
            expected = OpenLdapUserManagerImpl.class;
        }

        expect:
        assert result.class.is(expected)
        where:
        id << [LdapUserManager.Provider.MICROSOFTAD, LdapUserManager.Provider.OPENLDAP, null]
    }
}
