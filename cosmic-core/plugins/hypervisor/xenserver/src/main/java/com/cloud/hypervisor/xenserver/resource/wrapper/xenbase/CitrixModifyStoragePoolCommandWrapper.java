//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.template.TemplateProp;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.HashMap;
import java.util.Map;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Types.XenAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = ModifyStoragePoolCommand.class)
public final class CitrixModifyStoragePoolCommandWrapper extends CommandWrapper<ModifyStoragePoolCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixModifyStoragePoolCommandWrapper.class);

    @Override
    public Answer execute(final ModifyStoragePoolCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final StorageFilerTO pool = command.getPool();
        final boolean add = command.getAdd();
        if (add) {
            try {
                final SR sr = citrixResourceBase.getStorageRepository(conn, pool.getUuid());
                citrixResourceBase.setupHeartbeatSr(conn, sr, false);
                final long capacity = sr.getPhysicalSize(conn);
                final long available = capacity - sr.getPhysicalUtilisation(conn);
                if (capacity == -1) {
                    final String msg = "Pool capacity is -1! pool: " + pool.getHost() + pool.getPath();
                    s_logger.warn(msg);
                    return new Answer(command, false, msg);
                }
                final Map<String, TemplateProp> tInfo = new HashMap<>();
                final ModifyStoragePoolAnswer answer = new ModifyStoragePoolAnswer(command, capacity, available, tInfo);
                return answer;
            } catch (final XenAPIException e) {
                final String msg = "ModifyStoragePoolCommand add XenAPIException:" + e.toString() + " host:" + citrixResourceBase.getHost().getUuid() + " pool: " + pool.getHost()
                        + pool.getPath();
                s_logger.warn(msg, e);
                return new Answer(command, false, msg);
            } catch (final Exception e) {
                final String msg = "ModifyStoragePoolCommand add XenAPIException:" + e.getMessage() + " host:" + citrixResourceBase.getHost().getUuid() + " pool: "
                        + pool.getHost() + pool.getPath();
                s_logger.warn(msg, e);
                return new Answer(command, false, msg);
            }
        } else {
            try {
                final SR sr = citrixResourceBase.getStorageRepository(conn, pool.getUuid());
                final String srUuid = sr.getUuid(conn);
                final String result = citrixResourceBase.callHostPluginPremium(conn, "setup_heartbeat_file", "host", citrixResourceBase.getHost().getUuid(), "sr", srUuid, "add",
                        "false");
                if (result == null || !result.split("#")[1].equals("0")) {
                    throw new CloudRuntimeException("Unable to remove heartbeat file entry for SR " + srUuid + " due to " + result);
                }
                return new Answer(command, true, "seccuss");
            } catch (final XenAPIException e) {
                final String msg = "ModifyStoragePoolCommand remove XenAPIException:" + e.toString() + " host:" + citrixResourceBase.getHost().getUuid() + " pool: "
                        + pool.getHost() + pool.getPath();
                s_logger.warn(msg, e);
                return new Answer(command, false, msg);
            } catch (final Exception e) {
                final String msg = "ModifyStoragePoolCommand remove XenAPIException:" + e.getMessage() + " host:" + citrixResourceBase.getHost().getUuid() + " pool: "
                        + pool.getHost() + pool.getPath();
                s_logger.warn(msg, e);
                return new Answer(command, false, msg);
            }
        }
    }
}
