package org.apache.cloudstack.framework.sampleserver;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SampleManagementServerApp {

    public static void main(final String[] args) {
        setupLog4j();

        final ApplicationContext context = new ClassPathXmlApplicationContext("/resources/SampleManagementServerAppContext.xml");
        final SampleManagementServer server = context.getBean(SampleManagementServer.class);
        server.mainLoop();
    }

    private static void setupLog4j() {
        final URL configUrl = System.class.getResource("/resources/log4j-cloud.xml");
        if (configUrl != null) {
            System.out.println("Configure log4j using log4j-cloud.xml");

            try {
                final File file = new File(configUrl.toURI());

                System.out.println("Log4j configuration from : " + file.getAbsolutePath());
                DOMConfigurator.configureAndWatch(file.getAbsolutePath(), 10000);
            } catch (final URISyntaxException e) {
                System.out.println("Unable to convert log4j configuration Url to URI");
            }
        } else {
            System.out.println("Configure log4j with default properties");
        }
    }
}
