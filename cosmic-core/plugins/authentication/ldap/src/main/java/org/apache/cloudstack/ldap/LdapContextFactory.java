package org.apache.cloudstack.ldap;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapContextFactory {
    private static final Logger s_logger = LoggerFactory.getLogger(LdapContextFactory.class.getName());

    @Inject
    private LdapConfiguration _ldapConfiguration;

    public LdapContextFactory() {
    }

    public LdapContextFactory(final LdapConfiguration ldapConfiguration) {
        _ldapConfiguration = ldapConfiguration;
    }

    public LdapContext createBindContext() throws NamingException, IOException {
        return createBindContext(null);
    }

    public LdapContext createBindContext(final String providerUrl) throws NamingException, IOException {
        final String bindPrincipal = _ldapConfiguration.getBindPrincipal();
        final String bindPassword = _ldapConfiguration.getBindPassword();
        return createInitialDirContext(bindPrincipal, bindPassword, providerUrl, true);
    }

    private LdapContext createInitialDirContext(final String principal, final String password, final String providerUrl, final boolean isSystemContext)
            throws NamingException, IOException {
        final Hashtable<String, String> environment = getEnvironment(principal, password, providerUrl, isSystemContext);
        s_logger.debug("initializing ldap with provider url: " + environment.get(Context.PROVIDER_URL));
        return new InitialLdapContext(environment, null);
    }

    private Hashtable<String, String> getEnvironment(final String principal, final String password, final String providerUrl, final boolean isSystemContext) {
        final String factory = _ldapConfiguration.getFactory();
        final String url = providerUrl == null ? _ldapConfiguration.getProviderUrl() : providerUrl;

        final Hashtable<String, String> environment = new Hashtable<>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, factory);
        environment.put(Context.PROVIDER_URL, url);
        environment.put("com.sun.jndi.ldap.read.timeout", _ldapConfiguration.getReadTimeout().toString());
        environment.put("com.sun.jndi.ldap.connect.pool", "true");

        enableSSL(environment);
        setAuthentication(environment, isSystemContext);

        if (principal != null) {
            environment.put(Context.SECURITY_PRINCIPAL, principal);
        }

        if (password != null) {
            environment.put(Context.SECURITY_CREDENTIALS, password);
        }

        return environment;
    }

    private void enableSSL(final Hashtable<String, String> environment) {
        final boolean sslStatus = _ldapConfiguration.getSSLStatus();

        if (sslStatus) {
            s_logger.info("LDAP SSL enabled.");
            environment.put(Context.SECURITY_PROTOCOL, "ssl");
            System.setProperty("javax.net.ssl.trustStore", _ldapConfiguration.getTrustStore());
            System.setProperty("javax.net.ssl.trustStorePassword", _ldapConfiguration.getTrustStorePassword());
        }
    }

    private void setAuthentication(final Hashtable<String, String> environment, final boolean isSystemContext) {
        final String authentication = _ldapConfiguration.getAuthentication();

        if ("none".equals(authentication) && !isSystemContext) {
            environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        } else {
            environment.put(Context.SECURITY_AUTHENTICATION, authentication);
        }
    }

    public LdapContext createUserContext(final String principal, final String password) throws NamingException, IOException {
        return createInitialDirContext(principal, password, false);
    }

    private LdapContext createInitialDirContext(final String principal, final String password, final boolean isSystemContext) throws NamingException, IOException {
        return createInitialDirContext(principal, password, null, isSystemContext);
    }
}
