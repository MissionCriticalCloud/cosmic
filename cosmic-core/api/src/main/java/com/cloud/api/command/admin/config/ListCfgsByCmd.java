package com.cloud.api.command.admin.config;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ConfigurationResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.config.Configuration;
import com.cloud.legacymodel.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listConfigurations", group = APICommandGroup.ConfigurationService, description = "Lists all configurations.", responseObject = ConfigurationResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListCfgsByCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListCfgsByCmd.class.getName());

    private static final String s_name = "listconfigurationsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.CATEGORY, type = CommandType.STRING, description = "lists configurations by category")
    private String category;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "lists configuration by name")
    private String configName;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            description = "the ID of the Zone to update the parameter value for corresponding zone")
    private Long zoneId;

    @Parameter(name = ApiConstants.CLUSTER_ID,
            type = CommandType.UUID,
            entityType = ClusterResponse.class,
            description = "the ID of the Cluster to update the parameter value for corresponding cluster")
    private Long clusterId;

    @Parameter(name = ApiConstants.STORAGE_ID,
            type = CommandType.UUID,
            entityType = StoragePoolResponse.class,
            description = "the ID of the Storage pool to update the parameter value for corresponding storage pool")
    private Long storagePoolId;

    @Parameter(name = ApiConstants.ACCOUNT_ID,
            type = CommandType.UUID,
            entityType = AccountResponse.class,
            description = "the ID of the Account to update the parameter value for corresponding account")
    private Long accountId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public String getCategory() {
        return category;
    }

    public String getConfigName() {
        return configName;
    }

    @Override
    public Long getPageSizeVal() {
        Long defaultPageSize = 500L;
        final Integer pageSizeInt = getPageSize();
        if (pageSizeInt != null) {
            if (pageSizeInt.longValue() == s_pageSizeUnlimited) {
                defaultPageSize = null;
            } else {
                defaultPageSize = pageSizeInt.longValue();
            }
        }
        return defaultPageSize;
    }

    @Override
    public void execute() {
        final Pair<List<? extends Configuration>, Integer> result = _mgr.searchForConfigurations(this);
        final ListResponse<ConfigurationResponse> response = new ListResponse<>();
        final List<ConfigurationResponse> configResponses = new ArrayList<>();
        for (final Configuration cfg : result.first()) {
            final ConfigurationResponse cfgResponse = _responseGenerator.createConfigurationResponse(cfg);
            cfgResponse.setObjectName("configuration");
            if (getZoneId() != null) {
                cfgResponse.setScope("zone");
            }
            if (getClusterId() != null) {
                cfgResponse.setScope("cluster");
            }
            if (getStoragepoolId() != null) {
                cfgResponse.setScope("storagepool");
            }
            if (getAccountId() != null) {
                cfgResponse.setScope("account");
            }
            configResponses.add(cfgResponse);
        }

        response.setResponses(configResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public Long getStoragepoolId() {
        return storagePoolId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
