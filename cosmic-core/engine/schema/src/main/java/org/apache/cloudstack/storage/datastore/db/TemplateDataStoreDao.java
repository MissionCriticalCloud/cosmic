package org.apache.cloudstack.storage.datastore.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

import java.util.List;

public interface TemplateDataStoreDao extends GenericDao<TemplateDataStoreVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {

    List<TemplateDataStoreVO> listByStoreId(long id);

    List<TemplateDataStoreVO> listDestroyed(long storeId);

    List<TemplateDataStoreVO> listActiveOnCache(long id);

    void deletePrimaryRecordsForStore(long id);

    void deletePrimaryRecordsForTemplate(long templateId);

    List<TemplateDataStoreVO> listByTemplateStore(long templateId, long storeId);

    List<TemplateDataStoreVO> listByTemplateStoreStatus(long templateId, long storeId, State... states);

    List<TemplateDataStoreVO> listByTemplateStoreDownloadStatus(long templateId, long storeId, VMTemplateStorageResourceAssoc.Status... status);

    List<TemplateDataStoreVO> listByTemplateZoneDownloadStatus(long templateId, Long zoneId, VMTemplateStorageResourceAssoc.Status... status);

    TemplateDataStoreVO findByTemplateZoneDownloadStatus(long templateId, Long zoneId, VMTemplateStorageResourceAssoc.Status... status);

    TemplateDataStoreVO findByTemplateZoneStagingDownloadStatus(long templateId, Long zoneId, Status... status);

    TemplateDataStoreVO findByStoreTemplate(long storeId, long templateId);

    TemplateDataStoreVO findByStoreTemplate(long storeId, long templateId, boolean lock);

    TemplateDataStoreVO findByTemplate(long templateId, DataStoreRole role);

    TemplateDataStoreVO findReadyByTemplate(long templateId, DataStoreRole role);

    TemplateDataStoreVO findByTemplateZone(long templateId, Long zoneId, DataStoreRole role);

    List<TemplateDataStoreVO> listByTemplate(long templateId);

    TemplateDataStoreVO findByTemplateZoneReady(long templateId, Long zoneId);

    void duplicateCacheRecordsOnRegionStore(long storeId);

    TemplateDataStoreVO findReadyOnCache(long templateId);

    List<TemplateDataStoreVO> listOnCache(long templateId);

    void updateStoreRoleToCachce(long storeId);

    List<TemplateDataStoreVO> listTemplateDownloadUrls();

    void removeByTemplateStore(long templateId, long imageStoreId);

    void expireDnldUrlsForZone(Long dcId);

    List<TemplateDataStoreVO> listByTemplateState(VirtualMachineTemplate.State... states);
}
