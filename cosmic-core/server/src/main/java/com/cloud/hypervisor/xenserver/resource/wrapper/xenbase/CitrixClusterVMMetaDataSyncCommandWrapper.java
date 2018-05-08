package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.ClusterVMMetaDataSyncAnswer;
import com.cloud.legacymodel.communication.command.ClusterVMMetaDataSyncCommand;

import java.util.HashMap;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;
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
        } catch (final XmlRpcException | XenAPIException e) {
            s_logger.warn("Check for master failed, failing the Cluster sync VMMetaData command");
            return new ClusterVMMetaDataSyncAnswer(command.getClusterId(), null);
        }
        final HashMap<String, String> vmMetadatum = citrixResourceBase.clusterVMMetaDataSync(conn);
        return new ClusterVMMetaDataSyncAnswer(command.getClusterId(), vmMetadatum);
    }
}
