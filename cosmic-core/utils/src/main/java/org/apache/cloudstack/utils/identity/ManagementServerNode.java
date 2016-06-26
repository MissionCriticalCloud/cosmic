//

//

package org.apache.cloudstack.utils.identity;

import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.ComponentLifecycle;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.MacAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementServerNode extends AdapterBase implements SystemIntegrityChecker {
    private static final Logger s_logger = LoggerFactory.getLogger(ManagementServerNode.class);

    private static final long s_nodeId = MacAddress.getMacAddress().toLong();

    public ManagementServerNode() {
        setRunLevel(ComponentLifecycle.RUN_LEVEL_FRAMEWORK_BOOTSTRAP);
    }

    public static long getManagementServerId() {
        return s_nodeId;
    }

    @Override
    public boolean start() {
        try {
            check();
        } catch (final Exception e) {
            s_logger.error("System integrity check exception", e);
            System.exit(1);
        }
        return true;
    }

    @Override
    public void check() {
        if (s_nodeId <= 0) {
            throw new CloudRuntimeException("Unable to get the management server node id");
        }
    }
}
