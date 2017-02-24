package com.cloud.server;

import com.cloud.config.ApiServiceConfiguration;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.Resource;
import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.configuration.ResourceCountVO;
import com.cloud.configuration.dao.ResourceCountDao;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.InternalErrorException;
import com.cloud.framework.config.ConfigDepot;
import com.cloud.framework.config.ConfigDepotAdmin;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.config.impl.ConfigurationVO;
import com.cloud.network.Network;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Network.State;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.guru.ControlNetworkGuru;
import com.cloud.network.guru.DirectPodBasedNetworkGuru;
import com.cloud.network.guru.PodBasedNetworkGuru;
import com.cloud.network.guru.PublicNetworkGuru;
import com.cloud.network.guru.StorageNetworkGuru;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offerings.NetworkOfferingServiceMapVO;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.test.IPRangeConfig;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.PasswordGenerator;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.ComponentLifecycle;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.nio.Link;
import com.cloud.utils.script.Script;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServerImpl extends ManagerBase implements ConfigurationServer {
    public static final Logger s_logger = LoggerFactory.getLogger(ConfigurationServerImpl.class);
    @Inject
    protected ConfigDepotAdmin _configDepotAdmin;
    @Inject
    protected ConfigDepot _configDepot;
    @Inject
    protected ConfigurationManager _configMgr;
    @Inject
    protected ManagementService _mgrService;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private DataCenterDao _zoneDao;
    @Inject
    private HostPodDao _podDao;
    @Inject
    private DiskOfferingDao _diskOfferingDao;
    @Inject
    private ServiceOfferingDao _serviceOfferingDao;
    @Inject
    private NetworkOfferingDao _networkOfferingDao;
    @Inject
    private DataCenterDao _dataCenterDao;
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private VlanDao _vlanDao;
    @Inject
    private DomainDao _domainDao;
    @Inject
    private AccountDao _accountDao;
    @Inject
    private ResourceCountDao _resourceCountDao;
    @Inject
    private NetworkOfferingServiceMapDao _ntwkOfferingServiceMapDao;

    public ConfigurationServerImpl() {
        setRunLevel(ComponentLifecycle.RUN_LEVEL_FRAMEWORK_BOOTSTRAP);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        try {
            persistDefaultValues();
            _configDepotAdmin.populateConfigurations();
        } catch (final InternalErrorException e) {
            throw new RuntimeException("Unhandled configuration exception", e);
        }
        return true;
    }

    @Override
    public void persistDefaultValues() throws InternalErrorException {

        // Create system user and admin user
        saveUser();

        // Get init
        final String init = _configDao.getValue("init");

        if (init == null || init.equals("false")) {
            s_logger.debug("ConfigurationServer is saving default values to the database.");

            // Save default Configuration Table values
            final List<String> categories = Config.getCategories();
            for (final String category : categories) {
                // If this is not a premium environment, don't insert premium configuration values
                if (!_configDao.isPremium() && category.equals("Premium")) {
                    continue;
                }

                final List<Config> configs = Config.getConfigs(category);
                for (final Config c : configs) {
                    final String name = c.key();

                    // if the config value already present in the db, don't insert it again
                    if (_configDao.findByName(name) != null) {
                        continue;
                    }

                    final String instance = "DEFAULT";
                    final String component = c.getComponent();
                    final String value = c.getDefaultValue();
                    final String description = c.getDescription();
                    final ConfigurationVO configVO = new ConfigurationVO(category, instance, component, name, value, description);
                    configVO.setDefaultValue(value);
                    _configDao.persist(configVO);
                }
            }

            _configDao.update(Config.UseSecondaryStorageVm.key(), Config.UseSecondaryStorageVm.getCategory(), "true");
            s_logger.debug("ConfigurationServer made secondary storage vm required.");

            _configDao.update(Config.SecStorageEncryptCopy.key(), Config.SecStorageEncryptCopy.getCategory(), "false");
            s_logger.debug("ConfigurationServer made secondary storage copy encrypt set to false.");

            _configDao.update("secstorage.secure.copy.cert", "realhostip");
            s_logger.debug("ConfigurationServer made secondary storage copy use realhostip.");

            _configDao.update("user.password.encoders.exclude", "MD5,LDAP,PLAINTEXT");
            s_logger.debug("Configuration server excluded insecure encoders");

            _configDao.update("user.authenticators.exclude", "PLAINTEXT");
            s_logger.debug("Configuration server excluded plaintext authenticator");

            // Save default service offerings
            createServiceOffering(User.UID_SYSTEM, "Small Instance", 1, 512, 500, "Small Instance", ProvisioningType.THIN, false, false, null);
            createServiceOffering(User.UID_SYSTEM, "Medium Instance", 1, 1024, 1000, "Medium Instance", ProvisioningType.THIN, false, false, null);
            // Save default disk offerings
            createdefaultDiskOffering(null, "Small", "Small Disk, 5 GB", ProvisioningType.THIN, 5, null, false, false);
            createdefaultDiskOffering(null, "Medium", "Medium Disk, 20 GB", ProvisioningType.THIN, 20, null, false, false);
            createdefaultDiskOffering(null, "Large", "Large Disk, 100 GB", ProvisioningType.THIN, 100, null, false, false);
            createdefaultDiskOffering(null, "Large", "Large Disk, 100 GB", ProvisioningType.THIN, 100, null, false, false);
            createdefaultDiskOffering(null, "Custom", "Custom Disk", ProvisioningType.THIN, 0, null, true, false);

            // Save the mount parent to the configuration table
            final String mountParent = getMountParent();
            if (mountParent != null) {
                _configDao.update(Config.MountParent.key(), Config.MountParent.getCategory(), mountParent);
                s_logger.debug("ConfigurationServer saved \"" + mountParent + "\" as mount.parent.");
            } else {
                s_logger.debug("ConfigurationServer could not detect mount.parent.");
            }

            final String hostIpAdr = NetUtils.getDefaultHostIp();
            boolean needUpdateHostIp = true;
            if (hostIpAdr != null) {
                final Boolean devel = Boolean.valueOf(_configDao.getValue("developer"));
                if (devel) {
                    final String value = _configDao.getValue(ApiServiceConfiguration.ManagementHostIPAdr.key());
                    if (value != null && !value.equals("localhost")) {
                        needUpdateHostIp = false;
                    }
                }

                if (needUpdateHostIp) {
                    _configDepot.createOrUpdateConfigObject(ApiServiceConfiguration.class.getSimpleName(), ApiServiceConfiguration.ManagementHostIPAdr, hostIpAdr);
                    s_logger.debug("ConfigurationServer saved \"" + hostIpAdr + "\" as host.");
                }
            }

            // generate a single sign-on key
            updateSSOKey();

            // Create default network offerings
            createDefaultNetworkOfferings();

            // Create default networks
            createDefaultNetworks();

            // Create userIpAddress ranges

            // Update existing vlans with networkId
            final List<VlanVO> vlans = _vlanDao.listAll();
            if (vlans != null && !vlans.isEmpty()) {
                for (final VlanVO vlan : vlans) {
                    if (vlan.getNetworkId().longValue() == 0) {
                        updateVlanWithNetworkId(vlan);
                    }

                    // Create vlan user_ip_address range
                    final String ipPange = vlan.getIpRange();
                    final String[] range = ipPange.split("-");
                    final String startIp = range[0];
                    final String endIp = range[1];

                    Transaction.execute(new TransactionCallbackNoReturn() {
                        @Override
                        public void doInTransactionWithoutResult(final TransactionStatus status) {
                            final IPRangeConfig config = new IPRangeConfig();
                            final long startIPLong = NetUtils.ip2Long(startIp);
                            final long endIPLong = NetUtils.ip2Long(endIp);
                            config.savePublicIPRange(TransactionLegacy.currentTxn(), startIPLong, endIPLong, vlan.getDataCenterId(), vlan.getId(), vlan.getNetworkId(),
                                    vlan.getPhysicalNetworkId());
                        }
                    });
                }
            }
        }
        // Update resource count if needed
        updateResourceCount();

        // keystore for SSL/TLS connection
        updateSSLKeystore();

        // store the public and private keys in the database
        updateKeyPairs();

        // generate a PSK to communicate with SSVM
        updateSecondaryStorageVMSharedKey();

        // generate a random password for system vm
        updateSystemvmPassword();

        // generate a random password used to authenticate zone-to-zone copy
        generateSecStorageVmCopyPassword();

        // Update the cloud identifier
        updateCloudIdentifier();

        _configDepotAdmin.populateConfigurations();
        // setup XenServer default PV driver version
        initiateXenServerPVDriverVersion();

        // We should not update seed data UUID column here since this will be invoked in upgrade case as well.
        //updateUuids();
        // Set init to true
        _configDao.update("init", "Hidden", "true");

        // invalidate cache in DAO as we have changed DB status
        _configDao.invalidateCache();
    }

    @DB
    public void saveUser() {
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                final TransactionLegacy txn = TransactionLegacy.currentTxn();
                // insert system account
                String insertSql = "INSERT INTO `cloud`.`account` (id, uuid, account_name, type, domain_id, account.default) VALUES (1, UUID(), 'system', '1', '1', 1)";

                try {
                    final PreparedStatement stmt = txn.prepareAutoCloseStatement(insertSql);
                    stmt.executeUpdate();
                } catch (final SQLException ex) {
                    s_logger.debug("Looks like system account already exists");
                }
                // insert system user
                insertSql = "INSERT INTO `cloud`.`user` (id, uuid, username, password, account_id, firstname, lastname, created, user.default)"
                        + " VALUES (1, UUID(), 'system', RAND(), 1, 'system', 'cloud', now(), 1)";

                try {
                    final PreparedStatement stmt = txn.prepareAutoCloseStatement(insertSql);
                    stmt.executeUpdate();
                } catch (final SQLException ex) {
                    s_logger.debug("Looks like system user already exists");
                }

                // insert admin user, but leave the account disabled until we set a
                // password with the user authenticator
                final long id = 2;
                final String username = "admin";
                final String firstname = "admin";
                final String lastname = "cloud";

                // create an account for the admin user first
                insertSql = "INSERT INTO `cloud`.`account` (id, uuid, account_name, type, domain_id, account.default) VALUES (" + id + ", UUID(), '" + username
                        + "', '1', '1', 1)";
                try {
                    final PreparedStatement stmt = txn.prepareAutoCloseStatement(insertSql);
                    stmt.executeUpdate();
                } catch (final SQLException ex) {
                    s_logger.debug("Looks like admin account already exists");
                }

                // now insert the user
                insertSql = "INSERT INTO `cloud`.`user` (id, uuid, username, password, account_id, firstname, lastname, created, state, user.default) " + "VALUES (" + id
                        + ", UUID(), '" + username + "', RAND(), 2, '" + firstname + "','" + lastname + "',now(), 'disabled', 1)";

                try {
                    final PreparedStatement stmt = txn.prepareAutoCloseStatement(insertSql);
                    stmt.executeUpdate();
                } catch (final SQLException ex) {
                    s_logger.debug("Looks like admin user already exists");
                }

                try {
                    String tableName = "security_group";
                    try {
                        final String checkSql = "SELECT * from network_group";
                        final PreparedStatement stmt = txn.prepareAutoCloseStatement(checkSql);
                        stmt.executeQuery();
                        tableName = "network_group";
                    } catch (final Exception ex) {
                        // Ignore in case of exception, table must not exist
                    }

                    insertSql = "SELECT * FROM " + tableName + " where account_id=2 and name='default'";
                    PreparedStatement stmt = txn.prepareAutoCloseStatement(insertSql);
                    final ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        // save default security group
                        if (tableName.equals("security_group")) {
                            insertSql = "INSERT INTO " + tableName + " (uuid, name, description, account_id, domain_id) "
                                    + "VALUES (UUID(), 'default', 'Default Security Group', 2, 1)";
                        } else {
                            insertSql = "INSERT INTO " + tableName + " (name, description, account_id, domain_id, account_name) "
                                    + "VALUES ('default', 'Default Security Group', 2, 1, 'admin')";
                        }

                        try {
                            stmt = txn.prepareAutoCloseStatement(insertSql);
                            stmt.executeUpdate();
                        } catch (final SQLException ex) {
                            s_logger.warn("Failed to create default security group for default admin account due to ", ex);
                        }
                    }
                    rs.close();
                } catch (final Exception ex) {
                    s_logger.warn("Failed to create default security group for default admin account due to ", ex);
                }
            }
        });
    }

    private ServiceOfferingVO createServiceOffering(final long userId, final String name, final int cpu, final int ramSize, final int speed, final String displayText,
                                                    final ProvisioningType provisioningType, final boolean localStorageRequired, final boolean offerHA, String tags) {
        tags = cleanupTags(tags);
        ServiceOfferingVO offering =
                new ServiceOfferingVO(name, cpu, ramSize, speed, null, null, offerHA, displayText, provisioningType, localStorageRequired, false, tags, false, null, false);
        offering.setUniqueName("Cloud.Com-" + name);
        offering = _serviceOfferingDao.persistSystemServiceOffering(offering);
        return offering;
    }

    private DiskOfferingVO createdefaultDiskOffering(final Long domainId, final String name, final String description, final ProvisioningType provisioningType,
                                                     final int numGibibytes, String tags, final boolean isCustomized, final boolean isSystemUse) {
        long diskSize = numGibibytes;
        diskSize = diskSize * 1024 * 1024 * 1024;
        tags = cleanupTags(tags);

        DiskOfferingVO newDiskOffering = new DiskOfferingVO(domainId, name, description, provisioningType, diskSize, tags, isCustomized, null, null, null);
        newDiskOffering.setUniqueName("Cloud.Com-" + name);
        newDiskOffering.setSystemUse(isSystemUse);
        newDiskOffering = _diskOfferingDao.persistDeafultDiskOffering(newDiskOffering);
        return newDiskOffering;
    }

    private String getMountParent() {
        return getEnvironmentProperty("mount.parent");
    }

    private void updateSSOKey() {
        try {
            _configDao.update(Config.SSOKey.key(), Config.SSOKey.getCategory(), getPrivateKey());
        } catch (final NoSuchAlgorithmException ex) {
            s_logger.error("error generating sso key", ex);
        }
    }

    @DB
    protected void createDefaultNetworkOfferings() {

        final NetworkOfferingVO publicNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemPublicNetwork, TrafficType.Public, true);
        _networkOfferingDao.persistDefaultNetworkOffering(publicNetworkOffering);
        final NetworkOfferingVO managementNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemManagementNetwork, TrafficType.Management, false);
        _networkOfferingDao.persistDefaultNetworkOffering(managementNetworkOffering);
        final NetworkOfferingVO controlNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemControlNetwork, TrafficType.Control, false);
        _networkOfferingDao.persistDefaultNetworkOffering(controlNetworkOffering);
        final NetworkOfferingVO storageNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemStorageNetwork, TrafficType.Storage, true);
        _networkOfferingDao.persistDefaultNetworkOffering(storageNetworkOffering);
        final NetworkOfferingVO privateGatewayNetworkOffering = new NetworkOfferingVO(NetworkOffering.DefaultPrivateGatewayNetworkOffering, GuestType.Private, false);
        _networkOfferingDao.persistDefaultNetworkOffering(privateGatewayNetworkOffering);
        final NetworkOfferingVO privateGatewayNetworkOfferingSpecifyVlan =
                new NetworkOfferingVO(NetworkOffering.DefaultPrivateGatewayNetworkOfferingSpecifyVlan, GuestType.Private, true);
        _networkOfferingDao.persistDefaultNetworkOffering(privateGatewayNetworkOfferingSpecifyVlan);

        //populate providers
        final Map<Network.Service, Network.Provider> defaultSharedNetworkOfferingProviders = new HashMap<>();
        defaultSharedNetworkOfferingProviders.put(Service.Dhcp, Provider.VirtualRouter);
        defaultSharedNetworkOfferingProviders.put(Service.Dns, Provider.VirtualRouter);
        defaultSharedNetworkOfferingProviders.put(Service.UserData, Provider.VirtualRouter);

        final Map<Network.Service, Network.Provider> defaultIsolatedNetworkOfferingProviders = defaultSharedNetworkOfferingProviders;

        final Map<Network.Service, Network.Provider> defaultSharedSGNetworkOfferingProviders = new HashMap<>();
        defaultSharedSGNetworkOfferingProviders.put(Service.Dhcp, Provider.VirtualRouter);
        defaultSharedSGNetworkOfferingProviders.put(Service.Dns, Provider.VirtualRouter);
        defaultSharedSGNetworkOfferingProviders.put(Service.UserData, Provider.VirtualRouter);
        defaultSharedSGNetworkOfferingProviders.put(Service.SecurityGroup, Provider.SecurityGroupProvider);

        final Map<Network.Service, Network.Provider> defaultIsolatedSourceNatEnabledNetworkOfferingProviders = new HashMap<>();
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.Dhcp, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.Dns, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.UserData, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.Firewall, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.Gateway, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.Lb, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.SourceNat, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.StaticNat, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.PortForwarding, Provider.VirtualRouter);
        defaultIsolatedSourceNatEnabledNetworkOfferingProviders.put(Service.Vpn, Provider.VirtualRouter);

        // The only one diff between 1 and 2 network offerings is that the first one has SG enabled. In Basic zone only
        // first network offering has to be enabled, in Advance zone - the second one
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                // Offering #1
                NetworkOfferingVO defaultSharedSGNetworkOffering =
                        new NetworkOfferingVO(NetworkOffering.DefaultSharedNetworkOfferingWithSGService, "Offering for Shared Security group enabled networks",
                                TrafficType.Guest, false, true, null, null, true, Availability.Optional, null, Network.GuestType.Shared, true, true, false, false, false);

                defaultSharedSGNetworkOffering.setState(NetworkOffering.State.Enabled);
                defaultSharedSGNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(defaultSharedSGNetworkOffering);

                for (final Service service : defaultSharedSGNetworkOfferingProviders.keySet()) {
                    final NetworkOfferingServiceMapVO offService =
                            new NetworkOfferingServiceMapVO(defaultSharedSGNetworkOffering.getId(), service, defaultSharedSGNetworkOfferingProviders.get(service));
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }

                // Offering #2
                NetworkOfferingVO defaultSharedNetworkOffering =
                        new NetworkOfferingVO(NetworkOffering.DefaultSharedNetworkOffering, "Offering for Shared networks", TrafficType.Guest, false, true, null, null, true,
                                Availability.Optional, null, Network.GuestType.Shared, true, true, false, false, false);

                defaultSharedNetworkOffering.setState(NetworkOffering.State.Enabled);
                defaultSharedNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(defaultSharedNetworkOffering);

                for (final Service service : defaultSharedNetworkOfferingProviders.keySet()) {
                    final NetworkOfferingServiceMapVO offService =
                            new NetworkOfferingServiceMapVO(defaultSharedNetworkOffering.getId(), service, defaultSharedNetworkOfferingProviders.get(service));
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }

                // Offering #3
                NetworkOfferingVO defaultIsolatedSourceNatEnabledNetworkOffering =
                        new NetworkOfferingVO(NetworkOffering.DefaultIsolatedNetworkOfferingWithSourceNatService,
                                "Offering for Isolated networks with Source Nat service enabled", TrafficType.Guest, false, false, null, null, true, Availability.Required, null,
                                Network.GuestType.Isolated, true, false, false, false, true);

                defaultIsolatedSourceNatEnabledNetworkOffering.setState(NetworkOffering.State.Enabled);
                defaultIsolatedSourceNatEnabledNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(defaultIsolatedSourceNatEnabledNetworkOffering);

                for (final Service service : defaultIsolatedSourceNatEnabledNetworkOfferingProviders.keySet()) {
                    final NetworkOfferingServiceMapVO offService =
                            new NetworkOfferingServiceMapVO(defaultIsolatedSourceNatEnabledNetworkOffering.getId(), service,
                                    defaultIsolatedSourceNatEnabledNetworkOfferingProviders.get(service));
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }

                // Offering #4
                NetworkOfferingVO defaultIsolatedEnabledNetworkOffering =
                        new NetworkOfferingVO(NetworkOffering.DefaultIsolatedNetworkOffering, "Offering for Isolated networks with no Source Nat service", TrafficType.Guest,
                                false, true, null, null, true, Availability.Optional, null, Network.GuestType.Isolated, true, true, false, false, false);

                defaultIsolatedEnabledNetworkOffering.setState(NetworkOffering.State.Enabled);
                defaultIsolatedEnabledNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(defaultIsolatedEnabledNetworkOffering);

                for (final Service service : defaultIsolatedNetworkOfferingProviders.keySet()) {
                    final NetworkOfferingServiceMapVO offService =
                            new NetworkOfferingServiceMapVO(defaultIsolatedEnabledNetworkOffering.getId(), service, defaultIsolatedNetworkOfferingProviders.get(service));
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }

                // Offering #5
                NetworkOfferingVO defaultNetworkOfferingForVpcNetworks =
                        new NetworkOfferingVO(NetworkOffering.DefaultIsolatedNetworkOfferingForVpcNetworks,
                                "Offering for Isolated Vpc networks with Source Nat service enabled", TrafficType.Guest, false, false, null, null, true, Availability.Optional,
                                null, Network.GuestType.Isolated, false, false, false, false, true);

                defaultNetworkOfferingForVpcNetworks.setState(NetworkOffering.State.Enabled);
                defaultNetworkOfferingForVpcNetworks = _networkOfferingDao.persistDefaultNetworkOffering(defaultNetworkOfferingForVpcNetworks);

                final Map<Network.Service, Network.Provider> defaultVpcNetworkOfferingProviders = new HashMap<>();
                defaultVpcNetworkOfferingProviders.put(Service.Dhcp, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.Dns, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.UserData, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.NetworkACL, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.Gateway, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.Lb, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.SourceNat, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.StaticNat, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.PortForwarding, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProviders.put(Service.Vpn, Provider.VPCVirtualRouter);

                for (final Map.Entry<Service, Provider> entry : defaultVpcNetworkOfferingProviders.entrySet()) {
                    final NetworkOfferingServiceMapVO offService =
                            new NetworkOfferingServiceMapVO(defaultNetworkOfferingForVpcNetworks.getId(), entry.getKey(), entry.getValue());
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }

                // Offering #6
                NetworkOfferingVO defaultNetworkOfferingForVpcNetworksNoLB =
                        new NetworkOfferingVO(NetworkOffering.DefaultIsolatedNetworkOfferingForVpcNetworksNoLB,
                                "Offering for Isolated Vpc networks with Source Nat service enabled and LB service Disabled", TrafficType.Guest, false, false, null, null, true,
                                Availability.Optional, null, Network.GuestType.Isolated, false, false, false, false, false);

                defaultNetworkOfferingForVpcNetworksNoLB.setState(NetworkOffering.State.Enabled);
                defaultNetworkOfferingForVpcNetworksNoLB = _networkOfferingDao.persistDefaultNetworkOffering(defaultNetworkOfferingForVpcNetworksNoLB);

                final Map<Network.Service, Network.Provider> defaultVpcNetworkOfferingProvidersNoLB = new HashMap<>();
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.Dhcp, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.Dns, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.UserData, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.NetworkACL, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.Gateway, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.SourceNat, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.StaticNat, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.PortForwarding, Provider.VPCVirtualRouter);
                defaultVpcNetworkOfferingProvidersNoLB.put(Service.Vpn, Provider.VPCVirtualRouter);

                for (final Map.Entry<Service, Provider> entry : defaultVpcNetworkOfferingProvidersNoLB.entrySet()) {
                    final NetworkOfferingServiceMapVO offService =
                            new NetworkOfferingServiceMapVO(defaultNetworkOfferingForVpcNetworksNoLB.getId(), entry.getKey(), entry.getValue());
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }

                //offering #7 - network offering with internal lb service
                NetworkOfferingVO internalLbOff =
                        new NetworkOfferingVO(NetworkOffering.DefaultIsolatedNetworkOfferingForVpcNetworksWithInternalLB,
                                "Offering for Isolated Vpc networks with Internal LB support", TrafficType.Guest, false, false, null, null, true, Availability.Optional, null,
                                Network.GuestType.Isolated, false, false, false, true, false);

                internalLbOff.setState(NetworkOffering.State.Enabled);
                internalLbOff = _networkOfferingDao.persistDefaultNetworkOffering(internalLbOff);

                final Map<Network.Service, Network.Provider> internalLbOffProviders = new HashMap<>();
                internalLbOffProviders.put(Service.Dhcp, Provider.VPCVirtualRouter);
                internalLbOffProviders.put(Service.Dns, Provider.VPCVirtualRouter);
                internalLbOffProviders.put(Service.UserData, Provider.VPCVirtualRouter);
                internalLbOffProviders.put(Service.NetworkACL, Provider.VPCVirtualRouter);
                internalLbOffProviders.put(Service.Gateway, Provider.VPCVirtualRouter);
                internalLbOffProviders.put(Service.Lb, Provider.InternalLbVm);
                internalLbOffProviders.put(Service.SourceNat, Provider.VPCVirtualRouter);

                for (final Service service : internalLbOffProviders.keySet()) {
                    final NetworkOfferingServiceMapVO offService = new NetworkOfferingServiceMapVO(internalLbOff.getId(), service, internalLbOffProviders.get(service));
                    _ntwkOfferingServiceMapDao.persist(offService);
                    s_logger.trace("Added service for the network offering: " + offService);
                }
            }
        });
    }

    private void createDefaultNetworks() {
        final List<DataCenterVO> zones = _dataCenterDao.listAll();
        long id = 1;

        final HashMap<TrafficType, String> guruNames = new HashMap<>();
        guruNames.put(TrafficType.Public, PublicNetworkGuru.class.getSimpleName());
        guruNames.put(TrafficType.Management, PodBasedNetworkGuru.class.getSimpleName());
        guruNames.put(TrafficType.Control, ControlNetworkGuru.class.getSimpleName());
        guruNames.put(TrafficType.Storage, StorageNetworkGuru.class.getSimpleName());
        guruNames.put(TrafficType.Guest, DirectPodBasedNetworkGuru.class.getSimpleName());

        for (final DataCenterVO zone : zones) {
            final long zoneId = zone.getId();
            final long accountId = 1L;
            Long domainId = zone.getDomainId();

            if (domainId == null) {
                domainId = 1L;
            }
            // Create default networks - system only
            final List<NetworkOfferingVO> ntwkOff = _networkOfferingDao.listSystemNetworkOfferings();

            for (final NetworkOfferingVO offering : ntwkOff) {
                if (offering.isSystemOnly()) {
                    final long related = id;
                    final long networkOfferingId = offering.getId();
                    final Mode mode = Mode.Static;
                    final String networkDomain = null;

                    BroadcastDomainType broadcastDomainType = null;
                    final TrafficType trafficType = offering.getTrafficType();

                    boolean specifyIpRanges = false;

                    if (trafficType == TrafficType.Management) {
                        broadcastDomainType = BroadcastDomainType.Native;
                    } else if (trafficType == TrafficType.Storage) {
                        broadcastDomainType = BroadcastDomainType.Native;
                        specifyIpRanges = true;
                    } else if (trafficType == TrafficType.Control) {
                        broadcastDomainType = BroadcastDomainType.LinkLocal;
                    } else if (offering.getTrafficType() == TrafficType.Public) {
                        if ((zone.getNetworkType() == NetworkType.Advanced && !zone.isSecurityGroupEnabled()) || zone.getNetworkType() == NetworkType.Basic) {
                            specifyIpRanges = true;
                            broadcastDomainType = BroadcastDomainType.Vlan;
                        } else {
                            continue;
                        }
                    }

                    if (broadcastDomainType != null) {
                        final NetworkVO network =
                                new NetworkVO(id, trafficType, mode, broadcastDomainType, networkOfferingId, domainId, accountId, related, null, null, networkDomain,
                                        Network.GuestType.Shared, zoneId, null, null, specifyIpRanges, null, offering.getRedundantRouter(),
                                        zone.getDns1(), zone.getDns2(), null);
                        network.setGuruName(guruNames.get(network.getTrafficType()));
                        network.setState(State.Implemented);
                        _networkDao.persist(network, false, getServicesAndProvidersForNetwork(networkOfferingId));
                        id++;
                    }
                }
            }
        }
    }

    private void updateVlanWithNetworkId(final VlanVO vlan) {
        final long zoneId = vlan.getDataCenterId();
        long networkId = 0L;
        final DataCenterVO zone = _zoneDao.findById(zoneId);

        if (zone.getNetworkType() == NetworkType.Advanced) {
            networkId = getSystemNetworkIdByZoneAndTrafficType(zoneId, TrafficType.Public);
        } else {
            networkId = getSystemNetworkIdByZoneAndTrafficType(zoneId, TrafficType.Guest);
        }

        vlan.setNetworkId(networkId);
        _vlanDao.update(vlan.getId(), vlan);
    }

    @DB
    public void updateResourceCount() {
        final ResourceType[] resourceTypes = Resource.ResourceType.values();
        final List<AccountVO> accounts = _accountDao.listAll();
        final List<DomainVO> domains = _domainDao.listAll();
        final List<ResourceCountVO> domainResourceCount = _resourceCountDao.listResourceCountByOwnerType(ResourceOwnerType.Domain);
        final List<ResourceCountVO> accountResourceCount = _resourceCountDao.listResourceCountByOwnerType(ResourceOwnerType.Account);

        final List<ResourceType> accountSupportedResourceTypes = new ArrayList<>();
        final List<ResourceType> domainSupportedResourceTypes = new ArrayList<>();

        for (final ResourceType resourceType : resourceTypes) {
            if (resourceType.supportsOwner(ResourceOwnerType.Account)) {
                accountSupportedResourceTypes.add(resourceType);
            }
            if (resourceType.supportsOwner(ResourceOwnerType.Domain)) {
                domainSupportedResourceTypes.add(resourceType);
            }
        }

        final int accountExpectedCount = accountSupportedResourceTypes.size();
        final int domainExpectedCount = domainSupportedResourceTypes.size();

        if ((domainResourceCount.size() < domainExpectedCount * domains.size())) {
            s_logger.debug("resource_count table has records missing for some domains...going to insert them");
            for (final DomainVO domain : domains) {
                // Lock domain
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        _domainDao.lockRow(domain.getId(), true);
                        final List<ResourceCountVO> domainCounts = _resourceCountDao.listByOwnerId(domain.getId(), ResourceOwnerType.Domain);
                        final List<String> domainCountStr = new ArrayList<>();
                        for (final ResourceCountVO domainCount : domainCounts) {
                            domainCountStr.add(domainCount.getType().toString());
                        }

                        if (domainCountStr.size() < domainExpectedCount) {
                            for (final ResourceType resourceType : domainSupportedResourceTypes) {
                                if (!domainCountStr.contains(resourceType.toString())) {
                                    final ResourceCountVO resourceCountVO = new ResourceCountVO(resourceType, 0, domain.getId(), ResourceOwnerType.Domain);
                                    s_logger.debug("Inserting resource count of type " + resourceType + " for domain id=" + domain.getId());
                                    _resourceCountDao.persist(resourceCountVO);
                                }
                            }
                        }
                    }
                });
            }
        }

        if ((accountResourceCount.size() < accountExpectedCount * accounts.size())) {
            s_logger.debug("resource_count table has records missing for some accounts...going to insert them");
            for (final AccountVO account : accounts) {
                // lock account
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        _accountDao.lockRow(account.getId(), true);
                        final List<ResourceCountVO> accountCounts = _resourceCountDao.listByOwnerId(account.getId(), ResourceOwnerType.Account);
                        final List<String> accountCountStr = new ArrayList<>();
                        for (final ResourceCountVO accountCount : accountCounts) {
                            accountCountStr.add(accountCount.getType().toString());
                        }

                        if (accountCountStr.size() < accountExpectedCount) {
                            for (final ResourceType resourceType : accountSupportedResourceTypes) {
                                if (!accountCountStr.contains(resourceType.toString())) {
                                    final ResourceCountVO resourceCountVO = new ResourceCountVO(resourceType, 0, account.getId(), ResourceOwnerType.Account);
                                    s_logger.debug("Inserting resource count of type " + resourceType + " for account id=" + account.getId());
                                    _resourceCountDao.persist(resourceCountVO);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    protected void updateSSLKeystore() {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Processing updateSSLKeyStore");
        }

        final String dbString = _configDao.getValue("ssl.keystore");

        final File confFile = PropertiesUtil.findConfigFile("db.properties");
        String confPath = null;
        String keystorePath = null;
        File keystoreFile = null;

        if (null != confFile) {
            confPath = confFile.getParent();
            keystorePath = confPath + Link.keystoreFile;
            keystoreFile = new File(keystorePath);
        }

        final boolean dbExisted = (dbString != null && !dbString.isEmpty());

        s_logger.info("SSL keystore located at " + keystorePath);
        try {
            if (!dbExisted && null != confFile) {
                if (!keystoreFile.exists()) {
                    generateDefaultKeystore(keystorePath);
                    s_logger.info("Generated SSL keystore.");
                }
                final String base64Keystore = getBase64Keystore(keystorePath);
                final ConfigurationVO configVO =
                        new ConfigurationVO("Hidden", "DEFAULT", "management-server", "ssl.keystore", base64Keystore,
                                "SSL Keystore for the management servers");
                _configDao.persist(configVO);
                s_logger.info("Stored SSL keystore to database.");
            } else { // !keystoreFile.exists() and dbExisted
                // Export keystore to local file
                final byte[] storeBytes = Base64.decodeBase64(dbString);
                final String tmpKeystorePath = "/tmp/tmpkey";
                try (
                        FileOutputStream fo = new FileOutputStream(tmpKeystorePath)
                ) {
                    fo.write(storeBytes);
                    final Script script = new Script(true, "cp", 5000, null);
                    script.add("-f");
                    script.add(tmpKeystorePath);

                    //There is a chance, although small, that the keystorePath is null. In that case, do not add it to the script.
                    if (null != keystorePath) {
                        script.add(keystorePath);
                    }
                    final String result = script.execute();
                    if (result != null) {
                        throw new IOException();
                    }
                } catch (final Exception e) {
                    throw new IOException("Fail to create keystore file!", e);
                }
                s_logger.info("Stored database keystore to local.");
            }
        } catch (final Exception ex) {
            s_logger.warn("Would use fail-safe keystore to continue.", ex);
        }
    }

    /**
     * preshared key to be used by management server to communicate with SSVM during volume/template upload
     */
    private void updateSecondaryStorageVMSharedKey() {
        try {
            final ConfigurationVO configInDB = _configDao.findByName(Config.SSVMPSK.key());
            if (configInDB == null) {
                final ConfigurationVO configVO = new ConfigurationVO(Config.SSVMPSK.getCategory(), "DEFAULT", Config.SSVMPSK.getComponent(), Config.SSVMPSK.key(), getPrivateKey(),
                        Config.SSVMPSK.getDescription());
                s_logger.info("generating a new SSVM PSK. This goes to SSVM on Start");
                _configDao.persist(configVO);
            } else if (StringUtils.isEmpty(configInDB.getValue())) {
                s_logger.info("updating the SSVM PSK with new value. This goes to SSVM on Start");
                _configDao.update(Config.SSVMPSK.key(), Config.SSVMPSK.getCategory(), getPrivateKey());
            }
        } catch (final NoSuchAlgorithmException ex) {
            s_logger.error("error generating ssvm psk", ex);
        }
    }

    @DB
    protected void updateSystemvmPassword() {
        final String userid = System.getProperty("user.name");
        if (!userid.startsWith("cloud")) {
            return;
        }

        if (!Boolean.valueOf(_configDao.getValue("system.vm.random.password"))) {
            return;
        }

        final String already = _configDao.getValue("system.vm.password");
        if (already == null) {
            final TransactionLegacy txn = TransactionLegacy.currentTxn();
            try {
                final String rpassword = _mgrService.generateRandomPassword();
                final String wSql = "INSERT INTO `cloud`.`configuration` (category, instance, component, name, value, description) "
                        + "VALUES ('Secure','DEFAULT', 'management-server','system.vm.password', ?,'randmon password generated each management server starts for system vm')";
                final PreparedStatement stmt = txn.prepareAutoCloseStatement(wSql);
                stmt.setString(1, DBEncryptionUtil.encrypt(rpassword));
                stmt.executeUpdate();
                s_logger.info("Updated systemvm password in database");
            } catch (final SQLException e) {
                s_logger.error("Cannot retrieve systemvm password", e);
            }
        }
    }

    @DB
    protected void generateSecStorageVmCopyPassword() {
        final String already = _configDao.getValue("secstorage.copy.password");

        if (already == null) {

            s_logger.info("Need to store secondary storage vm copy password in the database");
            final String password = PasswordGenerator.generateRandomPassword(12);

            final String insertSql1 =
                    "INSERT INTO `cloud`.`configuration` (category, instance, component, name, value, description) " +
                            "VALUES ('Hidden','DEFAULT', 'management-server','secstorage.copy.password', '" + DBEncryptionUtil.encrypt(password) +
                            "','Password used to authenticate zone-to-zone template copy requests')";
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {

                    final TransactionLegacy txn = TransactionLegacy.currentTxn();
                    try {
                        final PreparedStatement stmt1 = txn.prepareAutoCloseStatement(insertSql1);
                        stmt1.executeUpdate();
                        s_logger.debug("secondary storage vm copy password inserted into database");
                    } catch (final SQLException ex) {
                        s_logger.warn("Failed to insert secondary storage vm copy password", ex);
                    }
                }
            });
        }
    }

    protected void updateCloudIdentifier() {
        // Creates and saves a UUID as the cloud identifier
        final String currentCloudIdentifier = _configDao.getValue("cloud.identifier");
        if (currentCloudIdentifier == null || currentCloudIdentifier.isEmpty()) {
            final String uuid = UUID.randomUUID().toString();
            _configDao.update(Config.CloudIdentifier.key(), Config.CloudIdentifier.getCategory(), uuid);
        }
    }

    private void initiateXenServerPVDriverVersion() {
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                final TransactionLegacy txn = TransactionLegacy.currentTxn();
                String pvdriverversion = Config.XenServerPVdriverVersion.getDefaultValue();
                PreparedStatement pstmt = null;
                ResultSet rs1 = null;
                ResultSet rs2 = null;
                try {
                    final String oldValue = _configDao.getValue(Config.XenServerPVdriverVersion.key());
                    if (oldValue == null) {
                        String sql = "select resource from host where hypervisor_type='XenServer' and removed is null and status not in ('Error', 'Removed') group by resource";
                        pstmt = txn.prepareAutoCloseStatement(sql);
                        rs1 = pstmt.executeQuery();
                        while (rs1.next()) {
                            final String resouce = rs1.getString(1); //resource column
                            if (resouce == null) {
                                continue;
                            }
                            if (resouce.equalsIgnoreCase("com.cloud.hypervisor.xenserver.resource.XenServer56Resource")
                                    || resouce.equalsIgnoreCase("com.cloud.hypervisor.xenserver.resource.XenServer56FP1Resource")
                                    || resouce.equalsIgnoreCase("com.cloud.hypervisor.xenserver.resource.XenServer56SP2Resource")
                                    || resouce.equalsIgnoreCase("com.cloud.hypervisor.xenserver.resource.XenServer600Resource")
                                    || resouce.equalsIgnoreCase("com.cloud.hypervisor.xenserver.resource.XenServer602Resource")) {
                                pvdriverversion = "xenserver56";
                                break;
                            }
                        }
                        _configDao.getValueAndInitIfNotExist(Config.XenServerPVdriverVersion.key(), Config.XenServerPVdriverVersion.getCategory(), pvdriverversion,
                                Config.XenServerPVdriverVersion.getDescription());
                        sql = "select id from vm_template where hypervisor_type='XenServer'  and format!='ISO' and removed is null";
                        pstmt = txn.prepareAutoCloseStatement(sql);
                        rs2 = pstmt.executeQuery();
                        final List<Long> tmpl_ids = new ArrayList<>();
                        while (rs2.next()) {
                            tmpl_ids.add(rs2.getLong(1));
                        }
                        for (final Long tmpl_id : tmpl_ids) {
                            templateDetailsInitIfNotExist(tmpl_id, "hypervisortoolsversion", pvdriverversion);
                        }
                    }
                } catch (final Exception e) {
                    s_logger.debug("initiateXenServerPVDriverVersion failed due to " + e.toString());
                    // ignore
                }
            }
        });
    }

    private String cleanupTags(String tags) {
        if (tags != null) {
            final String[] tokens = tags.split(",");
            final StringBuilder t = new StringBuilder();
            for (int i = 0; i < tokens.length; i++) {
                t.append(tokens[i].trim()).append(",");
            }
            t.delete(t.length() - 1, t.length());
            tags = t.toString();
        }

        return tags;
    }

    private String getEnvironmentProperty(final String name) {
        try {
            final File propsFile = PropertiesUtil.findConfigFile("environment.properties");

            if (propsFile == null) {
                return null;
            } else {
                final Properties props = new Properties();
                try (final FileInputStream finputstream = new FileInputStream(propsFile)) {
                    props.load(finputstream);
                } catch (final IOException e) {
                    s_logger.error("getEnvironmentProperty:Exception:" + e.getMessage());
                }
                return props.getProperty("mount.parent");
            }
        } catch (final Exception e) {
            return null;
        }
    }

    private String getPrivateKey() throws NoSuchAlgorithmException {
        String encodedKey = null;
        // Algorithm for generating Key is SHA1, should this be configurable?
        final KeyGenerator generator = KeyGenerator.getInstance("HmacSHA1");
        final SecretKey key = generator.generateKey();
        encodedKey = Base64.encodeBase64URLSafeString(key.getEncoded());
        return encodedKey;
    }

    public Map<String, String> getServicesAndProvidersForNetwork(final long networkOfferingId) {
        final Map<String, String> svcProviders = new HashMap<>();
        final List<NetworkOfferingServiceMapVO> servicesMap = _ntwkOfferingServiceMapDao.listByNetworkOfferingId(networkOfferingId);

        for (final NetworkOfferingServiceMapVO serviceMap : servicesMap) {
            if (svcProviders.containsKey(serviceMap.getService())) {
                continue;
            }
            svcProviders.put(serviceMap.getService(), serviceMap.getProvider());
        }

        return svcProviders;
    }

    private long getSystemNetworkIdByZoneAndTrafficType(final long zoneId, final TrafficType trafficType) {
        // find system public network offering
        Long networkOfferingId = null;
        final List<NetworkOfferingVO> offerings = _networkOfferingDao.listSystemNetworkOfferings();
        for (final NetworkOfferingVO offering : offerings) {
            if (offering.getTrafficType() == trafficType) {
                networkOfferingId = offering.getId();
                break;
            }
        }

        if (networkOfferingId == null) {
            throw new InvalidParameterValueException("Unable to find system network offering with traffic type " + trafficType);
        }

        final List<NetworkVO> networks = _networkDao.listBy(Account.ACCOUNT_ID_SYSTEM, networkOfferingId, zoneId);
        if (networks == null || networks.isEmpty()) {
            throw new InvalidParameterValueException("Unable to find network with traffic type " + trafficType + " in zone " + zoneId);
        }
        return networks.get(0).getId();
    }

    private void generateDefaultKeystore(final String keystorePath) throws IOException {
        final String cn = "Cloudstack User";
        String ou;

        try {
            ou = InetAddress.getLocalHost().getCanonicalHostName();
            final String[] group = ou.split("\\.");

            // Simple check to see if we got IP Address...
            final boolean isIPAddress = Pattern.matches("[0-9]$", group[group.length - 1]);
            if (isIPAddress) {
                ou = "cloud.com";
            } else {
                ou = group[group.length - 1];
                for (int i = group.length - 2; i >= 0 && i >= group.length - 3; i--) {
                    ou = group[i] + "." + ou;
                }
            }
        } catch (final UnknownHostException ex) {
            s_logger.info("Fail to get user's domain name. Would use cloud.com. ", ex);
            ou = "cloud.com";
        }

        final String o = ou;
        final String c = "Unknown";
        final String dname = "cn=\"" + cn + "\",ou=\"" + ou + "\",o=\"" + o + "\",c=\"" + c + "\"";
        final Script script = new Script(true, "keytool", 5000, null);
        script.add("-genkeypair");
        script.add("-keystore", keystorePath);
        script.add("-storepass", "vmops.com");
        script.add("-keypass", "vmops.com");
        script.add("-keyalg", "RSA");
        script.add("-validity", "3650");
        script.add("-dname", dname);
        final String result = script.execute();
        if (result != null) {
            throw new IOException("Fail to generate certificate!: " + result);
        }
    }

    static String getBase64Keystore(final String keystorePath) throws IOException {
        final byte[] storeBytes = FileUtils.readFileToByteArray(new File(keystorePath));
        if (storeBytes.length > 3000) { // Base64 codec would enlarge data by 1/3, and we have 4094 bytes in database entry at most
            throw new IOException("KeyStore is too big for database! Length " + storeBytes.length);
        }

        return new String(Base64.encodeBase64(storeBytes));
    }

    private void updateKeyPairsOnDisk(final String homeDir) {
        final File keyDir = new File(homeDir + "/.ssh");
        final Boolean devel = Boolean.valueOf(_configDao.getValue("developer"));
        if (!keyDir.isDirectory()) {
            s_logger.warn("Failed to create " + homeDir + "/.ssh for storing the SSH keypars");
            keyDir.mkdir();
        }
        final String pubKey = _configDao.getValue("ssh.publickey");
        final String prvKey = _configDao.getValue("ssh.privatekey");

        // Using non-default file names (id_rsa.cloud and id_rsa.cloud.pub) in developer mode. This is to prevent SSH keys overwritten for user running management server
        if (devel) {
            writeKeyToDisk(prvKey, homeDir + "/.ssh/id_rsa.cloud");
            writeKeyToDisk(pubKey, homeDir + "/.ssh/id_rsa.cloud.pub");
        } else {
            writeKeyToDisk(prvKey, homeDir + "/.ssh/id_rsa");
            writeKeyToDisk(pubKey, homeDir + "/.ssh/id_rsa.pub");
        }
    }

    protected void injectSshKeysIntoSystemVmIsoPatch(final String publicKeyPath, final String privKeyPath) {
        s_logger.info("Trying to inject public and private keys into systemvm iso");
        final String injectScript = getInjectScript();
        final String scriptPath = Script.findScript("", injectScript);
        final String systemVmIsoPath = Script.findScript("", "vms/systemvm.iso");
        if (scriptPath == null) {
            throw new CloudRuntimeException("Unable to find key inject script " + injectScript);
        }
        if (systemVmIsoPath == null) {
            throw new CloudRuntimeException("Unable to find systemvm iso vms/systemvm.iso");
        }
        final Script command = new Script("/bin/bash", s_logger);

        command.add(scriptPath);
        command.add(publicKeyPath);
        command.add(privKeyPath);
        command.add(systemVmIsoPath);

        final String result = command.execute();
        s_logger.info("Injected public and private keys into systemvm iso with result : " + result);
        if (result != null) {
            s_logger.warn("Failed to inject generated public key into systemvm iso " + result);
            throw new CloudRuntimeException("Failed to inject generated public key into systemvm iso " + result);
        }
    }

    private void templateDetailsInitIfNotExist(final long id, final String name, final String value) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement stmt = null;
        PreparedStatement stmtInsert = null;
        boolean insert = false;
        try {
            txn.start();
            stmt = txn.prepareAutoCloseStatement("SELECT id FROM vm_template_details WHERE template_id=? and name=?");
            stmt.setLong(1, id);
            stmt.setString(2, name);
            final ResultSet rs = stmt.executeQuery();
            if (rs == null || !rs.next()) {
                insert = true;
            }
            stmt.close();

            if (insert) {
                stmtInsert = txn.prepareAutoCloseStatement("INSERT INTO vm_template_details(template_id, name, value) VALUES(?, ?, ?)");
                stmtInsert.setLong(1, id);
                stmtInsert.setString(2, name);
                stmtInsert.setString(3, value);
                if (stmtInsert.executeUpdate() < 1) {
                    throw new CloudRuntimeException("Unable to init template " + id + " datails: " + name);
                }
            }
            txn.commit();
        } catch (final Exception e) {
            s_logger.warn("Unable to init template " + id + " datails: " + name, e);
            throw new CloudRuntimeException("Unable to init template " + id + " datails: " + name);
        }
    }

    private void writeKeyToDisk(final String key, final String keyPath) {
        final File keyfile = new File(keyPath);
        if (!keyfile.exists()) {
            try {
                keyfile.createNewFile();
            } catch (final IOException e) {
                s_logger.warn("Failed to create file: " + e.toString());
                throw new CloudRuntimeException("Failed to update keypairs on disk: cannot create  key file " + keyPath);
            }
        }

        if (keyfile.exists()) {
            try (FileOutputStream kStream = new FileOutputStream(keyfile)) {
                if (kStream != null) {
                    kStream.write(key.getBytes());
                }
            } catch (final FileNotFoundException e) {
                s_logger.warn("Failed to write  key to " + keyfile.getAbsolutePath());
                throw new CloudRuntimeException("Failed to update keypairs on disk: cannot find  key file " + keyPath);
            } catch (final IOException e) {
                s_logger.warn("Failed to write  key to " + keyfile.getAbsolutePath());
                throw new CloudRuntimeException("Failed to update keypairs on disk: cannot write to  key file " + keyPath);
            }
        }
    }

    protected String getInjectScript() {
        return "scripts/vm/systemvm/injectkeys.sh";
    }

    @Override
    @DB
    public void updateKeyPairs() {
        // Grab the SSH key pair and insert it into the database, if it is not present

        final String username = System.getProperty("user.name");
        final Boolean devel = Boolean.valueOf(_configDao.getValue("developer"));
        if (!username.equalsIgnoreCase("cloud") && !devel) {
            s_logger.warn("Systemvm keypairs could not be set. Management server should be run as cloud user, or in development mode.");
            return;
        }
        final String already = _configDao.getValue("ssh.privatekey");
        final String homeDir = System.getProperty("user.home");
        if (homeDir == null) {
            throw new CloudRuntimeException("Cannot get home directory for account: " + username);
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Processing updateKeyPairs");
        }

        if (homeDir != null && homeDir.startsWith("~")) {
            s_logger.error("No home directory was detected for the user '" + username + "'. Please check the profile of this user.");
            throw new CloudRuntimeException("No home directory was detected for the user '" + username + "'. Please check the profile of this user.");
        }

        // Using non-default file names (id_rsa.cloud and id_rsa.cloud.pub) in developer mode. This is to prevent SSH keys overwritten for user running management server
        File privkeyfile = null;
        File pubkeyfile = null;
        if (devel) {
            privkeyfile = new File(homeDir + "/.ssh/id_rsa.cloud");
            pubkeyfile = new File(homeDir + "/.ssh/id_rsa.cloud.pub");
        } else {
            privkeyfile = new File(homeDir + "/.ssh/id_rsa");
            pubkeyfile = new File(homeDir + "/.ssh/id_rsa.pub");
        }

        if (already == null || already.isEmpty()) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Systemvm keypairs not found in database. Need to store them in the database");
            }

            Script.runSimpleBashScript("if [ -f " + privkeyfile + " ]; then rm -f " + privkeyfile + "; fi; ssh-keygen -t rsa -N '' -f " + privkeyfile + " -q");

            final byte[] arr1 = new byte[4094]; // configuration table column value size
            try (DataInputStream dis = new DataInputStream(new FileInputStream(privkeyfile))) {
                dis.readFully(arr1);
            } catch (final EOFException e) {
                s_logger.info("[ignored] eof reached");
            } catch (final Exception e) {
                s_logger.error("Cannot read the private key file", e);
                throw new CloudRuntimeException("Cannot read the private key file");
            }
            final String privateKey = new String(arr1).trim();
            final byte[] arr2 = new byte[4094]; // configuration table column value size
            try (DataInputStream dis = new DataInputStream(new FileInputStream(pubkeyfile))) {
                dis.readFully(arr2);
            } catch (final EOFException e) {
                s_logger.info("[ignored] eof reached");
            } catch (final Exception e) {
                s_logger.warn("Cannot read the public key file", e);
                throw new CloudRuntimeException("Cannot read the public key file");
            }
            final String publicKey = new String(arr2).trim();

            final String insertSql1 =
                    "INSERT INTO `cloud`.`configuration` (category, instance, component, name, value, description) " +
                            "VALUES ('Hidden','DEFAULT', 'management-server','ssh.privatekey', '" + DBEncryptionUtil.encrypt(privateKey) +
                            "','Private key for the entire CloudStack')";
            final String insertSql2 =
                    "INSERT INTO `cloud`.`configuration` (category, instance, component, name, value, description) " +
                            "VALUES ('Hidden','DEFAULT', 'management-server','ssh.publickey', '" + DBEncryptionUtil.encrypt(publicKey) +
                            "','Public key for the entire CloudStack')";

            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {

                    final TransactionLegacy txn = TransactionLegacy.currentTxn();
                    try {
                        final PreparedStatement stmt1 = txn.prepareAutoCloseStatement(insertSql1);
                        stmt1.executeUpdate();
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Private key inserted into database");
                        }
                    } catch (final SQLException ex) {
                        s_logger.error("SQL of the private key failed", ex);
                        throw new CloudRuntimeException("SQL of the private key failed");
                    }

                    try {
                        final PreparedStatement stmt2 = txn.prepareAutoCloseStatement(insertSql2);
                        stmt2.executeUpdate();
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Public key inserted into database");
                        }
                    } catch (final SQLException ex) {
                        s_logger.error("SQL of the public key failed", ex);
                        throw new CloudRuntimeException("SQL of the public key failed");
                    }
                }
            });
        } else {
            s_logger.info("Keypairs already in database, updating local copy");
            updateKeyPairsOnDisk(homeDir);
        }
        s_logger.info("Going to update systemvm iso with generated keypairs if needed");
        try {
            injectSshKeysIntoSystemVmIsoPatch(pubkeyfile.getAbsolutePath(), privkeyfile.getAbsolutePath());
        } catch (final CloudRuntimeException e) {
            if (!devel) {
                throw new CloudRuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public List<ConfigurationVO> getConfigListByScope(final String scope, final Long resourceId) {

        // Getting the list of parameters defined at the scope
        final Set<ConfigKey<?>> configList = _configDepot.getConfigListByScope(scope);
        final List<ConfigurationVO> configVOList = new ArrayList<>();
        for (final ConfigKey<?> param : configList) {
            final ConfigurationVO configVo = _configDao.findByName(param.toString());
            configVo.setValue(_configDepot.get(param.toString()).valueIn(resourceId).toString());
            configVOList.add(configVo);
        }
        return configVOList;
    }
}
