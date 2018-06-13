package com.cloud.service.dao;

import com.cloud.model.enumeration.StorageProvisioningType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

/*
 * Data Access Object for service_offering table
 */
public interface ServiceOfferingDao extends GenericDao<ServiceOfferingVO, Long> {
    ServiceOfferingVO findByName(String name);

    List<ServiceOfferingVO> createSystemServiceOfferings(String name, String uniqueName, int cpuCount, int ramSize,
                                                         Integer rateMbps, Integer multicastRateMbps, boolean offerHA, String displayText, StorageProvisioningType provisioningType,
                                                         boolean recreatable, String tags, boolean systemUse, VirtualMachineType vmType, boolean defaultUse);

    ServiceOfferingVO persistSystemServiceOffering(ServiceOfferingVO vo);

    List<ServiceOfferingVO> findPublicServiceOfferings();

    List<ServiceOfferingVO> findServiceOfferingByDomainId(Long domainId);

    List<ServiceOfferingVO> findSystemOffering(Long domainId, Boolean isSystem, String vmType);

    ServiceOfferingVO persistDeafultServiceOffering(ServiceOfferingVO offering);

    void loadDetails(ServiceOfferingVO serviceOffering);

    void saveDetails(ServiceOfferingVO serviceOffering);

    ServiceOfferingVO findById(Long vmId, long serviceOfferingId);

    ServiceOfferingVO findByIdIncludingRemoved(Long vmId, long serviceOfferingId);

    boolean isDynamic(long serviceOfferingId);

    ServiceOfferingVO getcomputeOffering(ServiceOfferingVO serviceOffering, Map<String, String> customParameters);

    ServiceOfferingVO findDefaultSystemOffering(String offeringName, Boolean useLocalStorage);
}
