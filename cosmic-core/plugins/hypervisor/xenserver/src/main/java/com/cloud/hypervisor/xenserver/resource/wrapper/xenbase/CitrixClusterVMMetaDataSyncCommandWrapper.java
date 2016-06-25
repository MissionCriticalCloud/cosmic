//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ClusterVMMetaDataSyncAnswer;
import com.cloud.agent.api.ClusterVMMetaDataSyncCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.HashMap;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = ClusterVMMetaDataSyncCommand.class)
public final class CitrixClusterVMMetaDataSyncCommandWrapper extends CommandWrapper<ClusterVMMetaDataSyncCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixClusterVMMetaDataSyncCommandWrapper.class);

    @Override
    public Answer execute(final ClusterVMMetaDataSyncCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        //check if this is master
        try {
            final Pool pool = Pool.getByUuid(conn, citrixResourceBase.getHost().getPool());
            final Pool.Record poolr = pool.getRecord(conn);
            final Host.Record hostr = poolr.master.getRecord(conn);
            if (!citrixResourceBase.getHost().getUuid().equals(hostr.uuid)) {
                return new ClusterVMMetaDataSyncAnswer(command.getClusterId(), null);
            }
        } catch (final Throwable e) {
            s_logger.warn("Check for master failed, failing the Cluster sync VMMetaData command");
            return new ClusterVMMetaDataSyncAnswer(command.getClusterId(), null);
        }
        final HashMap<String, String> vmMetadatum = citrixResourceBase.clusterVMMetaDataSync(conn);
        return new ClusterVMMetaDataSyncAnswer(command.getClusterId(), vmMetadatum);
    }
}
