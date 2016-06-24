// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.api.command.user.region;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.RegionResponse;
import org.apache.cloudstack.region.Region;
import org.apache.cloudstack.region.RegionService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listRegions", description = "Lists Regions", responseObject = RegionResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListRegionsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListRegionsCmd.class.getName());

    private static final String s_name = "listregionsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Inject
    RegionService _regionService;
    @Parameter(name = ApiConstants.ID, type = CommandType.INTEGER, description = "List Region by region ID.")
    private Integer id;
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "List Region by region name.")
    private String name;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        List<? extends Region> result = _regionService.listRegions(this);
        ListResponse<RegionResponse> response = new ListResponse<RegionResponse>();
        List<RegionResponse> regionResponses = new ArrayList<RegionResponse>();
        for (Region region : result) {
            RegionResponse regionResponse = _responseGenerator.createRegionResponse(region);
            regionResponse.setObjectName("region");
            regionResponses.add(regionResponse);
        }

        response.setResponses(regionResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
