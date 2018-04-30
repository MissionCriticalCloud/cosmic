package com.cloud.alert;

import com.cloud.alert.dao.AlertDao;
import com.cloud.api.ApiDBUtils;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityManager;
import com.cloud.capacity.CapacityState;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.capacity.dao.CapacityDaoImpl.SummedCapacity;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DataCenterIpAddressDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.AlertGenerator;
import com.cloud.event.EventTypes;
import com.cloud.framework.config.ConfigDepot;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.managed.context.ManagedContextTimerTask;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.StorageManager;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.SearchCriteria;

import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertManagerImpl extends ManagerBase implements AlertManager, Configurable {
    private static final Logger s_logger = LoggerFactory.getLogger(AlertManagerImpl.class.getName());
    private static final Logger s_alertsLogger = LoggerFactory.getLogger("com.cloud.alerts");

    private static final long INITIAL_CAPACITY_CHECK_DELAY = 30L * 1000L; // thirty seconds expressed in milliseconds

    private static final DecimalFormat DfPct = new DecimalFormat("###.##");
    private static final DecimalFormat DfWhole = new DecimalFormat("########");
    private final ExecutorService _executor;
    @Inject
    protected StorageManager _storageMgr;
    @Inject
    protected CapacityManager _capacityMgr;
    @Inject
    protected ConfigDepot _configDepot;
    Map<Short, Double> _capacityTypeThresholdMap = new HashMap<>();
    private EmailAlert _emailAlert;
    @Inject
    private AlertDao _alertDao;
    @Inject
    private CapacityDao _capacityDao;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private HostPodDao _podDao;
    @Inject
    private ClusterDao _clusterDao;
    @Inject
    private IPAddressDao _publicIPAddressDao;
    @Inject
    private DataCenterIpAddressDao _privateIPAddressDao;
    @Inject
    private PrimaryDataStoreDao _storagePoolDao;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private ResourceManager _resourceMgr;
    @Inject
    private ConfigurationManager _configMgr;
    @Inject
    private ZoneRepository zoneRepository;
    private Timer _timer = null;
    private long _capacityCheckPeriod = 60L * 60L * 1000L; // one hour by default
    private double _publicIPCapacityThreshold = 0.75;
    private double _privateIPCapacityThreshold = 0.75;
    private double _secondaryStorageCapacityThreshold = 0.75;
    private double _vlanCapacityThreshold = 0.75;
    private double _directNetworkPublicIpCapacityThreshold = 0.75;
    private double _localStorageCapacityThreshold = 0.75;

    public AlertManagerImpl() {
        _executor = Executors.newCachedThreadPool(new NamedThreadFactory("Email-Alerts-Sender"));
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        final Map<String, String> configs = _configDao.getConfiguration("management-server", params);

        // set up the email system for alerts
        final String emailAddressList = configs.get("alert.email.addresses");
        String[] emailAddresses = null;
        if (emailAddressList != null) {
            emailAddresses = emailAddressList.split(",");
        }

        final String smtpHost = configs.get("alert.smtp.host");
        final int smtpPort = NumbersUtil.parseInt(configs.get("alert.smtp.port"), 25);
        final String useAuthStr = configs.get("alert.smtp.useAuth");
        final boolean useAuth = ((useAuthStr == null) ? false : Boolean.parseBoolean(useAuthStr));
        final String smtpUsername = configs.get("alert.smtp.username");
        final String smtpPassword = configs.get("alert.smtp.password");
        final String emailSender = configs.get("alert.email.sender");
        final String smtpDebugStr = configs.get("alert.smtp.debug");
        final int smtpTimeout = NumbersUtil.parseInt(configs.get("alert.smtp.timeout"), 30000);
        final int smtpConnectionTimeout = NumbersUtil.parseInt(configs.get("alert.smtp.connectiontimeout"), 30000);
        boolean smtpDebug = false;
        if (smtpDebugStr != null) {
            smtpDebug = Boolean.parseBoolean(smtpDebugStr);
        }

        _emailAlert = new EmailAlert(emailAddresses, smtpHost, smtpPort, smtpConnectionTimeout, smtpTimeout, useAuth, smtpUsername, smtpPassword, emailSender, smtpDebug);

        final String publicIPCapacityThreshold = _configDao.getValue(Config.PublicIpCapacityThreshold.key());
        final String privateIPCapacityThreshold = _configDao.getValue(Config.PrivateIpCapacityThreshold.key());
        final String secondaryStorageCapacityThreshold = _configDao.getValue(Config.SecondaryStorageCapacityThreshold.key());
        final String vlanCapacityThreshold = _configDao.getValue(Config.VlanCapacityThreshold.key());
        final String directNetworkPublicIpCapacityThreshold = _configDao.getValue(Config.DirectNetworkPublicIpCapacityThreshold.key());
        final String localStorageCapacityThreshold = _configDao.getValue(Config.LocalStorageCapacityThreshold.key());

        if (publicIPCapacityThreshold != null) {
            _publicIPCapacityThreshold = Double.parseDouble(publicIPCapacityThreshold);
        }
        if (privateIPCapacityThreshold != null) {
            _privateIPCapacityThreshold = Double.parseDouble(privateIPCapacityThreshold);
        }
        if (secondaryStorageCapacityThreshold != null) {
            _secondaryStorageCapacityThreshold = Double.parseDouble(secondaryStorageCapacityThreshold);
        }
        if (vlanCapacityThreshold != null) {
            _vlanCapacityThreshold = Double.parseDouble(vlanCapacityThreshold);
        }
        if (directNetworkPublicIpCapacityThreshold != null) {
            _directNetworkPublicIpCapacityThreshold = Double.parseDouble(directNetworkPublicIpCapacityThreshold);
        }
        if (localStorageCapacityThreshold != null) {
            _localStorageCapacityThreshold = Double.parseDouble(localStorageCapacityThreshold);
        }

        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP, _publicIPCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_PRIVATE_IP, _privateIPCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_SECONDARY_STORAGE, _secondaryStorageCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_VLAN, _vlanCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP, _directNetworkPublicIpCapacityThreshold);
        _capacityTypeThresholdMap.put(Capacity.CAPACITY_TYPE_LOCAL_STORAGE, _localStorageCapacityThreshold);

        final String capacityCheckPeriodStr = configs.get("capacity.check.period");
        if (capacityCheckPeriodStr != null) {
            _capacityCheckPeriod = Long.parseLong(capacityCheckPeriodStr);
            if (_capacityCheckPeriod <= 0) {
                _capacityCheckPeriod = Long.parseLong(Config.CapacityCheckPeriod.getDefaultValue());
            }
        }

        _timer = new Timer("CapacityChecker");

        return true;
    }

    @Override
    public boolean start() {
        _timer.schedule(new CapacityChecker(), INITIAL_CAPACITY_CHECK_DELAY, _capacityCheckPeriod);
        return true;
    }

    @Override
    public boolean stop() {
        _timer.cancel();
        return true;
    }

    @Override
    public void clearAlert(final AlertType alertType, final long dataCenterId, final long podId) {
        try {
            if (_emailAlert != null) {
                _emailAlert.clearAlert(alertType.getType(), dataCenterId, podId);
            }
        } catch (final Exception ex) {
            s_logger.error("Problem clearing email alert", ex);
        }
    }

    @Override
    public void recalculateCapacity() {
        // FIXME: the right way to do this is to register a listener (see RouterStatsListener, VMSyncListener)
        //        for the vm sync state.  The listener model has connects/disconnects to keep things in sync much better
        //        than this model right now, so when a VM is started, we update the amount allocated, and when a VM
        //        is stopped we updated the amount allocated, and when VM sync reports a changed state, we update
        //        the amount allocated.  Hopefully it's limited to 3 entry points and will keep the amount allocated
        //        per host accurate.

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("recalculating system capacity");
            s_logger.debug("Executing cpu/ram capacity update");
        }

        // Calculate CPU and RAM capacities
        //     get all hosts...even if they are not in 'UP' state
        final List<HostVO> hosts = _resourceMgr.listAllNotInMaintenanceHostsInOneZone(Host.Type.Routing, null);
        if (hosts != null) {
            for (final HostVO host : hosts) {
                _capacityMgr.updateCapacityForHost(host);
            }
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Done executing cpu/ram capacity update");
            s_logger.debug("Executing storage capacity update");
        }
        // Calculate storage pool capacity
        final List<StoragePoolVO> storagePools = _storagePoolDao.listAll();
        for (final StoragePoolVO pool : storagePools) {
            final long disk = _capacityMgr.getAllocatedPoolCapacity(pool, null);
            if (pool.isShared()) {
                _storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED, disk);
            } else {
                _storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_LOCAL_STORAGE, disk);
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Done executing storage capacity update");
            s_logger.debug("Executing capacity updates for public ip and Vlans");
        }

        final List<Zone> zones = zoneRepository.findByRemovedIsNull();

        for (final Zone zone : zones) {
            final long zoneId = zone.getId();

            //NOTE
            //What happens if we have multiple vlans? Dashboard currently shows stats
            //with no filter based on a vlan
            //ideal way would be to remove out the vlan param, and filter only on dcId
            //implementing the same

            // Calculate new Public IP capacity for Virtual Network
            if (zone.getNetworkType() == NetworkType.Advanced) {
                createOrUpdateIpCapacity(zoneId, null, Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP, zone.getAllocationState());
            }

            // Calculate new Public IP capacity for Direct Attached Network
            createOrUpdateIpCapacity(zoneId, null, Capacity.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP, zone.getAllocationState());

            if (zone.getNetworkType() == NetworkType.Advanced) {
                //Calculate VLAN's capacity
                createOrUpdateVlanCapacity(zoneId, zone.getAllocationState());
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Done capacity updates for public ip and Vlans");
            s_logger.debug("Executing capacity updates for private ip");
        }

        // Calculate new Private IP capacity
        final List<HostPodVO> pods = _podDao.listAll();
        for (final HostPodVO pod : pods) {
            final long podId = pod.getId();
            final long dcId = pod.getDataCenterId();

            createOrUpdateIpCapacity(dcId, podId, Capacity.CAPACITY_TYPE_PRIVATE_IP, _configMgr.findPodAllocationState(pod));
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Done executing capacity updates for private ip");
            s_logger.debug("Done recalculating system capacity");
        }
    }

    @Override
    public void sendAlert(final AlertType alertType, final long dataCenterId, final Long podId, final String subject, final String body) {

        // publish alert
        AlertGenerator.publishAlertOnEventBus(alertType.getName(), dataCenterId, podId, subject, body);

        // TODO:  queue up these messages and send them as one set of issues once a certain number of issues is reached?  If that's the case,
        //         shouldn't we have a type/severity as part of the API so that severe errors get sent right away?
        try {
            if (_emailAlert != null) {
                _emailAlert.sendAlert(alertType, dataCenterId, podId, null, subject, body);
            } else {
                s_alertsLogger.warn(" alertType:: " + alertType + " // dataCenterId:: " + dataCenterId + " // podId:: " + podId +
                        " // message:: " + subject + " // body:: " + body);
            }
        } catch (final Exception ex) {
            s_logger.error("Problem sending email alert", ex);
        }
    }

    public void checkForAlerts() {

        recalculateCapacity();

        // abort if we can't possibly send an alert...
        if (_emailAlert == null) {
            return;
        }

        //Get all datacenters, pods and clusters in the system.
        final List<Zone> zones = zoneRepository.findByRemovedIsNull();
        final List<ClusterVO> clusterList = _clusterDao.listAll();
        final List<HostPodVO> podList = _podDao.listAll();
        //Get capacity types at different levels
        final List<Short> dataCenterCapacityTypes = getCapacityTypesAtZoneLevel();
        final List<Short> podCapacityTypes = getCapacityTypesAtPodLevel();
        final List<Short> clusterCapacityTypes = getCapacityTypesAtClusterLevel();

        // Generate Alerts for Zone Level capacities
        for (final Zone zone : zones) {
            for (final Short capacityType : dataCenterCapacityTypes) {
                final List<SummedCapacity> capacity;
                capacity = _capacityDao.findCapacityBy(capacityType.intValue(), zone.getId(), null, null);

                if (capacityType == Capacity.CAPACITY_TYPE_SECONDARY_STORAGE) {
                    capacity.add(getUsedStats(capacityType, zone.getId(), null, null));
                }
                if (capacity == null || capacity.size() == 0) {
                    continue;
                }
                final double totalCapacity = capacity.get(0).getTotalCapacity();
                final double usedCapacity = capacity.get(0).getUsedCapacity();
                if (totalCapacity != 0 && usedCapacity / totalCapacity > _capacityTypeThresholdMap.get(capacityType)) {
                    generateEmailAlert(zone, null, null, totalCapacity, usedCapacity, capacityType);
                }
            }
        }

        // Generate Alerts for Pod Level capacities
        for (final HostPodVO pod : podList) {
            for (final Short capacityType : podCapacityTypes) {
                final List<SummedCapacity> capacity = _capacityDao.findCapacityBy(capacityType.intValue(), pod.getDataCenterId(), pod.getId(), null);
                if (capacity == null || capacity.size() == 0) {
                    continue;
                }
                final double totalCapacity = capacity.get(0).getTotalCapacity();
                final double usedCapacity = capacity.get(0).getUsedCapacity();
                if (totalCapacity != 0 && usedCapacity / totalCapacity > _capacityTypeThresholdMap.get(capacityType)) {
                    generateEmailAlert(zoneRepository.findById(pod.getDataCenterId()).orElse(null), pod, null, totalCapacity, usedCapacity, capacityType);
                }
            }
        }

        // Generate Alerts for Cluster Level capacities
        for (final ClusterVO cluster : clusterList) {
            for (final Short capacityType : clusterCapacityTypes) {
                final List<SummedCapacity> capacity;
                capacity = _capacityDao.findCapacityBy(capacityType.intValue(), cluster.getDataCenterId(), null, cluster.getId());

                // cpu and memory allocated capacity notification threshold can be defined at cluster level, so getting the value if they are defined at cluster level
                final double threshold;
                switch (capacityType) {
                    case Capacity.CAPACITY_TYPE_STORAGE:
                        capacity.add(getUsedStats(capacityType, cluster.getDataCenterId(), cluster.getPodId(), cluster.getId()));
                        threshold = StorageCapacityThreshold.valueIn(cluster.getId());
                        break;
                    case Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED:
                        threshold = StorageAllocatedCapacityThreshold.valueIn(cluster.getId());
                        break;
                    case Capacity.CAPACITY_TYPE_CPU:
                        threshold = CPUCapacityThreshold.valueIn(cluster.getId());
                        break;
                    case Capacity.CAPACITY_TYPE_MEMORY:
                        threshold = MemoryCapacityThreshold.valueIn(cluster.getId());
                        break;
                    default:
                        threshold = _capacityTypeThresholdMap.get(capacityType);
                }
                if (capacity == null || capacity.size() == 0) {
                    continue;
                }

                final double totalCapacity = capacity.get(0).getTotalCapacity();
                final double usedCapacity = capacity.get(0).getUsedCapacity() + capacity.get(0).getReservedCapacity();
                if (totalCapacity != 0 && usedCapacity / totalCapacity > threshold) {
                    generateEmailAlert(zoneRepository.findById(cluster.getDataCenterId()).orElse(null), ApiDBUtils.findPodById(cluster.getPodId()), cluster, totalCapacity, usedCapacity,
                            capacityType);
                }
            }
        }
    }

    private List<Short> getCapacityTypesAtZoneLevel() {

        final List<Short> dataCenterCapacityTypes = new ArrayList<>();
        dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP);
        dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP);
        dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_SECONDARY_STORAGE);
        dataCenterCapacityTypes.add(Capacity.CAPACITY_TYPE_VLAN);
        return dataCenterCapacityTypes;
    }

    private List<Short> getCapacityTypesAtPodLevel() {

        final List<Short> podCapacityTypes = new ArrayList<>();
        podCapacityTypes.add(Capacity.CAPACITY_TYPE_PRIVATE_IP);
        return podCapacityTypes;
    }

    private List<Short> getCapacityTypesAtClusterLevel() {

        final List<Short> clusterCapacityTypes = new ArrayList<>();
        clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_CPU);
        clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_MEMORY);
        clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_STORAGE);
        clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED);
        clusterCapacityTypes.add(Capacity.CAPACITY_TYPE_LOCAL_STORAGE);
        return clusterCapacityTypes;
    }

    private SummedCapacity getUsedStats(final short capacityType, final long zoneId, final Long podId, final Long clusterId) {
        final CapacityVO capacity;
        if (capacityType == Capacity.CAPACITY_TYPE_SECONDARY_STORAGE) {
            capacity = _storageMgr.getSecondaryStorageUsedStats(null, zoneId);
        } else {
            capacity = _storageMgr.getStoragePoolUsedStats(null, clusterId, podId, zoneId);
        }
        if (capacity != null) {
            return new SummedCapacity(capacity.getUsedCapacity(), 0, capacity.getTotalCapacity(), capacityType, clusterId, podId);
        } else {
            return null;
        }
    }

    private void generateEmailAlert(final Zone zone, final HostPodVO pod, final ClusterVO cluster, final double totalCapacity, final double usedCapacity, final short
            capacityType) {
        String msgSubject = null;
        String msgContent = null;
        final String totalStr;
        final String usedStr;
        final String pctStr = formatPercent(usedCapacity / totalCapacity);
        AlertType alertType = null;
        final Long podId = pod == null ? null : pod.getId();
        final Long clusterId = cluster == null ? null : cluster.getId();

        switch (capacityType) {

            //Cluster Level
            case Capacity.CAPACITY_TYPE_MEMORY:
                msgSubject = "System Alert: Low Available Memory in cluster " + cluster.getName() + " pod " + pod.getName() + " of availability zone " + zone.getName();
                totalStr = formatBytesToMegabytes(totalCapacity);
                usedStr = formatBytesToMegabytes(usedCapacity);
                msgContent = "System memory is low, total: " + totalStr + " MB, used: " + usedStr + " MB (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_MEMORY;
                break;
            case Capacity.CAPACITY_TYPE_CPU:
                msgSubject = "System Alert: Low Unallocated CPU in cluster " + cluster.getName() + " pod " + pod.getName() + " of availability zone " + zone.getName();
                totalStr = DfWhole.format(totalCapacity);
                usedStr = DfWhole.format(usedCapacity);
                msgContent = "Unallocated CPU is low, total: " + totalStr + " Mhz, used: " + usedStr + " Mhz (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_CPU;
                break;
            case Capacity.CAPACITY_TYPE_STORAGE:
                msgSubject = "System Alert: Low Available Storage in cluster " + cluster.getName() + " pod " + pod.getName() + " of availability zone " + zone.getName();
                totalStr = formatBytesToMegabytes(totalCapacity);
                usedStr = formatBytesToMegabytes(usedCapacity);
                msgContent = "Available storage space is low, total: " + totalStr + " MB, used: " + usedStr + " MB (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_STORAGE;
                break;
            case Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED:
                msgSubject =
                        "System Alert: Remaining unallocated Storage is low in cluster " + cluster.getName() + " pod " + pod.getName() + " of availability zone " +
                                zone.getName();
                totalStr = formatBytesToMegabytes(totalCapacity);
                usedStr = formatBytesToMegabytes(usedCapacity);
                msgContent = "Unallocated storage space is low, total: " + totalStr + " MB, allocated: " + usedStr + " MB (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_STORAGE_ALLOCATED;
                break;
            case Capacity.CAPACITY_TYPE_LOCAL_STORAGE:
                msgSubject =
                        "System Alert: Remaining unallocated Local Storage is low in cluster " + cluster.getName() + " pod " + pod.getName() + " of availability zone " +
                                zone.getName();
                totalStr = formatBytesToMegabytes(totalCapacity);
                usedStr = formatBytesToMegabytes(usedCapacity);
                msgContent = "Unallocated storage space is low, total: " + totalStr + " MB, allocated: " + usedStr + " MB (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_LOCAL_STORAGE;
                break;

            //Pod Level
            case Capacity.CAPACITY_TYPE_PRIVATE_IP:
                msgSubject = "System Alert: Number of unallocated private IPs is low in pod " + pod.getName() + " of availability zone " + zone.getName();
                totalStr = Double.toString(totalCapacity);
                usedStr = Double.toString(usedCapacity);
                msgContent = "Number of unallocated private IPs is low, total: " + totalStr + ", allocated: " + usedStr + " (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_PRIVATE_IP;
                break;

            //Zone Level
            case Capacity.CAPACITY_TYPE_SECONDARY_STORAGE:
                msgSubject = "System Alert: Low Available Secondary Storage in availability zone " + zone.getName();
                totalStr = formatBytesToMegabytes(totalCapacity);
                usedStr = formatBytesToMegabytes(usedCapacity);
                msgContent = "Available secondary storage space is low, total: " + totalStr + " MB, used: " + usedStr + " MB (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_SECONDARY_STORAGE;
                break;
            case Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP:
                msgSubject = "System Alert: Number of unallocated virtual network public IPs is low in availability zone " + zone.getName();
                totalStr = Double.toString(totalCapacity);
                usedStr = Double.toString(usedCapacity);
                msgContent = "Number of unallocated public IPs is low, total: " + totalStr + ", allocated: " + usedStr + " (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_VIRTUAL_NETWORK_PUBLIC_IP;
                break;
            case Capacity.CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP:
                msgSubject = "System Alert: Number of unallocated shared network IPs is low in availability zone " + zone.getName();
                totalStr = Double.toString(totalCapacity);
                usedStr = Double.toString(usedCapacity);
                msgContent = "Number of unallocated shared network IPs is low, total: " + totalStr + ", allocated: " + usedStr + " (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_DIRECT_ATTACHED_PUBLIC_IP;
                break;
            case Capacity.CAPACITY_TYPE_VLAN:
                msgSubject = "System Alert: Number of unallocated VLANs is low in availability zone " + zone.getName();
                totalStr = Double.toString(totalCapacity);
                usedStr = Double.toString(usedCapacity);
                msgContent = "Number of unallocated VLANs is low, total: " + totalStr + ", allocated: " + usedStr + " (" + pctStr + "%)";
                alertType = AlertManager.AlertType.ALERT_TYPE_VLAN;
                break;
        }

        try {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(msgSubject);
                s_logger.debug(msgContent);
            }
            _emailAlert.sendAlert(alertType, zone.getId(), podId, clusterId, msgSubject, msgContent);
        } catch (final Exception ex) {
            s_logger.error("Exception in CapacityChecker", ex);
        }
    }

    public void createOrUpdateIpCapacity(final Long dcId, final Long podId, final short capacityType, final AllocationState capacityState) {
        SearchCriteria<CapacityVO> capacitySC = _capacityDao.createSearchCriteria();

        List<CapacityVO> capacities = _capacityDao.search(capacitySC, null);
        capacitySC = _capacityDao.createSearchCriteria();
        capacitySC.addAnd("podId", SearchCriteria.Op.EQ, podId);
        capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ, dcId);
        capacitySC.addAnd("capacityType", SearchCriteria.Op.EQ, capacityType);

        final int totalIPs;
        final int allocatedIPs;
        capacities = _capacityDao.search(capacitySC, null);
        if (capacityType == Capacity.CAPACITY_TYPE_PRIVATE_IP) {
            totalIPs = _privateIPAddressDao.countIPs(podId, dcId, false);
            allocatedIPs = _privateIPAddressDao.countIPs(podId, dcId, true);
        } else if (capacityType == Capacity.CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP) {
            totalIPs = _publicIPAddressDao.countIPsForNetwork(dcId, false, VlanType.VirtualNetwork);
            allocatedIPs = _publicIPAddressDao.countIPsForNetwork(dcId, true, VlanType.VirtualNetwork);
        } else {
            totalIPs = _publicIPAddressDao.countIPsForNetwork(dcId, false, VlanType.DirectAttached);
            allocatedIPs = _publicIPAddressDao.countIPsForNetwork(dcId, true, VlanType.DirectAttached);
        }

        final CapacityState ipCapacityState = (capacityState == AllocationState.Disabled) ? CapacityState.Disabled : CapacityState.Enabled;
        if (capacities.size() == 0) {
            final CapacityVO newPublicIPCapacity = new CapacityVO(null, dcId, podId, null, allocatedIPs, totalIPs, capacityType);
            newPublicIPCapacity.setCapacityState(ipCapacityState);
            _capacityDao.persist(newPublicIPCapacity);
        } else if (!(capacities.get(0).getUsedCapacity() == allocatedIPs && capacities.get(0).getTotalCapacity() == totalIPs && capacities.get(0).getCapacityState() ==
                ipCapacityState)) {
            final CapacityVO capacity = capacities.get(0);
            capacity.setUsedCapacity(allocatedIPs);
            capacity.setTotalCapacity(totalIPs);
            capacity.setCapacityState(ipCapacityState);
            _capacityDao.update(capacity.getId(), capacity);
        }
    }

    private void createOrUpdateVlanCapacity(final long dcId, final AllocationState capacityState) {

        SearchCriteria<CapacityVO> capacitySC = _capacityDao.createSearchCriteria();

        List<CapacityVO> capacities = _capacityDao.search(capacitySC, null);
        capacitySC = _capacityDao.createSearchCriteria();
        capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ, dcId);
        capacitySC.addAnd("capacityType", SearchCriteria.Op.EQ, Capacity.CAPACITY_TYPE_VLAN);
        capacities = _capacityDao.search(capacitySC, null);

        final int totalVlans = _dcDao.countZoneVlans(dcId, false);
        final int allocatedVlans = _dcDao.countZoneVlans(dcId, true);

        final CapacityState vlanCapacityState = (capacityState == AllocationState.Disabled) ? CapacityState.Disabled : CapacityState.Enabled;
        if (capacities.size() == 0) {
            final CapacityVO newVlanCapacity = new CapacityVO(null, dcId, null, null, allocatedVlans, totalVlans, Capacity.CAPACITY_TYPE_VLAN);
            newVlanCapacity.setCapacityState(vlanCapacityState);
            _capacityDao.persist(newVlanCapacity);
        } else if (!(capacities.get(0).getUsedCapacity() == allocatedVlans && capacities.get(0).getTotalCapacity() == totalVlans && capacities.get(0).getCapacityState() ==
                vlanCapacityState)) {
            final CapacityVO capacity = capacities.get(0);
            capacity.setUsedCapacity(allocatedVlans);
            capacity.setTotalCapacity(totalVlans);
            capacity.setCapacityState(vlanCapacityState);
            _capacityDao.update(capacity.getId(), capacity);
        }
    }

    private static String formatPercent(final double percentage) {
        return DfPct.format(percentage * 100);
    }

    private static String formatBytesToMegabytes(final double bytes) {
        final double megaBytes = (bytes / (1024 * 1024));
        return DfWhole.format(megaBytes);
    }

    @Override
    public String getConfigComponentName() {
        return AlertManager.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{CPUCapacityThreshold, MemoryCapacityThreshold, StorageAllocatedCapacityThreshold, StorageCapacityThreshold};
    }

    @Override
    @ActionEvent(eventType = EventTypes.ALERT_GENERATE, eventDescription = "generating alert", async = true)
    public boolean generateAlert(final AlertType alertType, final long dataCenterId, final Long podId, final String msg) {
        try {
            sendAlert(alertType, dataCenterId, podId, msg, msg);
            return true;
        } catch (final Exception ex) {
            s_logger.warn("Failed to generate an alert of type=" + alertType + "; msg=" + msg);
            return false;
        }
    }

    class CapacityChecker extends ManagedContextTimerTask {
        @Override
        protected void runInContext() {
            s_logger.debug("Running Capacity Checker ... ");
            checkForAlerts();
            s_logger.debug("Done running Capacity Checker ... ");
        }
    }

    class EmailAlert {
        private final String _smtpHost;
        private final String _smtpUsername;
        private final String _smtpPassword;
        private final String _emailSender;
        private final Session _smtpSession;
        private InternetAddress[] _recipientList;
        private int _smtpPort = -1;
        private boolean _smtpUseAuth = false;
        private final int _smtpTimeout;
        private final int _smtpConnectionTimeout;

        public EmailAlert(final String[] recipientList, final String smtpHost, final int smtpPort, final int smtpConnectionTimeout, final int smtpTimeout, final boolean
                smtpUseAuth,
                          final String smtpUsername,
                          final String smtpPassword, final String emailSender, final boolean smtpDebug) {
            if (recipientList != null) {
                _recipientList = new InternetAddress[recipientList.length];
                for (int i = 0; i < recipientList.length; i++) {
                    try {
                        _recipientList[i] = new InternetAddress(recipientList[i], recipientList[i]);
                    } catch (final Exception ex) {
                        s_logger.error("Exception creating address for: " + recipientList[i], ex);
                    }
                }
            }

            _smtpHost = smtpHost;
            _smtpPort = smtpPort;
            _smtpUseAuth = smtpUseAuth;
            _smtpUsername = smtpUsername;
            _smtpPassword = smtpPassword;
            _emailSender = emailSender;
            _smtpTimeout = smtpTimeout;
            _smtpConnectionTimeout = smtpConnectionTimeout;

            if (_smtpHost != null) {
                final Properties smtpProps = new Properties();
                smtpProps.put("mail.smtp.host", smtpHost);
                smtpProps.put("mail.smtp.port", smtpPort);
                smtpProps.put("mail.smtp.auth", "" + smtpUseAuth);
                smtpProps.put("mail.smtp.timeout", _smtpTimeout);
                smtpProps.put("mail.smtp.connectiontimeout", _smtpConnectionTimeout);

                if (smtpUsername != null) {
                    smtpProps.put("mail.smtp.user", smtpUsername);
                }

                smtpProps.put("mail.smtps.host", smtpHost);
                smtpProps.put("mail.smtps.port", smtpPort);
                smtpProps.put("mail.smtps.auth", "" + smtpUseAuth);
                smtpProps.put("mail.smtps.timeout", _smtpTimeout);
                smtpProps.put("mail.smtps.connectiontimeout", _smtpConnectionTimeout);

                if (smtpUsername != null) {
                    smtpProps.put("mail.smtps.user", smtpUsername);
                }

                if ((smtpUsername != null) && (smtpPassword != null)) {
                    _smtpSession = Session.getInstance(smtpProps, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(smtpUsername, smtpPassword);
                        }
                    });
                } else {
                    _smtpSession = Session.getInstance(smtpProps);
                }
                _smtpSession.setDebug(smtpDebug);
            } else {
                _smtpSession = null;
            }
        }

        // TODO:  make sure this handles SSL transport (useAuth is true) and regular
        public void sendAlert(final AlertType alertType, final long dataCenterId, final Long podId, final Long clusterId, final String subject, final String content) throws
                MessagingException,
                UnsupportedEncodingException {
            s_alertsLogger.warn(" alertType:: " + alertType + " // dataCenterId:: " + dataCenterId + " // podId:: " +
                    podId + " // clusterId:: " + clusterId + " // message:: " + subject);
            AlertVO alert = null;
            if ((alertType != AlertManager.AlertType.ALERT_TYPE_HOST) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_USERVM) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_DOMAIN_ROUTER) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_CONSOLE_PROXY) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_SSVM) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_STORAGE_MISC) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_MANAGMENT_NODE) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_RESOURCE_LIMIT_EXCEEDED) &&
                    (alertType != AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED)) {
                alert = _alertDao.getLastAlert(alertType.getType(), dataCenterId, podId, clusterId);
            }

            if (alert == null) {
                // set up a new alert
                final AlertVO newAlert = new AlertVO();
                newAlert.setType(alertType.getType());
                newAlert.setSubject(subject);
                newAlert.setClusterId(clusterId);
                newAlert.setPodId(podId);
                newAlert.setDataCenterId(dataCenterId);
                newAlert.setSentCount(1); // initialize sent count to 1 since we are now sending an alert
                newAlert.setLastSent(new Date());
                newAlert.setName(alertType.getName());
                _alertDao.persist(newAlert);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Have already sent: " + alert.getSentCount() + " emails for alert type '" + alertType + "' -- skipping send email");
                }
                return;
            }

            if (_smtpSession != null) {
                final SMTPMessage msg = new SMTPMessage(_smtpSession);
                msg.setSender(new InternetAddress(_emailSender, _emailSender));
                msg.setFrom(new InternetAddress(_emailSender, _emailSender));
                for (final InternetAddress address : _recipientList) {
                    msg.addRecipient(RecipientType.TO, address);
                }
                msg.setSubject(subject);
                msg.setSentDate(new Date());
                msg.setContent(content, "text/plain");
                msg.saveChanges();

                final SMTPTransport smtpTrans;
                if (_smtpUseAuth) {
                    smtpTrans = new SMTPSSLTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                } else {
                    smtpTrans = new SMTPTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                }
                sendMessage(smtpTrans, msg);
            }
        }

        private void sendMessage(final SMTPTransport smtpTrans, final SMTPMessage msg) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        smtpTrans.connect();
                        smtpTrans.sendMessage(msg, msg.getAllRecipients());
                        smtpTrans.close();
                    } catch (final SendFailedException e) {
                        s_logger.error(" Failed to send email alert " + e);
                    } catch (final MessagingException e) {
                        s_logger.error(" Failed to send email alert " + e);
                    }
                }
            });
        }

        public void clearAlert(final short alertType, final long dataCenterId, final Long podId) {
            if (alertType != -1) {
                final AlertVO alert = _alertDao.getLastAlert(alertType, dataCenterId, podId, null);
                if (alert != null) {
                    final AlertVO updatedAlert = _alertDao.createForUpdate();
                    updatedAlert.setResolved(new Date());
                    _alertDao.update(alert.getId(), updatedAlert);
                }
            }
        }
    }
}
