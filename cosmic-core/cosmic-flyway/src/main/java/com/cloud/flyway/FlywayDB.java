package com.cloud.flyway;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class FlywayDB {

    private static final Logger logger = LoggerFactory.getLogger(FlywayDB.class);

    public void check() {
        logger.info("Start database migration");

        final InitialContext cxt;
        final DataSource dataSource;

        try {
            cxt = new InitialContext();
            dataSource = (DataSource) cxt.lookup("java:/comp/env/jdbc/cosmic");
        } catch (final NamingException e) {
            logger.error(e.getMessage(), e);
            throw new CloudRuntimeException(e.getMessage(), e);
        }

        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setTable("schema_version");
        flyway.setEncoding("UTF-8");

        try {
            flyway.migrate();
        } catch (final FlywayException e) {
            logger.error(e.getMessage(), e);
            throw new CloudRuntimeException(e.getMessage(), e);
        }

        logger.info("Database migration successful");
    }
}
