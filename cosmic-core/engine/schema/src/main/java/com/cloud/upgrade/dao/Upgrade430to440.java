package com.cloud.upgrade.dao;

import com.cloud.network.Network;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade430to440 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade430to440.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.3.0", "4.4.0"};
    }

    @Override
    public String getUpgradedVersion() {
        return "4.4.0";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        final String script = Script.findScript("", "db/schema-430to440.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-4310to440.sql");
        }

        return new File[]{new File(script)};
    }

    @Override
    public void performDataMigration(final Connection conn) {
        secondaryIpsAccountAndDomainIdsUpdate(conn);
        moveCidrsToTheirOwnTable(conn);
        addExtractTemplateAndVolumeColumns(conn);
        updateVlanUris(conn);
    }

    private void secondaryIpsAccountAndDomainIdsUpdate(final Connection conn) {
        final String secondIpsSql = "SELECT id, vmId, network_id, account_id, domain_id, ip4_address FROM `cloud`.`nic_secondary_ips`";

        try (PreparedStatement pstmt = conn.prepareStatement(secondIpsSql);
             ResultSet rs1 = pstmt.executeQuery()
        ) {
            while (rs1.next()) {
                final long ipId = rs1.getLong(1);
                final long vmId = rs1.getLong(2);
                final long networkId = rs1.getLong(3);
                final long accountId = rs1.getLong(4);
                final long domainId = rs1.getLong(5);
                final String ipAddr = rs1.getString(6);

                try (PreparedStatement pstmtVm = conn.prepareStatement("SELECT account_id, domain_id FROM `cloud`.`vm_instance` where id = ?")) {
                    pstmtVm.setLong(1, vmId);

                    try (ResultSet vmRs = pstmtVm.executeQuery()) {

                        if (vmRs.next()) {
                            final long vmAccountId = vmRs.getLong(1);
                            final long vmDomainId = vmRs.getLong(2);

                            if (vmAccountId != accountId && vmAccountId != domainId) {
                                // update the secondary ip accountid and domainid to vm accountid domainid
                                // check the network type. If network is shared accountid doaminid needs to be updated in
                                // in both nic_secondary_ips table and user_ip_address table

                                try (PreparedStatement pstmtUpdate = conn.prepareStatement("UPDATE `cloud`.`nic_secondary_ips` SET account_id = ?, domain_id= ? WHERE id = ?")) {
                                    pstmtUpdate.setLong(1, vmAccountId);
                                    pstmtUpdate.setLong(2, vmDomainId);
                                    pstmtUpdate.setLong(3, ipId);
                                    pstmtUpdate.executeUpdate();
                                } catch (final SQLException e) {
                                    throw new CloudRuntimeException("Exception while updating secondary ip for nic " + ipId, e);
                                }

                                try (PreparedStatement pstmtNw = conn.prepareStatement("SELECT guest_type FROM `cloud`.`networks` where id = ?")) {
                                    pstmtNw.setLong(1, networkId);

                                    try (ResultSet networkRs = pstmtNw.executeQuery()) {
                                        if (networkRs.next()) {
                                            final String guesttype = networkRs.getString(1);

                                            if (guesttype.equals(Network.GuestType.Shared.toString())) {
                                                try (PreparedStatement pstmtUpdate = conn.prepareStatement("UPDATE `cloud`.`user_ip_address` SET account_id = ?, domain_id= ? " +
                                                        "WHERE public_ip_address = ?")) {
                                                    pstmtUpdate.setLong(1, vmAccountId);
                                                    pstmtUpdate.setLong(2, vmDomainId);
                                                    pstmtUpdate.setString(3, ipAddr);
                                                    pstmtUpdate.executeUpdate();
                                                } catch (final SQLException e) {
                                                    throw new CloudRuntimeException("Exception while updating public ip  " + ipAddr, e);
                                                }
                                            }
                                        }
                                    } catch (final SQLException e) {
                                        throw new CloudRuntimeException("Exception while retrieving guest type for network " + networkId, e);
                                    }
                                } catch (final SQLException e) {
                                    throw new CloudRuntimeException("Exception while retrieving guest type for network " + networkId, e);
                                }
                            } // if
                        } // if
                    }
                }
            } // while
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Exception while Moving private zone information to dedicated resources", e);
        }
        s_logger.debug("Done updating vm nic secondary ip  account and domain ids");
    }

    private void moveCidrsToTheirOwnTable(final Connection conn) {
        s_logger.debug("Moving network acl item cidrs to a row per cidr");

        final String networkAclItemSql = "SELECT id, cidr FROM `cloud`.`network_acl_item`";
        final String networkAclItemCidrSql = "INSERT INTO `cloud`.`network_acl_item_cidrs` (network_acl_item_id, cidr) VALUES (?,?)";

        try (PreparedStatement pstmtItem = conn.prepareStatement(networkAclItemSql);
             ResultSet rsItems = pstmtItem.executeQuery();
             PreparedStatement pstmtCidr = conn.prepareStatement(networkAclItemCidrSql)
        ) {

            // for each network acl item
            while (rsItems.next()) {
                final long itemId = rsItems.getLong(1);
                // get the source cidr list
                final String cidrList = rsItems.getString(2);
                s_logger.debug("Moving '" + cidrList + "' to a row per cidr");
                // split it
                final String[] cidrArray = cidrList.split(",");
                // insert a record per cidr
                pstmtCidr.setLong(1, itemId);
                for (final String cidr : cidrArray) {
                    pstmtCidr.setString(2, cidr);
                    pstmtCidr.executeUpdate();
                }
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Exception while Moving network acl item cidrs to a row per cidr", e);
        }
        s_logger.debug("Done moving network acl item cidrs to a row per cidr");
    }

    private void addExtractTemplateAndVolumeColumns(final Connection conn) {

        try (PreparedStatement selectTemplateInfostmt = conn.prepareStatement("SELECT *  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud' AND TABLE_NAME = " +
                "'template_store_ref' AND COLUMN_NAME = 'download_url_created'");
             ResultSet templateInfoResults = selectTemplateInfostmt.executeQuery();
             PreparedStatement addDownloadUrlCreatedToTemplateStorerefstatement = conn.prepareStatement("ALTER TABLE `cloud`.`template_store_ref` ADD COLUMN " +
                     "`download_url_created` datetime");
             PreparedStatement addDownloadUrlToTemplateStorerefstatement = conn.prepareStatement("ALTER TABLE `cloud`.`template_store_ref` ADD COLUMN `download_url` varchar(255)");
             PreparedStatement selectVolumeInfostmt = conn.prepareStatement("SELECT *  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud' AND TABLE_NAME = " +
                     "'volume_store_ref' AND COLUMN_NAME = 'download_url_created'");
             ResultSet volumeInfoResults = selectVolumeInfostmt.executeQuery();
             PreparedStatement addDownloadUrlCreatedToVolumeStorerefstatement = conn.prepareStatement("ALTER TABLE `cloud`.`volume_store_ref` ADD COLUMN `download_url_created` " +
                     "datetime")
        ) {

            // Add download_url_created, download_url to template_store_ref
            if (!templateInfoResults.next()) {
                addDownloadUrlCreatedToTemplateStorerefstatement.executeUpdate();
                addDownloadUrlToTemplateStorerefstatement.executeUpdate();
            }

            // Add download_url_created to volume_store_ref - note download_url already exists
            if (!volumeInfoResults.next()) {
                addDownloadUrlCreatedToVolumeStorerefstatement.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Adding columns for Extract Template And Volume functionality failed");
        }
    }

    private void updateVlanUris(final Connection conn) {
        s_logger.debug("updating vlan URIs");
        try (PreparedStatement selectstatement = conn.prepareStatement("SELECT id, vlan_id FROM `cloud`.`vlan` where vlan_id not like '%:%'");
             ResultSet results = selectstatement.executeQuery()) {

            while (results.next()) {
                final long id = results.getLong(1);
                final String vlan = results.getString(2);
                if (vlan == null || "".equals(vlan)) {
                    continue;
                }
                final String vlanUri = BroadcastDomainType.Vlan.toUri(vlan).toString();
                try (PreparedStatement updatestatement = conn.prepareStatement("update `cloud`.`vlan` set vlan_id=? where id=?")) {
                    updatestatement.setString(1, vlanUri);
                    updatestatement.setLong(2, id);
                    updatestatement.executeUpdate();
                } catch (final SQLException e) {
                    throw new CloudRuntimeException("Unable to update vlan URI " + vlanUri + " for vlan record " + id, e);
                }
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to update vlan URIs ", e);
        }
        s_logger.debug("Done updateing vlan URIs");
    }

    @Override
    public File[] getCleanupScripts() {
        final String script = Script.findScript("", "db/schema-430to440-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-430to440-cleanup.sql");
        }

        return new File[]{new File(script)};
    }
}
