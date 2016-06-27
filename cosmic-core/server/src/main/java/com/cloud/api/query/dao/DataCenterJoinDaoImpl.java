package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.vo.DataCenterJoinVO;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.dc.DataCenter;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.user.AccountManager;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.ResourceTagResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataCenterJoinDaoImpl extends GenericDaoBase<DataCenterJoinVO, Long> implements DataCenterJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(DataCenterJoinDaoImpl.class);
    @Inject
    public AccountManager _accountMgr;
    private final SearchBuilder<DataCenterJoinVO> dofIdSearch;

    protected DataCenterJoinDaoImpl() {

        dofIdSearch = createSearchBuilder();
        dofIdSearch.and("id", dofIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        dofIdSearch.done();

        _count = "select count(distinct id) from data_center_view WHERE ";
    }

    @Override
    public ZoneResponse newDataCenterResponse(final ResponseView view, final DataCenterJoinVO dataCenter, final Boolean showCapacities) {
        final ZoneResponse zoneResponse = new ZoneResponse();
        zoneResponse.setId(dataCenter.getUuid());
        zoneResponse.setName(dataCenter.getName());
        zoneResponse.setSecurityGroupsEnabled(ApiDBUtils.isSecurityGroupEnabledInZone(dataCenter.getId()));
        zoneResponse.setLocalStorageEnabled(dataCenter.isLocalStorageEnabled());

        if ((dataCenter.getDescription() != null) && !dataCenter.getDescription().equalsIgnoreCase("null")) {
            zoneResponse.setDescription(dataCenter.getDescription());
        }

        if (view == ResponseView.Full) {
            zoneResponse.setDns1(dataCenter.getDns1());
            zoneResponse.setDns2(dataCenter.getDns2());
            zoneResponse.setIp6Dns1(dataCenter.getIp6Dns1());
            zoneResponse.setIp6Dns2(dataCenter.getIp6Dns2());
            zoneResponse.setInternalDns1(dataCenter.getInternalDns1());
            zoneResponse.setInternalDns2(dataCenter.getInternalDns2());
            // FIXME zoneResponse.setVlan(dataCenter.get.getVnet());
            zoneResponse.setGuestCidrAddress(dataCenter.getGuestNetworkCidr());
        }

        if (showCapacities != null && showCapacities) {
            zoneResponse.setCapacitites(ApiResponseHelper.getDataCenterCapacityResponse(dataCenter.getId()));
        }

        // set network domain info
        zoneResponse.setDomain(dataCenter.getDomain());

        // set domain info

        zoneResponse.setDomainId(dataCenter.getDomainUuid());
        zoneResponse.setDomainName(dataCenter.getDomainName());

        zoneResponse.setType(dataCenter.getNetworkType().toString());
        zoneResponse.setAllocationState(dataCenter.getAllocationState().toString());
        zoneResponse.setZoneToken(dataCenter.getZoneToken());
        zoneResponse.setDhcpProvider(dataCenter.getDhcpProvider());

        // update tag information
        final List<ResourceTagJoinVO> resourceTags = ApiDBUtils.listResourceTagViewByResourceUUID(dataCenter.getUuid(), ResourceObjectType.Zone);
        for (final ResourceTagJoinVO resourceTag : resourceTags) {
            final ResourceTagResponse tagResponse = ApiDBUtils.newResourceTagResponse(resourceTag, false);
            zoneResponse.addTag(tagResponse);
        }

        zoneResponse.setResourceDetails(ApiDBUtils.getResourceDetails(dataCenter.getId(), ResourceObjectType.Zone));

        zoneResponse.setObjectName("zone");
        return zoneResponse;
    }

    @Override
    public DataCenterJoinVO newDataCenterView(final DataCenter dataCenter) {
        final SearchCriteria<DataCenterJoinVO> sc = dofIdSearch.create();
        sc.setParameters("id", dataCenter.getId());
        final List<DataCenterJoinVO> dcs = searchIncludingRemoved(sc, null, null, false);
        assert dcs != null && dcs.size() == 1 : "No data center found for data center id " + dataCenter.getId();
        return dcs.get(0);
    }
}
