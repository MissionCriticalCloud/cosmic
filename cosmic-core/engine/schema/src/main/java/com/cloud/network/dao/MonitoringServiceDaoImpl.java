package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MonitoringServiceDaoImpl extends GenericDaoBase<MonitoringServiceVO, Long> implements MonitoringServiceDao {
    private final SearchBuilder<MonitoringServiceVO> AllFieldsSearch;

    public MonitoringServiceDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("isDefault", AllFieldsSearch.entity().isDefaultService(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("service", AllFieldsSearch.entity().getService(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("processname", AllFieldsSearch.entity().getProcessName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("servicename", AllFieldsSearch.entity().getServiceName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("servicepath", AllFieldsSearch.entity().getServicePath(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("servicePidFile", AllFieldsSearch.entity().getServicePidFile(), SearchCriteria.Op.EQ);

        AllFieldsSearch.done();
    }

    @Override
    public List<MonitoringServiceVO> listAllServices() {
        return null;
    }

    @Override
    public List<MonitoringServiceVO> listDefaultServices(final boolean isDefault) {
        final SearchCriteria<MonitoringServiceVO> sc = AllFieldsSearch.create();
        sc.setParameters("isDefault", isDefault);
        return listBy(sc);
    }

    @Override
    public MonitoringServiceVO getServiceByName(final String service) {
        final SearchCriteria<MonitoringServiceVO> sc = AllFieldsSearch.create();
        sc.setParameters("service", service);
        return findOneBy(sc);
    }
}
