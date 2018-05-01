package com.cloud.template;

import com.cloud.api.BaseListTemplateOrIsoPermissionsCmd;
import com.cloud.api.BaseUpdateTemplateOrIsoPermissionsCmd;
import com.cloud.api.command.user.iso.DeleteIsoCmd;
import com.cloud.api.command.user.iso.ExtractIsoCmd;
import com.cloud.api.command.user.iso.RegisterIsoCmd;
import com.cloud.api.command.user.iso.UpdateIsoCmd;
import com.cloud.api.command.user.template.CopyTemplateCmd;
import com.cloud.api.command.user.template.CreateTemplateCmd;
import com.cloud.api.command.user.template.DeleteTemplateCmd;
import com.cloud.api.command.user.template.ExtractTemplateCmd;
import com.cloud.api.command.user.template.GetUploadParamsForTemplateCmd;
import com.cloud.api.command.user.template.RegisterTemplateCmd;
import com.cloud.api.command.user.template.UpdateTemplateCmd;
import com.cloud.api.response.GetUploadParamsResponse;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.legacymodel.user.Account;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

public interface TemplateApiService {

    VirtualMachineTemplate registerTemplate(RegisterTemplateCmd cmd) throws URISyntaxException, ResourceAllocationException;

    public GetUploadParamsResponse registerTemplateForPostUpload(GetUploadParamsForTemplateCmd cmd) throws ResourceAllocationException, MalformedURLException;

    VirtualMachineTemplate registerIso(RegisterIsoCmd cmd) throws IllegalArgumentException, ResourceAllocationException;

    VirtualMachineTemplate copyTemplate(CopyTemplateCmd cmd) throws StorageUnavailableException, ResourceAllocationException;

    VirtualMachineTemplate prepareTemplate(long templateId, long zoneId, Long storageId);

    boolean detachIso(long vmId);

    boolean attachIso(long isoId, long vmId);

    /**
     * Deletes a template
     *
     * @param cmd - the command specifying templateId
     */
    boolean deleteTemplate(DeleteTemplateCmd cmd);

    /**
     * Deletes a template
     *
     * @param cmd - the command specifying isoId
     * @return true if deletion is successful, false otherwise
     */
    boolean deleteIso(DeleteIsoCmd cmd);

    /**
     * Extracts an ISO
     *
     * @param cmd - the command specifying the mode and id of the ISO
     * @return extractUrl extract url.
     */
    String extract(ExtractIsoCmd cmd) throws InternalErrorException;

    /**
     * Extracts a Template
     *
     * @param cmd - the command specifying the mode and id of the template
     * @return extractUrl  extract url
     */
    String extract(ExtractTemplateCmd cmd) throws InternalErrorException;

    List<String> listTemplatePermissions(BaseListTemplateOrIsoPermissionsCmd cmd);

    boolean updateTemplateOrIsoPermissions(BaseUpdateTemplateOrIsoPermissionsCmd cmd);

    VirtualMachineTemplate createPrivateTemplateRecord(CreateTemplateCmd cmd, Account templateOwner) throws ResourceAllocationException;

    VirtualMachineTemplate createPrivateTemplate(CreateTemplateCmd command) throws CloudRuntimeException;

    VirtualMachineTemplate updateTemplate(UpdateIsoCmd cmd);

    VirtualMachineTemplate updateTemplate(UpdateTemplateCmd cmd);
}
