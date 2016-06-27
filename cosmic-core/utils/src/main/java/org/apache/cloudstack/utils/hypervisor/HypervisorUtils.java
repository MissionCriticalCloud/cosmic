//

//

package org.apache.cloudstack.utils.hypervisor;

import com.cloud.utils.exception.CloudRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypervisorUtils {
    public static final Logger s_logger = LoggerFactory.getLogger(HypervisorUtils.class);

    public static void checkVolumeFileForActivity(final String filePath, final int timeoutSeconds, final long inactiveThresholdMilliseconds, final long minimumFileSize) throws
            IOException {
        final File file = new File(filePath);
        if (!file.exists()) {
            throw new CloudRuntimeException("File " + file.getAbsolutePath() + " not found");
        }
        if (file.length() < minimumFileSize) {
            s_logger.debug("VM disk file too small, fresh clone? skipping modify check");
            return;
        }
        int waitedSeconds = 0;
        final int intervalSeconds = 1;
        while (true) {
            final BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            final long modifyIdle = System.currentTimeMillis() - attrs.lastModifiedTime().toMillis();
            final long accessIdle = System.currentTimeMillis() - attrs.lastAccessTime().toMillis();
            if (modifyIdle > inactiveThresholdMilliseconds && accessIdle > inactiveThresholdMilliseconds) {
                s_logger.debug("File " + filePath + " has not been accessed or modified for at least " + inactiveThresholdMilliseconds + " ms");
                return;
            } else {
                s_logger.debug("File was modified " + modifyIdle + "ms ago, accessed " + accessIdle + "ms ago, waiting for inactivity threshold of "
                        + inactiveThresholdMilliseconds + "ms or timeout of " + timeoutSeconds + "s (waited " + waitedSeconds + "s)");
            }
            try {
                TimeUnit.SECONDS.sleep(intervalSeconds);
            } catch (final InterruptedException ex) {
                throw new CloudRuntimeException("Interrupted while waiting for activity on " + filePath + " to subside", ex);
            }
            waitedSeconds += intervalSeconds;
            if (waitedSeconds >= timeoutSeconds) {
                throw new CloudRuntimeException("Reached timeout while waiting for activity on " + filePath + " to subside");
            }
        }
    }
}
