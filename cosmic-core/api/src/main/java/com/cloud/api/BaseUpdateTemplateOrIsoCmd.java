package com.cloud.api;

import com.cloud.api.command.user.iso.UpdateIsoCmd;
import com.cloud.api.response.GuestOSResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseUpdateTemplateOrIsoCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateIsoCmd.class.getName());

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ROUTING, type = CommandType.BOOLEAN, description = "true if the template type is routing i.e., if template is used to deploy router")
    protected Boolean isRoutingType;
    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, description = "Details in key/value pairs using format details[i].keyname=keyvalue. Example: details[0]" +
            ".hypervisortoolsversion=xenserver61")
    protected Map details;
    @Parameter(name = ApiConstants.BOOTABLE, type = CommandType.BOOLEAN, description = "true if image is bootable, false otherwise; available only for updateIso API")
    private Boolean bootable;
    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, description = "the display text of the image", length = 4096)
    private String displayText;
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class, required = true, description = "the ID of the image file")
    private Long id;
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the image file")
    private String templateName;
    @Parameter(name = ApiConstants.OS_TYPE_ID,
            type = CommandType.UUID,
            entityType = GuestOSResponse.class,
            description = "the ID of the OS type that best represents the OS of this image.")
    private Long osTypeId;
    @Parameter(name = ApiConstants.FORMAT, type = CommandType.STRING, description = "the format for the image")
    private String format;
    @Parameter(name = ApiConstants.PASSWORD_ENABLED, type = CommandType.BOOLEAN, description = "true if the image supports the password reset feature; default is false")
    private Boolean passwordEnabled;
    @Parameter(name = ApiConstants.SORT_KEY, type = CommandType.INTEGER, description = "sort key of the template, integer")
    private Integer sortKey;
    @Parameter(name = ApiConstants.IS_DYNAMICALLY_SCALABLE,
            type = CommandType.BOOLEAN,
            description = "true if template/ISO contains XS tools inorder to support dynamic scaling of VM cpu/memory")
    private Boolean isDynamicallyScalable;
    @Parameter(name = ApiConstants.URL,
            type = CommandType.STRING,
            description = "The URL where the templates originally was downloaded from")
    private String url;
    @Parameter(name = ApiConstants.MANUFACTURER_STRING, type = CommandType.STRING, description = "Manufacturer String to put in hardware info, defaults to 'Mission Critical Cloud'")
    private String manufacturerString;
    @Parameter(name = ApiConstants.OPTIMISE_FOR, type = CommandType.STRING, description = "Optimise for 'Windows' or 'Generic'")
    private String optimiseFor;
    @Parameter(name = ApiConstants.CPU_FLAGS, type = CommandType.STRING, description = "Optionally specify CPU flags to pass to VM")
    private String cpuFlags;
    @Parameter(name = ApiConstants.MAC_LEARNING, type = CommandType.BOOLEAN, description = "Set mag learning boolean, defaults to false")
    private Boolean macLearning;
    @Parameter(name = ApiConstants.MAINTENANCE_POLICY, type = CommandType.STRING, description = "Set maintenance policy to 'LiveMigrate' or 'ShutdownAndStart'")
    private String maintenancePolicy;
    @Parameter(name = ApiConstants.IS_REMOTE_GATEWAY_TEMPLATE, type = CommandType.BOOLEAN, description = "true if the template is allowed to acquire the network gateway IP and act as the default gateway for VMs; default is false")
    private Boolean isRemoteGatewayTemplate;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Boolean getBootable() {
        return bootable;
    }

    public String getDisplayText() {
        return displayText;
    }

    public Long getId() {
        return id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Long getOsTypeId() {
        return osTypeId;
    }

    public Boolean getPasswordEnabled() {
        return passwordEnabled;
    }

    public String getFormat() {
        return format;
    }

    public Integer getSortKey() {
        return sortKey;
    }

    public Boolean isDynamicallyScalable() {
        return isDynamicallyScalable;
    }

    public Boolean isRoutingType() {
        return isRoutingType;
    }

    public String getUrl() {
        return url;
    }

    public String getManufacturerString() {
        return manufacturerString;
    }

    public OptimiseFor getOptimiseFor() {
        if (optimiseFor != null) {
            return OptimiseFor.valueOf(optimiseFor);
        } else {
            return OptimiseFor.Generic;
        }
    }

    public MaintenancePolicy getMaintenancePolicy() {

        if (maintenancePolicy != null) {
            return MaintenancePolicy.valueOf(maintenancePolicy);
        } else {
            return MaintenancePolicy.LiveMigrate;
        }
    }

    public String getCpuFlags() {
        return cpuFlags;
    }

    public Boolean getMacLearning() {
        return macLearning;
    }

    public Boolean getIsRemoteGatewayTemplate() {
        if (isRemoteGatewayTemplate != null) {
            return isRemoteGatewayTemplate;
        }
        return false;
    }

    public Map getDetails() {
        if (details == null || details.isEmpty()) {
            return null;
        }

        final Collection paramsCollection = details.values();
        return (Map) paramsCollection.toArray()[0];
    }
}
