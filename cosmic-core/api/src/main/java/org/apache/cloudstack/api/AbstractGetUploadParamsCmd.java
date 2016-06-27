package org.apache.cloudstack.api;

import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.GetUploadParamsResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.net.URL;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGetUploadParamsCmd extends BaseCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(AbstractGetUploadParamsCmd.class.getName());

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "the name of the volume/template")
    private String name;

    @Parameter(name = ApiConstants.FORMAT, type = CommandType.STRING, required = true, description = "the format for the volume/template. Possible values include QCOW2, OVA, "
            + "and VHD.")
    private String format;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "the ID of the zone the volume/template is "
            + "to be hosted on")
    private Long zoneId;

    @Parameter(name = ApiConstants.CHECKSUM, type = CommandType.STRING, description = "the MD5 checksum value of this volume/template")
    private String checksum;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "an optional accountName. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "an optional domainId. If the account parameter is used, "
            + "domainId must also be used.")
    private Long domainId;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "Upload volume/template for the project")
    private Long projectId;

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public GetUploadParamsResponse createGetUploadParamsResponse(final UUID id, final URL postURL, final String metadata, final String timeout, final String signature) {
        return new GetUploadParamsResponse(id, postURL, metadata, timeout, signature);
    }
}
