package com.cloud.upgrade.dao;

import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Upgrade442to450 implements DbUpgrade {
    final static Logger s_logger = LoggerFactory.getLogger(Upgrade442to450.class);

    @Override
    public String[] getUpgradableVersionRange() {
        return new String[]{"4.4.2", "4.5.0"};
    }

    @Override
    public String getUpgradedVersion() {
        return "4.5.0";
    }

    @Override
    public boolean supportsRollingUpgrade() {
        return false;
    }

    @Override
    public File[] getPrepareScripts() {
        final String script = Script.findScript("", "db/schema-442to450.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-442to450.sql");
        }

        return new File[]{new File(script)};
    }

    @Override
    public void performDataMigration(final Connection conn) {
        dropInvalidKeyFromStoragePoolTable(conn);
        dropDuplicatedForeignKeyFromAsyncJobTable(conn);
        updateMaxRouterSizeConfig(conn);
        upgradeMemoryOfVirtualRoutervmOffering(conn);
        upgradeMemoryOfInternalLoadBalancervmOffering(conn);
    }

    @Override
    public File[] getCleanupScripts() {
        final String script = Script.findScript("", "db/schema-442to450-cleanup.sql");
        if (script == null) {
            throw new CloudRuntimeException("Unable to find db/schema-442to450-cleanup.sql");
        }

        return new File[]{new File(script)};
    }

    private void dropInvalidKeyFromStoragePoolTable(final Connection conn) {
        final HashMap<String, List<String>> uniqueKeys = new HashMap<>();
        final List<String> keys = new ArrayList<>();

        keys.add("id_2");
        uniqueKeys.put("storage_pool", keys);

        s_logger.debug("Dropping id_2 key from storage_pool table");
        for (final Map.Entry<String, List<String>> entry : uniqueKeys.entrySet()) {
            DbUpgradeUtils.dropKeysIfExist(conn, entry.getKey(), entry.getValue(), false);
        }
    }

    private void dropDuplicatedForeignKeyFromAsyncJobTable(final Connection conn) {
        final HashMap<String, List<String>> foreignKeys = new HashMap<>();
        final List<String> keys = new ArrayList<>();

        keys.add("fk_async_job_join_map__join_job_id");
        foreignKeys.put("async_job_join_map", keys);

        s_logger.debug("Dropping fk_async_job_join_map__join_job_id key from async_job_join_map table");
        for (final Map.Entry<String, List<String>> entry : foreignKeys.entrySet()) {
            DbUpgradeUtils.dropKeysIfExist(conn, entry.getKey(), entry.getValue(), true);
        }
    }

    private void updateMaxRouterSizeConfig(final Connection conn) {
        final String sqlUpdateConfig = "UPDATE `cloud`.`configuration` SET value=? WHERE name='router.ram.size' AND category='Hidden'";
        try (PreparedStatement updatePstmt = conn.prepareStatement(sqlUpdateConfig)) {
            final String encryptedValue = DBEncryptionUtil.encrypt("256");
            updatePstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
            updatePstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to upgrade max ram size of router in config.", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable encrypt configuration values ", e);
        }
        s_logger.debug("Done updating router.ram.size config to 256");
    }

    private void upgradeMemoryOfVirtualRoutervmOffering(final Connection conn) {
        final int newRamSize = 256; //256MB
        long serviceOfferingId = 0;

        /**
         * Pick first row in service_offering table which has system vm type as domainrouter. User added offerings would start from 2nd row onwards.
         * We should not update/modify any user-defined offering.
         */

        try (
                PreparedStatement selectPstmt = conn.prepareStatement("SELECT id FROM `cloud`.`service_offering` WHERE vm_type='domainrouter'");
                PreparedStatement updatePstmt = conn.prepareStatement("UPDATE `cloud`.`service_offering` SET ram_size=? WHERE id=?");
                ResultSet selectResultSet = selectPstmt.executeQuery()
        ) {
            if (selectResultSet.next()) {
                serviceOfferingId = selectResultSet.getLong("id");
            }

            updatePstmt.setInt(1, newRamSize);
            updatePstmt.setLong(2, serviceOfferingId);
            updatePstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to upgrade ram_size of service offering for domain router. ", e);
        }
        s_logger.debug("Done upgrading RAM for service offering of domain router to " + newRamSize);
    }

    private void upgradeMemoryOfInternalLoadBalancervmOffering(final Connection conn) {
        final int newRamSize = 256; //256MB
        long serviceOfferingId = 0;

        /**
         * Pick first row in service_offering table which has system vm type as internalloadbalancervm. User added offerings would start from 2nd row onwards.
         * We should not update/modify any user-defined offering.
         */

        try (PreparedStatement selectPstmt = conn.prepareStatement("SELECT id FROM `cloud`.`service_offering` WHERE vm_type='internalloadbalancervm'");
             PreparedStatement updatePstmt = conn.prepareStatement("UPDATE `cloud`.`service_offering` SET ram_size=? WHERE id=?");
             ResultSet selectResultSet = selectPstmt.executeQuery()) {
            if (selectResultSet.next()) {
                serviceOfferingId = selectResultSet.getLong("id");
            }

            updatePstmt.setInt(1, newRamSize);
            updatePstmt.setLong(2, serviceOfferingId);
            updatePstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to upgrade ram_size of service offering for internal loadbalancer vm. ", e);
        }
        s_logger.debug("Done upgrading RAM for service offering of internal loadbalancer vm to " + newRamSize);
    }
}
