package com.cloud.storage.dao;

import com.cloud.storage.DiskOfferingVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DiskOfferingDao extends GenericDao<DiskOfferingVO, Long> {
    List<DiskOfferingVO> listByDomainId(long domainId);

    List<DiskOfferingVO> findPrivateDiskOffering();

    List<DiskOfferingVO> findPublicDiskOfferings();

    DiskOfferingVO findByUniqueName(String uniqueName);

    DiskOfferingVO persistDeafultDiskOffering(DiskOfferingVO offering);
}
