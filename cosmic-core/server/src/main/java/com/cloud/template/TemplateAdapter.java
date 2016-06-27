package com.cloud.template;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.utils.component.Adapter;
import org.apache.cloudstack.api.command.user.iso.DeleteIsoCmd;
import org.apache.cloudstack.api.command.user.iso.RegisterIsoCmd;
import org.apache.cloudstack.api.command.user.template.DeleteTemplateCmd;
import org.apache.cloudstack.api.command.user.template.ExtractTemplateCmd;
import org.apache.cloudstack.api.command.user.template.GetUploadParamsForTemplateCmd;
import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.apache.cloudstack.storage.command.TemplateOrVolumePostUploadCommand;

import java.util.List;
import java.util.Map;

public interface TemplateAdapter extends Adapter {
    TemplateProfile prepare(RegisterTemplateCmd cmd) throws ResourceAllocationException;

    TemplateProfile prepare(GetUploadParamsForTemplateCmd cmd) throws ResourceAllocationException;

    TemplateProfile prepare(RegisterIsoCmd cmd) throws ResourceAllocationException;

    VMTemplateVO create(TemplateProfile profile);

    List<TemplateOrVolumePostUploadCommand> createTemplateForPostUpload(TemplateProfile profile);

    TemplateProfile prepareDelete(DeleteTemplateCmd cmd);

    TemplateProfile prepareDelete(DeleteIsoCmd cmd);

    TemplateProfile prepareExtractTemplate(ExtractTemplateCmd cmd);

    boolean delete(TemplateProfile profile);

    TemplateProfile prepare(boolean isIso, Long userId, String name, String displayText, Integer bits, Boolean passwordEnabled, Boolean requiresHVM, String url,
                            Boolean isPublic, Boolean featured, Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType, String
                                    accountName,
                            Long domainId, String chksum, Boolean bootable, Map details) throws ResourceAllocationException;

    TemplateProfile prepare(boolean isIso, long userId, String name, String displayText, Integer bits, Boolean passwordEnabled, Boolean requiresHVM, String url,
                            Boolean isPublic, Boolean featured, Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType, String chksum,
                            Boolean bootable, String templateTag, Account templateOwner, Map details, Boolean sshKeyEnabled, String imageStoreUuid, Boolean isDynamicallyScalable,
                            TemplateType templateType) throws ResourceAllocationException;

    class TemplateAdapterType {
        public static final TemplateAdapterType Hypervisor = new TemplateAdapterType("HypervisorAdapter");
        String _name;

        public TemplateAdapterType(final String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }
}
