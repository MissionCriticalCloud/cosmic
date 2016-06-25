//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.UpgradeSnapshotCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.net.URI;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = UpgradeSnapshotCommand.class)
public final class CitrixUpgradeSnapshotCommandWrapper extends CommandWrapper<UpgradeSnapshotCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixUpgradeSnapshotCommandWrapper.class);

    @Override
    public Answer execute(final UpgradeSnapshotCommand command, final CitrixResourceBase citrixResourceBase) {
        final String secondaryStorageUrl = command.getSecondaryStorageUrl();
        final String backedUpSnapshotUuid = command.getSnapshotUuid();
        final Long volumeId = command.getVolumeId();
        final Long accountId = command.getAccountId();
        final Long templateId = command.getTemplateId();
        final Long tmpltAcountId = command.getTmpltAccountId();
        final String version = command.getVersion();

        if (!version.equals("2.1")) {
            return new Answer(command, true, "success");
        }
        try {
            final Connection conn = citrixResourceBase.getConnection();
            final URI uri = new URI(secondaryStorageUrl);
            final String secondaryStorageMountPath = uri.getHost() + ":" + uri.getPath();
            final String snapshotPath = secondaryStorageMountPath + "/snapshots/" + accountId + "/" + volumeId + "/" + backedUpSnapshotUuid + ".vhd";
            final String templatePath = secondaryStorageMountPath + "/template/tmpl/" + tmpltAcountId + "/" + templateId;
            citrixResourceBase.upgradeSnapshot(conn, templatePath, snapshotPath);
            return new Answer(command, true, "success");
        } catch (final Exception e) {
            final String details = "upgrading snapshot " + backedUpSnapshotUuid + " failed due to " + e.toString();
            s_logger.error(details, e);
        }
        return new Answer(command, false, "failure");
    }
}
