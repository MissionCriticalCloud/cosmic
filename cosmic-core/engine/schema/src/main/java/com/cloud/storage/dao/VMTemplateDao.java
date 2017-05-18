package com.cloud.storage.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.List;
import java.util.Map;

/*
 * Data Access Object for vm_templates table
 */
public interface VMTemplateDao extends GenericDao<VMTemplateVO, Long>, StateDao<VirtualMachineTemplate.State, VirtualMachineTemplate.Event, VirtualMachineTemplate> {

    public List<VMTemplateVO> listByPublic();

    public VMTemplateVO findByName(String templateName);

    public VMTemplateVO findByTemplateName(String templateName);

    // public void update(VMTemplateVO template);

    public List<VMTemplateVO> listAllSystemVMTemplates();

    public List<VMTemplateVO> listDefaultBuiltinTemplates();

    public String getRoutingTemplateUniqueName();

    public List<VMTemplateVO> findIsosByIdAndPath(Long domainId, Long accountId, String path);

    public List<VMTemplateVO> listReadyTemplates();

    public List<VMTemplateVO> listByAccountId(long accountId);

    public long addTemplateToZone(VMTemplateVO tmplt, long zoneId);

    public List<VMTemplateVO> listAllInZone(long dataCenterId);

    public List<VMTemplateVO> listInZoneByState(long dataCenterId, VirtualMachineTemplate.State... states);

    public List<VMTemplateVO> listAllActive();

    public List<VMTemplateVO> listByState(VirtualMachineTemplate.State... states);

    public List<VMTemplateVO> listByHypervisorType(List<HypervisorType> hyperTypes);

    public List<VMTemplateVO> publicIsoSearch(Boolean bootable, boolean listRemoved, Map<String, String> tags);

    public List<VMTemplateVO> userIsoSearch(boolean listRemoved);

    VMTemplateVO findSystemVMTemplate(long zoneId);

    VMTemplateVO findSystemVMReadyTemplate(long zoneId, HypervisorType hypervisorType);

    VMTemplateVO findRoutingTemplate(HypervisorType type, String templateName);

    List<Long> listPrivateTemplatesByHost(Long hostId);

    public Long countTemplatesForAccount(long accountId);

    void loadDetails(VMTemplateVO tmpl);

    void saveDetails(VMTemplateVO tmpl);
}
