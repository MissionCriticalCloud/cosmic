//

//

package com.cloud.utils.testcase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4jEnabledTestCase extends TestCase {
    public static int getRandomMilliseconds(final int rangeLo, final int rangeHi) {
        final int i = new Random().nextInt();

        final long pos = (long) i - (long) Integer.MIN_VALUE;
        final long iRange = (long) Integer.MAX_VALUE - (long) Integer.MIN_VALUE;
        return rangeLo + (int) ((rangeHi - rangeLo) * pos / iRange);
    }

    @Override
    protected void setUp() {
        final URL configUrl = System.class.getResource("/conf/log4j-cloud.xml");
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
