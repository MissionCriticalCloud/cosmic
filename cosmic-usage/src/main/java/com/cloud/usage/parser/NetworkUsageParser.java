package com.cloud.usage.parser;

import com.cloud.usage.UsageNetworkVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageNetworkDao;
import com.cloud.user.AccountVO;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.usage.UsageTypes;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NetworkUsageParser {
    public static final Logger s_logger = LoggerFactory.getLogger(NetworkUsageParser.class.getName());

    private static UsageDao s_usageDao;
    private static UsageNetworkDao s_usageNetworkDao;

    @Inject
    private UsageDao _usageDao;
    @Inject
    private UsageNetworkDao _usageNetworkDao;

    public static boolean parse(final AccountVO account, final Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all Network usage events for account: " + account.getId());
        }

        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_network table for all entries for userId with
        // event_date in the given range
        final SearchCriteria<UsageNetworkVO> sc = s_usageNetworkDao.createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, account.getId());
        sc.addAnd("eventTimeMillis", SearchCriteria.Op.BETWEEN, startDate.getTime(), endDate.getTime());
        final List<UsageNetworkVO> usageNetworkVOs = s_usageNetworkDao.search(sc, null);

        final Map<String, NetworkInfo> networkUsageByZone = new HashMap<>();

        // Calculate the total bytes since last parsing
        for (final UsageNetworkVO usageNetwork : usageNetworkVOs) {
            final long zoneId = usageNetwork.getZoneId();
            String key = "" + zoneId;
            if (usageNetwork.getHostId() != 0) {
                key += "-Host" + usageNetwork.getHostId();
            }
            final NetworkInfo networkInfo = networkUsageByZone.get(key);

            long bytesSent = usageNetwork.getBytesSent();
            long bytesReceived = usageNetwork.getBytesReceived();
            if (networkInfo != null) {
                bytesSent += networkInfo.getBytesSent();
                bytesReceived += networkInfo.getBytesRcvd();
            }

            networkUsageByZone.put(key, new NetworkInfo(zoneId, usageNetwork.getHostId(), usageNetwork.getHostType(), usageNetwork.getNetworkId(), bytesSent,
                    bytesReceived));
        }

        final List<UsageVO> usageRecords = new ArrayList<>();
        for (final String key : networkUsageByZone.keySet()) {
            final NetworkInfo networkInfo = networkUsageByZone.get(key);
            final long totalBytesSent = networkInfo.getBytesSent();
            final long totalBytesReceived = networkInfo.getBytesRcvd();

            if ((totalBytesSent > 0L) || (totalBytesReceived > 0L)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Creating usage record, total bytes sent:" + totalBytesSent + ", total bytes received: " + totalBytesReceived + " for account: " +
                            account.getId() + " in availability zone " + networkInfo.getZoneId() + ", start: " + startDate + ", end: " + endDate);
                }

                Long hostId = null;

                // Create the usage record for bytes sent
                String usageDesc = "network bytes sent";
                if (networkInfo.getHostId() != 0) {
                    hostId = networkInfo.getHostId();
                    usageDesc += " for Host: " + networkInfo.getHostId();
                }
                UsageVO usageRecord =
                        new UsageVO(networkInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, totalBytesSent + " bytes sent",
                                UsageTypes.NETWORK_BYTES_SENT, new Double(totalBytesSent), hostId, networkInfo.getHostType(), networkInfo.getNetworkId(), startDate, endDate);
                usageRecords.add(usageRecord);

                // Create the usage record for bytes received
                usageDesc = "network bytes received";
                if (networkInfo.getHostId() != 0) {
                    usageDesc += " for Host: " + networkInfo.getHostId();
                }
                usageRecord =
                        new UsageVO(networkInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, totalBytesReceived + " bytes received",
                                UsageTypes.NETWORK_BYTES_RECEIVED, new Double(totalBytesReceived), hostId, networkInfo.getHostType(), networkInfo.getNetworkId(), startDate,
                                endDate);
                usageRecords.add(usageRecord);
            } else {
                // Don't charge anything if there were zero bytes processed
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No usage record (0 bytes used) generated for account: " + account.getId());
                }
            }
        }

        s_usageDao.saveUsageRecords(usageRecords);

        return true;
    }

    @PostConstruct
    void init() {
        s_usageDao = _usageDao;
        s_usageNetworkDao = _usageNetworkDao;
    }

    private static class NetworkInfo {
        private final long zoneId;
        private final long hostId;
        private final String hostType;
        private final Long networkId;
        private final long bytesSent;
        private final long bytesRcvd;

        public NetworkInfo(final long zoneId, final long hostId, final String hostType, final Long networkId, final long bytesSent, final long bytesRcvd) {
            this.zoneId = zoneId;
            this.hostId = hostId;
            this.hostType = hostType;
            this.networkId = networkId;
            this.bytesSent = bytesSent;
            this.bytesRcvd = bytesRcvd;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getHostId() {
            return hostId;
        }

        public Long getNetworkId() {
            return networkId;
        }

        public long getBytesSent() {
            return bytesSent;
        }

        public long getBytesRcvd() {
            return bytesRcvd;
        }

        public String getHostType() {
            return hostType;
        }
    }
}
