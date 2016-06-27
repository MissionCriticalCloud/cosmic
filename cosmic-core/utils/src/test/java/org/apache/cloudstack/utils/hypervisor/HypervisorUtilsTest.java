//

//

package org.apache.cloudstack.utils.hypervisor;

import com.cloud.utils.exception.CloudRuntimeException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HypervisorUtilsTest {

    final long _minFileSize = 10485760L;

    @Test
    public void checkVolumeFileForActivitySmallFileTest() throws IOException {
        System.out.print("Testing don't block on newly created clones - ");
        final String filePath = "./testsmallfileinactive";
        final int timeoutSeconds = 5;
        final long thresholdMilliseconds = 2000;
        final File file = new File(filePath);

        final long startTime = setupcheckVolumeFileForActivityFile(file, 0);
        HypervisorUtils.checkVolumeFileForActivity(filePath, timeoutSeconds, thresholdMilliseconds, _minFileSize);
        final long endTime = System.currentTimeMillis();

        Assert.assertEquals(startTime, endTime, 1000L);
        System.out.println("pass");

        file.delete();
    }

    private long setupcheckVolumeFileForActivityFile(final File file, final long minSize) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        final char[] chars = new char[1048576];
        Arrays.fill(chars, 'X');
        long written = 0;
        final FileWriter writer = new FileWriter(file);
        while (written < minSize) {
            writer.write(chars);
            written += chars.length;
        }
        final long creationTime = System.currentTimeMillis();
        writer.close();
        return creationTime;
    }

    @Test
    public void checkVolumeFileForActivityTest() throws IOException {
        System.out.print("Testing block on modified files - ");
        final String filePath = "./testfileinactive";
        final int timeoutSeconds = 5;
        final long thresholdMilliseconds = 2000;
        final File file = new File(filePath);

        final long startTime = setupcheckVolumeFileForActivityFile(file, _minFileSize);
        HypervisorUtils.checkVolumeFileForActivity(filePath, timeoutSeconds, thresholdMilliseconds, _minFileSize);
        final long duration = System.currentTimeMillis() - startTime;

        Assert.assertFalse("Didn't block long enough, expected at least " + thresholdMilliseconds + " and got " + duration, duration < thresholdMilliseconds);
        System.out.println("pass");

        file.delete();
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkVolumeFileForActivityTimeoutTest() throws IOException {
        System.out.print("Testing timeout of blocking on modified files - ");
        final String filePath = "./testfileinactive";
        final int timeoutSeconds = 3;
        final long thresholdMilliseconds = 5000;
        final File file = new File(filePath);
        setupcheckVolumeFileForActivityFile(file, _minFileSize);

        try {
            HypervisorUtils.checkVolumeFileForActivity(filePath, timeoutSeconds, thresholdMilliseconds, _minFileSize);
        } catch (final CloudRuntimeException ex) {
            System.out.println("pass");
            throw ex;
        } finally {
            file.delete();
        }
        System.out.println("Fail");
    }
}
