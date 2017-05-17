package com.cloud.upgrade;

import com.cloud.upgrade.dao.DbUpgrade;
import com.cloud.upgrade.dao.Upgrade40to41;
import com.cloud.upgrade.dao.Upgrade410to420;
import com.cloud.upgrade.dao.Upgrade420to421;
import com.cloud.upgrade.dao.Upgrade421to430;
import com.cloud.upgrade.dao.Upgrade430to440;
import com.cloud.upgrade.dao.Upgrade431to440;
import com.cloud.upgrade.dao.Upgrade432to440;
import com.cloud.upgrade.dao.Upgrade440to441;
import com.cloud.upgrade.dao.Upgrade441to442;
import com.cloud.upgrade.dao.Upgrade442to450;
import com.cloud.upgrade.dao.Upgrade443to450;
import com.cloud.upgrade.dao.Upgrade444to450;
import com.cloud.upgrade.dao.Upgrade450to451;
import com.cloud.upgrade.dao.Upgrade451to452;
import com.cloud.upgrade.dao.Upgrade452to460;
import com.cloud.upgrade.dao.Upgrade453to460;
import com.cloud.upgrade.dao.Upgrade460to461;
import com.cloud.upgrade.dao.Upgrade461to470;
import com.cloud.upgrade.dao.Upgrade470to471;
import com.cloud.upgrade.dao.Upgrade471to480;
import com.cloud.upgrade.dao.Upgrade480to500;
import com.cloud.upgrade.dao.Upgrade500to501;
import com.cloud.upgrade.dao.Upgrade501to510;
import com.cloud.upgrade.dao.Upgrade510to511;
import com.cloud.upgrade.dao.Upgrade511to520;
import com.cloud.upgrade.dao.Upgrade520to530;
import com.cloud.upgrade.dao.Upgrade530to531;
import com.cloud.upgrade.dao.Upgrade531to532;
import com.cloud.upgrade.dao.Upgrade532to533;
import com.cloud.upgrade.dao.Upgrade533to534;
import com.cloud.upgrade.dao.Upgrade534to535;
import com.cloud.upgrade.dao.VersionDao;
import com.cloud.upgrade.dao.VersionDaoImpl;
import com.cloud.upgrade.dao.VersionVO;
import com.cloud.upgrade.dao.VersionVO.Step;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.ScriptRunner;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.maint.Version;

import javax.ejb.Local;
import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Local(value = {SystemIntegrityChecker.class})
public class DatabaseUpgradeChecker implements SystemIntegrityChecker {
    private static final Logger s_logger = LoggerFactory.getLogger(DatabaseUpgradeChecker.class);

    protected HashMap<String, DbUpgrade[]> _upgradeMap = new HashMap<>();

    @Inject
    VersionDao _dao;

    public DatabaseUpgradeChecker() {
        _dao = new VersionDaoImpl();

        _upgradeMap.put("4.0.0", new DbUpgrade[]{new Upgrade40to41(), new Upgrade410to420(), new Upgrade420to421(), new Upgrade421to430(), new Upgrade430to440(), new
                Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new
                Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.0.1", new DbUpgrade[]{new Upgrade40to41(), new Upgrade410to420(), new Upgrade420to421(), new Upgrade421to430(), new Upgrade430to440(), new
                Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new
                Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.0.2", new DbUpgrade[]{new Upgrade40to41(), new Upgrade410to420(), new Upgrade420to421(), new Upgrade421to430(), new Upgrade430to440(), new
                Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new
                Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.1.0", new DbUpgrade[]{new Upgrade410to420(), new Upgrade420to421(), new Upgrade421to430(), new Upgrade430to440(), new Upgrade440to441(), new
                Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new
                Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new
                Upgrade520to530(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.1.1", new DbUpgrade[]{new Upgrade410to420(), new Upgrade420to421(), new Upgrade421to430(), new Upgrade430to440(), new Upgrade440to441(), new
                Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new
                Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new
                Upgrade520to530(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.2.0", new DbUpgrade[]{new Upgrade420to421(), new Upgrade421to430(), new Upgrade430to440(), new Upgrade440to441(), new Upgrade441to442(), new
                Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new
                Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new
                Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.2.1", new DbUpgrade[]{new Upgrade421to430(), new Upgrade430to440(), new Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new
                Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new
                Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new
                Upgrade531to532(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.3.0", new DbUpgrade[]{new Upgrade430to440(), new Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new
                Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new
                Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new
                Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.3.1", new DbUpgrade[]{new Upgrade431to440(), new Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new
                Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new
                Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new
                Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.3.2", new DbUpgrade[]{new Upgrade432to440(), new Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new
                Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new
                Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new
                Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.4.0", new DbUpgrade[]{new Upgrade440to441(), new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new
                Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new
                Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new
                Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.4.1", new DbUpgrade[]{new Upgrade441to442(), new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new
                Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new
                Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new
                Upgrade534to535()});

        _upgradeMap.put("4.4.2", new DbUpgrade[]{new Upgrade442to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new
                Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.4.3", new DbUpgrade[]{new Upgrade443to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new
                Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.4.4", new DbUpgrade[]{new Upgrade444to450(), new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new
                Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.5.0", new DbUpgrade[]{new Upgrade450to451(), new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new
                Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new
                Upgrade520to530(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.5.1", new DbUpgrade[]{new Upgrade451to452(), new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new
                Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new
                Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.5.2", new DbUpgrade[]{new Upgrade452to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new
                Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new
                Upgrade531to532(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.5.3", new DbUpgrade[]{new Upgrade453to460(), new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new
                Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new
                Upgrade531to532(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.6.0", new DbUpgrade[]{new Upgrade460to461(), new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new
                Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new
                Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.6.1", new DbUpgrade[]{new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new
                Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new
                Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.6.2", new DbUpgrade[]{new Upgrade461to470(), new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new
                Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new
                Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.7.0", new DbUpgrade[]{new Upgrade470to471(), new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new
                Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new
                Upgrade534to535()});

        _upgradeMap.put("4.7.1", new DbUpgrade[]{new Upgrade471to480(), new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new
                Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("4.8.0", new DbUpgrade[]{new Upgrade480to500(), new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new
                Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.0.0", new DbUpgrade[]{new Upgrade500to501(), new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new
                Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.0.1", new DbUpgrade[]{new Upgrade501to510(), new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new
                Upgrade531to532(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.1.0", new DbUpgrade[]{new Upgrade510to511(), new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new
                Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.1.1", new DbUpgrade[]{new Upgrade511to520(), new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new
                Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.2.0", new DbUpgrade[]{new Upgrade520to530(), new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new
                Upgrade534to535()});

        _upgradeMap.put("5.3.0", new DbUpgrade[]{new Upgrade530to531(), new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.3.1", new DbUpgrade[]{new Upgrade531to532(), new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.3.2", new DbUpgrade[]{new Upgrade532to533(), new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.3.3", new DbUpgrade[]{new Upgrade533to534(), new Upgrade534to535()});

        _upgradeMap.put("5.3.4", new DbUpgrade[]{new Upgrade534to535()});
    }

    @Override
    public void check() {
        final GlobalLock lock = GlobalLock.getInternLock("DatabaseUpgrade");
        try {
            s_logger.info("Grabbing lock to check for database upgrade.");
            if (!lock.lock(20 * 60)) {
                throw new CloudRuntimeException("Unable to acquire lock to check for database integrity.");
            }

            try {
                final String dbVersion = _dao.getCurrentVersion();
                final String currentVersion = this.getClass().getPackage().getImplementationVersion();

                if (currentVersion == null) {
                    return;
                }

                s_logger.info("DB version = " + dbVersion + " Code Version = " + currentVersion);

                if (Version.compare(Version.trimToPatch(dbVersion), Version.trimToPatch(currentVersion)) > 0) {
                    throw new CloudRuntimeException("Database version " + dbVersion + " is higher than management software version " + currentVersion);
                }

                if (Version.compare(Version.trimToPatch(dbVersion), Version.trimToPatch(currentVersion)) == 0) {
                    s_logger.info("DB version and code version matches so no upgrade needed.");
                    return;
                }

                upgrade(dbVersion, currentVersion);
            } finally {
                lock.unlock();
            }
        } finally {
            lock.releaseRef();
        }
    }

    protected void upgrade(final String dbVersion, final String currentVersion) {
        s_logger.info("Database upgrade must be performed from " + dbVersion + " to " + currentVersion);

        final String trimmedDbVersion = Version.trimToPatch(dbVersion);
        final String trimmedCurrentVersion = Version.trimToPatch(currentVersion);

        final DbUpgrade[] upgrades = _upgradeMap.get(trimmedDbVersion);
        if (upgrades == null) {
            s_logger.error("There is no upgrade path from " + dbVersion + " to " + currentVersion);
            throw new CloudRuntimeException("There is no upgrade path from " + dbVersion + " to " + currentVersion);
        }

        for (final DbUpgrade upgrade : upgrades) {
            VersionVO version;
            if (Version.compare(upgrade.getUpgradedVersion(), trimmedCurrentVersion) > 0) {
                break;
            }
            s_logger.debug("Running upgrade " + upgrade.getClass().getSimpleName() + " to upgrade from " + upgrade.getUpgradableVersionRange()[0] + "-" +
                    upgrade.getUpgradableVersionRange()[1] + " to " + upgrade.getUpgradedVersion());
            TransactionLegacy txn = TransactionLegacy.open("Upgrade");
            txn.start();
            try {
                final Connection conn;
                try {
                    conn = txn.getConnection();
                } catch (final SQLException e) {
                    final String errorMessage = "Unable to upgrade the database";
                    s_logger.error(errorMessage, e);
                    throw new CloudRuntimeException(errorMessage, e);
                }
                final File[] scripts = upgrade.getPrepareScripts();
                if (scripts != null) {
                    for (final File script : scripts) {
                        runScript(conn, script);
                    }
                }

                upgrade.performDataMigration(conn);
                version = new VersionVO(upgrade.getUpgradedVersion());
                version = _dao.persist(version);

                txn.commit();
            } catch (final CloudRuntimeException e) {
                final String errorMessage = "Unable to upgrade the database";
                s_logger.error(errorMessage, e);
                throw new CloudRuntimeException(errorMessage, e);
            } finally {
                txn.close();
            }

            // Run the corresponding '-cleanup.sql' script
            txn = TransactionLegacy.open("Cleanup");
            try {
                s_logger.info("Cleanup upgrade " + upgrade.getClass().getSimpleName() + " to upgrade from " + upgrade.getUpgradableVersionRange()[0] + "-" +
                        upgrade.getUpgradableVersionRange()[1] + " to " + upgrade.getUpgradedVersion());

                txn.start();

                Connection conn;
                try {
                    conn = txn.getConnection();
                } catch (SQLException e) {
                    s_logger.error("Unable to cleanup the database", e);
                    throw new CloudRuntimeException("Unable to cleanup the database", e);
                }

                File[] scripts = upgrade.getCleanupScripts();
                if (scripts != null) {
                    for (File script : scripts) {
                        runScript(conn, script);
                        s_logger.debug("Cleanup script " + script.getAbsolutePath() + " is executed successfully");
                    }
                }
                txn.commit();

                txn.start();
                version.setStep(Step.Complete);
                version.setUpdated(new Date());
                _dao.update(version.getId(), version);
                txn.commit();
                s_logger.debug("Upgrade completed for version " + version.getVersion());
            } finally {
                txn.close();
            }
        }
    }

    protected void runScript(final Connection conn, final File file) {

        try (FileReader reader = new FileReader(file)) {
            s_logger.info("Running DB script: " + file.getName());
            final ScriptRunner runner = new ScriptRunner(conn, false, true);
            runner.runScript(reader);
        } catch (final FileNotFoundException e) {
            s_logger.error("Unable to find upgrade script: " + file.getAbsolutePath(), e);
            throw new CloudRuntimeException("Unable to find upgrade script: " + file.getAbsolutePath(), e);
        } catch (final IOException e) {
            s_logger.error("Unable to read upgrade script: " + file.getAbsolutePath(), e);
            throw new CloudRuntimeException("Unable to read upgrade script: " + file.getAbsolutePath(), e);
        } catch (final SQLException e) {
            s_logger.error("Unable to execute upgrade script: " + file.getAbsolutePath(), e);
            throw new CloudRuntimeException("Unable to execute upgrade script: " + file.getAbsolutePath(), e);
        }
    }
}
