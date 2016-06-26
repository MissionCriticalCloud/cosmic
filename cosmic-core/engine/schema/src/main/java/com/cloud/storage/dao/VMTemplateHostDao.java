package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

import java.util.List;

public interface VMTemplateHostDao extends GenericDao<VMTemplateHostVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {
    List<VMTemplateHostVO> listByHostId(long id);

    List<VMTemplateHostVO> listByTemplateId(long templateId);

    List<VMTemplateHostVO> listByOnlyTemplateId(long templateId);

    VMTemplateHostVO findByHostTemplate(long hostId, long templateId);

    VMTemplateHostVO findByTemplateId(long templateId);

    VMTemplateHostVO findByHostTemplate(long hostId, long templateId, boolean lock);

    List<VMTemplateHostVO> listByHostTemplate(long hostId, long templateId);

    void update(VMTemplateHostVO instance);

    List<VMTemplateHostVO> listByTemplateStatus(long templateId, VMTemplateHostVO.Status downloadState);

    List<VMTemplateHostVO> listByTemplateStatus(long templateId, long datacenterId, VMTemplateHostVO.Status downloadState);

    List<VMTemplateHostVO> listByTemplateStatus(long templateId, long datacenterId, long podId, VMTemplateHostVO.Status downloadState);

    List<VMTemplateHostVO> listByTemplateStates(long templateId, VMTemplateHostVO.Status... states);

    List<VMTemplateHostVO> listDestroyed(long hostId);

    boolean templateAvailable(long templateId, long hostId);

    List<VMTemplateHostVO> listByZoneTemplate(long dcId, long templateId, boolean readyOnly);

    void deleteByHost(Long hostId);

    VMTemplateHostVO findLocalSecondaryStorageByHostTemplate(long hostId, long templateId);

    List<VMTemplateHostVO> listByTemplateHostStatus(long templateId, long hostId, Status... states);

    List<VMTemplateHostVO> listByState(Status state);
}
