package com.cloud.spring.module.web;

import com.cloud.spring.module.factory.CloudStackSpringContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

public class CloudStackContextLoaderListener extends ContextLoaderListener {

    public static final String WEB_PARENT_MODULE = "parentModule";
    public static final String WEB_PARENT_MODULE_DEFAULT = "web";

    private static final Logger log = LoggerFactory.getLogger(CloudStackContextLoaderListener.class);

    CloudStackSpringContext cloudStackContext;
    String configuredParentName;

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        try {
            cloudStackContext = new CloudStackSpringContext();
            cloudStackContext.registerShutdownHook();
            event.getServletContext().setAttribute(CloudStackSpringContext.CLOUDSTACK_CONTEXT_SERVLET_KEY, cloudStackContext);
        } catch (final IOException e) {
            log.error("Failed to start CloudStack", e);
            throw new RuntimeException("Failed to initialize CloudStack Spring modules", e);
        }

        configuredParentName = event.getServletContext().getInitParameter(WEB_PARENT_MODULE);
        if (configuredParentName == null) {
            configuredParentName = WEB_PARENT_MODULE_DEFAULT;
        }

        super.contextInitialized(event);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();

        Driver driver = null;

        while (drivers.hasMoreElements()) {
            try {
                driver = drivers.nextElement();
                DriverManager.deregisterDriver(driver);
            } catch (final SQLException e) {
                log.warn("Deregistration of driver failed.", e);
            }
        }
    }

    @Override
    protected void customizeContext(final ServletContext servletContext, final ConfigurableWebApplicationContext applicationContext) {
        super.customizeContext(servletContext, applicationContext);

        final String[] newLocations = cloudStackContext.getConfigLocationsForWeb(configuredParentName, applicationContext.getConfigLocations());

        applicationContext.setConfigLocations(newLocations);
    }

    @Override
    protected ApplicationContext loadParentContext(final ServletContext servletContext) {
        return cloudStackContext.getApplicationContextForWeb(configuredParentName);
    }
}
