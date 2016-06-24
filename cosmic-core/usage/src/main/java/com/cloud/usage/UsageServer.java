// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.usage;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.ComponentContext;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

public class UsageServer implements Daemon {
    public static final String Name = "usage-server";
    private static final Logger s_logger = LoggerFactory.getLogger(UsageServer.class.getName());
    private UsageManager mgr;
    private ClassPathXmlApplicationContext appContext;

    /**
     * @param args
     */
    public static void main(final String[] args) {
        initLog4j();
        final UsageServer usage = new UsageServer();
        usage.start();
    }

    static private void initLog4j() {
        File file = PropertiesUtil.findConfigFile("log4j-cloud.xml");
        if (file != null) {
            System.out.println("log4j configuration found at " + file.getAbsolutePath());
            try {
                Log4jConfigurer.initLogging(file.getAbsolutePath());
            } catch (final FileNotFoundException e) {
                s_logger.info("[ignored] log initialisation ;)" + e.getLocalizedMessage(), e);
            }
            DOMConfigurator.configureAndWatch(file.getAbsolutePath());
        } else {
            file = PropertiesUtil.findConfigFile("log4j-cloud.properties");
            if (file != null) {
                System.out.println("log4j configuration found at " + file.getAbsolutePath());
                try {
                    Log4jConfigurer.initLogging(file.getAbsolutePath());
                } catch (final FileNotFoundException e) {
                    s_logger.info("[ignored] log properties initialization :)" + e.getLocalizedMessage(), e);
                }
                PropertyConfigurator.configureAndWatch(file.getAbsolutePath());
            }
        }
    }

    @Override
    public void init(final DaemonContext arg0) throws DaemonInitException, Exception {
        initLog4j();
    }

    @Override
    public void start() {

        appContext = new ClassPathXmlApplicationContext("usageApplicationContext.xml");

        try {
            ComponentContext.initComponentsLifeCycle();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        mgr = appContext.getBean(UsageManager.class);

        if (mgr != null) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("UsageServer ready...");
            }
        }
    }

    @Override
    public void stop() {
        appContext.close();
    }

    @Override
    public void destroy() {

    }
}
