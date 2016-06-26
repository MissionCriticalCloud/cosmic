package com.cloud.acl;

import com.cloud.domain.DomainVO;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.projects.ProjectVO;
import com.cloud.projects.dao.ProjectAccountDao;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupService;
import org.apache.cloudstack.affinity.dao.AffinityGroupDomainMapDao;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class AffinityGroupAccessChecker extends DomainChecker {

    @Inject
    AffinityGroupService _affinityGroupService;
    @Inject
    AccountManager _accountMgr;
    @Inject
    AffinityGroupDomainMapDao _affinityGroupDomainMapDao;
    @Inject
    ProjectDao _projectDao;
    @Inject
    ProjectAccountDao _projectAccountDao;

    @Override
    public boolean checkAccess(final Account caller, final ControlledEntity entity, final AccessType accessType) throws PermissionDeniedException {
        if (entity instanceof AffinityGroup) {
            final AffinityGroup group = (AffinityGroup) entity;

            if (_affinityGroupService.isAdminControlledGroup(group)) {
                if (accessType == AccessType.OperateEntry && !_accountMgr.isRootAdmin(caller.getId())) {
                    throw new PermissionDeniedException(caller + " does not have permission to operate with resource "
                            + entity);
                }
            }

            if (group.getAclType() == ACLType.Domain) {
                if (!_affinityGroupService.isAffinityGroupAvailableInDomain(group.getId(), caller.getDomainId())) {
                    final DomainVO callerDomain = _domainDao.findById(caller.getDomainId());
                    if (callerDomain == null) {
                        throw new CloudRuntimeException("cannot check permission on account " + caller.getAccountName() + " whose domain does not exist");
                    }

                    throw new PermissionDeniedException("Affinity group is not available in domain id=" + callerDomain.getUuid());
                } else {
                    return true;
                }
            } else {
                //acl_type account
                if (caller.getId() != group.getAccountId()) {
                    //check if the group belongs to a project
                    final ProjectVO project = _projectDao.findByProjectAccountId(group.getAccountId());
                    if (project != null) {
                        if (AccessType.ModifyProject.equals(accessType) && _projectAccountDao.canModifyProjectAccount(caller.getId(), group.getAccountId())) {
                            return true;
                        } else if (!AccessType.ModifyProject.equals(accessType) && _projectAccountDao.canAccessProjectAccount(caller.getId(), group.getAccountId())) {
                            return true;
                        }
                    }
                    throw new PermissionDeniedException(caller + " does not have permission to operate with resource " + entity);
                } else {
                    return true;
                }
            }
        }

        return false;
    }
}
