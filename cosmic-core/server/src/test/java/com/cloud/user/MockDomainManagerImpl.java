package com.cloud.user;

import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.api.command.admin.domain.ListDomainChildrenCmd;
import org.apache.cloudstack.api.command.admin.domain.ListDomainsCmd;
import org.apache.cloudstack.api.command.admin.domain.UpdateDomainCmd;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class MockDomainManagerImpl extends ManagerBase implements DomainManager, DomainService {

    @Override
    public Set<Long> getDomainChildrenIds(final String parentDomainPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain createDomain(final String name, final Long parentId, final Long ownerId, final String networkDomain, final String domainUUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Long> getDomainParentIds(final long domainId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeDomain(final long domainId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<? extends Domain> findInactiveDomains() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteDomain(final DomainVO domain, final Boolean cleanup) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteDomain(final long domainId, final Boolean cleanup) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Domain updateDomain(final UpdateDomainCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Domain createDomain(final String name, final Long parentId, final String networkDomain, final String domainUUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain getDomain(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain getDomain(final String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain getDomainByName(final String name, final long parentId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isChildDomain(final Long parentId, final Long childId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Pair<List<? extends Domain>, Integer> searchForDomains(final ListDomainsCmd cmd) throws PermissionDeniedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pair<List<? extends Domain>, Integer> searchForDomainChildren(final ListDomainChildrenCmd cmd) throws PermissionDeniedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DomainVO findDomainByPath(final String domainPath) {
        // TODO Auto-generated method stub
        return null;
    }
}
