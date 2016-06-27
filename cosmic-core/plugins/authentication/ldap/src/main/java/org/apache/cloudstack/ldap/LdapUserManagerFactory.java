package org.apache.cloudstack.ldap;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LdapUserManagerFactory implements ApplicationContextAware {

    public static final Logger s_logger = LoggerFactory.getLogger(LdapUserManagerFactory.class.getName());

    private static final Map<LdapUserManager.Provider, LdapUserManager> ldapUserManagerMap = new HashMap<>();

    private ApplicationContext applicationCtx;

    public LdapUserManager getInstance(final LdapUserManager.Provider provider) {
        LdapUserManager ldapUserManager;
        if (provider == LdapUserManager.Provider.MICROSOFTAD) {
            ldapUserManager = ldapUserManagerMap.get(LdapUserManager.Provider.MICROSOFTAD);
            if (ldapUserManager == null) {
                ldapUserManager = new ADLdapUserManagerImpl();
                applicationCtx.getAutowireCapableBeanFactory().autowireBeanProperties(ldapUserManager, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
                ldapUserManagerMap.put(LdapUserManager.Provider.MICROSOFTAD, ldapUserManager);
            }
        } else {
            //defaults to openldap
            ldapUserManager = ldapUserManagerMap.get(LdapUserManager.Provider.OPENLDAP);
            if (ldapUserManager == null) {
                ldapUserManager = new OpenLdapUserManagerImpl();
                applicationCtx.getAutowireCapableBeanFactory().autowireBeanProperties(ldapUserManager, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
                ldapUserManagerMap.put(LdapUserManager.Provider.OPENLDAP, ldapUserManager);
            }
        }
        return ldapUserManager;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        applicationCtx = applicationContext;
    }
}
