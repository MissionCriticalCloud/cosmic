package org.apache.cloudstack.api.command;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.LDAPConfigResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;
import org.apache.cloudstack.ldap.LdapConfiguration;
import org.apache.cloudstack.ldap.LdapConfigurationVO;
import org.apache.cloudstack.ldap.LdapManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated as of 4.3 use the new api {@link LdapAddConfigurationCmd}
 */
@Deprecated
@APICommand(name = "ldapConfig", description = "Configure the LDAP context for this site.", responseObject = LDAPConfigResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = true, responseHasSensitiveInfo = false)

public class LDAPConfigCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(LDAPConfigCmd.class.getName());

    private static final String s_name = "ldapconfigresponse";

    @Inject
    private ConfigurationDao _configDao;

    @Inject
    private LdapManager _ldapManager;

    @Inject
    private LdapConfiguration _ldapConfiguration;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.LIST_ALL, type = CommandType.BOOLEAN, description = "If true return current LDAP configuration")
    private Boolean listAll;

    @Parameter(name = ApiConstants.HOST_NAME, type = CommandType.STRING, description = "Hostname or ip address of the ldap server eg: my.ldap.com")
    private String hostname;

    @Parameter(name = ApiConstants.PORT, type = CommandType.INTEGER, description = "Specify the LDAP port if required, default is 389.")
    private Integer port = 0;

    @Parameter(name = ApiConstants.USE_SSL, type = CommandType.BOOLEAN, description = "Check Use SSL if the external LDAP server is configured for LDAP over SSL.")
    private Boolean useSSL;

    @Parameter(name = ApiConstants.SEARCH_BASE,
            type = CommandType.STRING,
            description = "The search base defines the starting point for the search in the directory tree Example:  dc=cloud,dc=com.")
    private String searchBase;

    @Parameter(name = ApiConstants.QUERY_FILTER,
            type = CommandType.STRING,
            description = "You specify a query filter here, which narrows down the users, who can be part of this domain.")
    private String queryFilter;

    @Parameter(name = ApiConstants.BIND_DN,
            type = CommandType.STRING,
            description = "Specify the distinguished name of a user with the search permission on the directory.")
    private String bindDN;

    @Parameter(name = ApiConstants.BIND_PASSWORD, type = CommandType.STRING, description = "Enter the password.")
    private String bindPassword;

    @Parameter(name = ApiConstants.TRUST_STORE, type = CommandType.STRING, description = "Enter the path to trust certificates store.")
    private String trustStore;

    @Parameter(name = ApiConstants.TRUST_STORE_PASSWORD, type = CommandType.STRING, description = "Enter the password for trust store.")
    private String trustStorePassword;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        if (getListAll()) {
            // return the existing conf

            final LdapListConfigurationCmd listConfigurationCmd = new LdapListConfigurationCmd(_ldapManager);
            final Pair<List<? extends LdapConfigurationVO>, Integer> result = _ldapManager.listConfigurations(listConfigurationCmd);
            final ListResponse<LDAPConfigResponse> response = new ListResponse<>();
            final List<LDAPConfigResponse> responses = new ArrayList<>();

            if (result.second() > 0) {
                final boolean useSSlConfig = _ldapConfiguration.getSSLStatus();
                final String searchBaseConfig = _ldapConfiguration.getBaseDn();
                final String bindDnConfig = _ldapConfiguration.getBindPrincipal();
                for (final LdapConfigurationVO ldapConfigurationVO : result.first()) {
                    responses.add(createLDAPConfigResponse(ldapConfigurationVO.getHostname(), ldapConfigurationVO.getPort(), useSSlConfig, null, searchBaseConfig,
                            bindDnConfig));
                }
            }
            response.setResponses(responses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else if (getHostname() == null || getPort() == null) {
            throw new InvalidParameterValueException("You need to provide hostname, port to configure your LDAP server");
        } else {
            final boolean result = updateLDAP();
            if (result) {
                final LDAPConfigResponse lr = createLDAPConfigResponse(getHostname(), getPort(), getUseSSL(), getQueryFilter(), getSearchBase(), getBindDN());
                lr.setResponseName(getCommandName());
                setResponseObject(lr);
            }
        }
    }

    public Boolean getListAll() {
        return listAll == null ? Boolean.FALSE : listAll;
    }

    private LDAPConfigResponse createLDAPConfigResponse(final String hostname, final Integer port, final Boolean useSSL, final String queryFilter, final String searchBase, final
    String bindDN) {
        final LDAPConfigResponse lr = new LDAPConfigResponse();
        lr.setHostname(hostname);
        lr.setPort(port.toString());
        lr.setUseSSL(useSSL.toString());
        lr.setQueryFilter(queryFilter);
        lr.setBindDN(bindDN);
        lr.setSearchBase(searchBase);
        lr.setObjectName("ldapconfig");
        return lr;
    }

    public Integer getPort() {
        return (Integer) (port.intValue() <= 0 ? 389 : port.intValue());
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    private boolean updateLDAP() {
        _ldapManager.addConfiguration(hostname, port);

        /**
         * There is no query filter now. It is derived from ldap.user.object and ldap.search.group.principle
         */
        //        ConfigurationVO cvo = _configDao.findByName(LDAPParams.queryfilter.toString());
        //        _configDao.update(cvo.getName(),cvo.getCategory(),getQueryFilter());

        ConfigurationVO cvo = _configDao.findByName("ldap.basedn");
        _configDao.update(cvo.getName(), cvo.getCategory(), getSearchBase());

        /**
         * There is no ssl now. it is derived from the presence of trust store and password
         */
        //        cvo = _configDao.findByName(LDAPParams.usessl.toString());
        //        _configDao.update(cvo.getName(),cvo.getCategory(),getUseSSL().toString());

        cvo = _configDao.findByName("ldap.bind.principal");
        _configDao.update(cvo.getName(), cvo.getCategory(), getBindDN());

        cvo = _configDao.findByName("ldap.bind.password");
        _configDao.update(cvo.getName(), cvo.getCategory(), getBindPassword());

        cvo = _configDao.findByName("ldap.truststore");
        _configDao.update(cvo.getName(), cvo.getCategory(), getTrustStore());

        cvo = _configDao.findByName("ldap.truststore.password");
        _configDao.update(cvo.getName(), cvo.getCategory(), getTrustStorePassword());

        return true;
    }

    public Boolean getUseSSL() {
        return useSSL == null ? Boolean.FALSE : useSSL;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(final String queryFilter) {
        this.queryFilter = StringEscapeUtils.unescapeHtml(queryFilter);
    }

    public String getSearchBase() {
        return searchBase;
    }

    public String getBindDN() {
        return bindDN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(final String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setBindDN(final String bdn) {
        bindDN = bdn;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
    }

    public void setUseSSL(final Boolean useSSL) {
        this.useSSL = useSSL;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    private List<? extends LdapConfigurationVO> listLDAPConfig() {

        final LdapListConfigurationCmd listConfigurationCmd = new LdapListConfigurationCmd(_ldapManager);
        final Pair<List<? extends LdapConfigurationVO>, Integer> result = _ldapManager.listConfigurations(listConfigurationCmd);
        return result.first();
    }
}
