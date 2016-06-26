package com.cloud.usage;

import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class must not be used concurrently because its state changes often during
 * execution in a non synchronized way
 */
public class UsageSanityChecker {

    protected static final Logger s_logger = LoggerFactory.getLogger(UsageSanityChecker.class);
    protected static final int DEFAULT_AGGREGATION_RANGE = 1440;
    protected StringBuilder errors;
    protected List<CheckCase> checkCases;
    protected String lastCheckFile = "/usr/local/libexec/sanity-check-last-id";
    protected String lastCheckId = "";
    protected int lastId = -1;
    protected int maxId = -1;
    protected Connection conn;

    public static void main(final String[] args) {
        final UsageSanityChecker usc = new UsageSanityChecker();
        final String sanityErrors;
        try {
            sanityErrors = usc.runSanityCheck();
            if (sanityErrors.length() > 0) {
                s_logger.error(sanityErrors.toString());
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public String runSanityCheck() throws SQLException {

        readLastCheckId();
        if (lastId > 0) {
            lastCheckId = " and cu.id > ?";
        }

        conn = getConnection();
        readMaxId();

        reset();

        checkMaxUsage();
        checkVmUsage();
        checkVolumeUsage();
        checkTemplateISOUsage();
        checkSnapshotUsage();

        checkItemCountByPstmt();

        return errors.toString();
    }

    protected void readLastCheckId() {
        try (BufferedReader reader = new BufferedReader(new FileReader(lastCheckFile))) {
            String lastIdText = null;
            lastId = -1;
            if ((reader != null) && (lastIdText = reader.readLine()) != null) {
                lastId = Integer.parseInt(lastIdText);
            }
        } catch (final Exception e) {
            s_logger.error("readLastCheckId:Exception:" + e.getMessage(), e);
        }
    }

    /**
     * Local acquisition of {@link Connection} to remove static cling
     *
     * @return
     */
    protected Connection getConnection() {
        return TransactionLegacy.getStandaloneConnection();
    }

    protected void readMaxId() throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("select max(id) from cloud_usage.cloud_usage");
             ResultSet rs = pstmt.executeQuery()) {
            maxId = -1;
            if (rs.next() && (rs.getInt(1) > 0)) {
                maxId = rs.getInt(1);
                lastCheckId += " and cu.id <= ?";
            }
        } catch (final Exception e) {
            s_logger.error("readMaxId:" + e.getMessage(), e);
        }
    }

    protected void reset() {
        errors = new StringBuilder();
        checkCases = new ArrayList<>();
    }

    protected void checkMaxUsage() throws SQLException {
        int aggregationRange = DEFAULT_AGGREGATION_RANGE;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT value FROM `cloud`.`configuration` where name = 'usage.stats.job.aggregation.range'")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    aggregationRange = rs.getInt(1);
                } else {
                    s_logger.debug("Failed to retrieve aggregation range. Using default : " + aggregationRange);
                }
            } catch (final SQLException e) {
                s_logger.error("checkMaxUsage:Exception:" + e.getMessage());
                throw new CloudRuntimeException("checkMaxUsage:Exception:" + e.getMessage());
            }
        } catch (final SQLException e) {
            s_logger.error("checkMaxUsage:Exception:" + e.getMessage());
            throw new CloudRuntimeException("checkMaxUsage:Exception:" + e.getMessage());
        }
        final int aggregationHours = aggregationRange / 60;

        addCheckCase("SELECT count(*) FROM `cloud_usage`.`cloud_usage` cu where usage_type not in (4,5) and raw_usage > "
                        + aggregationHours,
                "usage records with raw_usage > " + aggregationHours,
                lastCheckId);
    }

    protected void checkVmUsage() {
        addCheckCase("select count(*) from cloud_usage.cloud_usage cu inner join cloud.vm_instance vm "
                        + "where vm.type = 'User' and cu.usage_type in (1 , 2) "
                        + "and cu.usage_id = vm.id and cu.start_date > vm.removed ",
                "Vm usage records which are created after Vm is destroyed",
                lastCheckId);

        addCheckCase("select sum(cnt) from (select count(*) as cnt from cloud_usage.usage_vm_instance "
                        + "where usage_type =1 and end_date is null group by vm_instance_id "
                        + "having count(vm_instance_id) > 1) c ;",
                "duplicate running Vm entries in vm usage helper table");

        addCheckCase("select sum(cnt) from (select count(*) as cnt from cloud_usage.usage_vm_instance "
                        + "where usage_type =2 and end_date is null group by vm_instance_id "
                        + "having count(vm_instance_id) > 1) c ;",
                "duplicate allocated Vm entries in vm usage helper table");

        addCheckCase("select count(vm_instance_id) from cloud_usage.usage_vm_instance o "
                        + "where o.end_date is null and o.usage_type=1 and not exists "
                        + "(select 1 from cloud_usage.usage_vm_instance i where "
                        + "i.vm_instance_id=o.vm_instance_id and usage_type=2 and i.end_date is null)",
                "running Vm entries without corresponding allocated entries in vm usage helper table");
    }

    protected void checkVolumeUsage() {
        addCheckCase("select count(*) from cloud_usage.cloud_usage cu inner join cloud.volumes v where "
                        + "cu.usage_type = 6 and cu.usage_id = v.id and cu.start_date > v.removed ",
                "volume usage records which are created after volume is removed",
                lastCheckId);

        addCheckCase("select sum(cnt) from (select count(*) as cnt from cloud_usage.usage_volume "
                        + "where deleted is null group by id having count(id) > 1) c;",
                "duplicate records in volume usage helper table");
    }

    protected void checkTemplateISOUsage() {
        addCheckCase("select count(*) from cloud_usage.cloud_usage cu inner join cloud.template_zone_ref tzr where "
                        + "cu.usage_id = tzr.template_id and cu.zone_id = tzr.zone_id and cu.usage_type in (7,8) and cu.start_date > tzr.removed ",
                "template/ISO usage records which are created after it is removed",
                lastCheckId);
    }

    protected void checkSnapshotUsage() {
        addCheckCase("select count(*) from cloud_usage.cloud_usage cu inner join cloud.snapshots s where "
                        + "cu.usage_id = s.id and cu.usage_type = 9 and cu.start_date > s.removed ",
                "snapshot usage records which are created after it is removed",
                lastCheckId);
    }

    protected boolean checkItemCountByPstmt() throws SQLException {
        boolean checkOk = true;

        for (final CheckCase check : checkCases) {
            checkOk &= checkItemCountByPstmt(check);
        }

        return checkOk;
    }

    protected void addCheckCase(final String sqlTemplate, final String itemName, final String lastCheckId) {
        checkCases.add(new CheckCase(sqlTemplate, itemName, lastCheckId));
    }

    protected void addCheckCase(final String sqlTemplate, final String itemName) {
        checkCases.add(new CheckCase(sqlTemplate, itemName));
    }

    protected boolean checkItemCountByPstmt(final CheckCase checkCase) throws SQLException {
        boolean checkOk = true;
        /*
         * Check for item usage records which are created after it is removed
         */
        try (PreparedStatement pstmt = conn.prepareStatement(checkCase.sqlTemplate)) {
            if (checkCase.checkId) {
                pstmt.setInt(1, lastId);
                pstmt.setInt(2, maxId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && (rs.getInt(1) > 0)) {
                    errors.append(String.format("Error: Found %s %s\n", rs.getInt(1), checkCase.itemName));
                    checkOk = false;
                }
            } catch (final Exception e) {
                s_logger.error("checkItemCountByPstmt:Exception:" + e.getMessage());
                throw new CloudRuntimeException("checkItemCountByPstmt:Exception:" + e.getMessage(), e);
            }
        } catch (final Exception e) {
            s_logger.error("checkItemCountByPstmt:Exception:" + e.getMessage());
            throw new CloudRuntimeException("checkItemCountByPstmt:Exception:" + e.getMessage(), e);
        }
        return checkOk;
    }

    protected void updateNewMaxId() {
        try (FileWriter fstream = new FileWriter(lastCheckFile);
             BufferedWriter out = new BufferedWriter(fstream)
        ) {
            out.write("" + maxId);
        } catch (final IOException e) {
            s_logger.error("updateNewMaxId:Exception:" + e.getMessage());
            // Error while writing last check id
        }
    }
}

/**
 * Just an abstraction of the kind of check to repeat across these cases
 * encapsulating what change for each specific case
 */
class CheckCase {
    public String sqlTemplate;
    public String itemName;
    public boolean checkId = false;

    public CheckCase(final String sqlTemplate, final String itemName, final String lastCheckId) {
        checkId = true;
        this.sqlTemplate = sqlTemplate + lastCheckId;
        this.itemName = itemName;
    }

    public CheckCase(final String sqlTemplate, final String itemName) {
        checkId = false;
        this.sqlTemplate = sqlTemplate;
        this.itemName = itemName;
    }
}
