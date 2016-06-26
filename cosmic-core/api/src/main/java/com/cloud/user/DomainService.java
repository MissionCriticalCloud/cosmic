package com.cloud.user;

import com.cloud.domain.Domain;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.command.admin.domain.ListDomainChildrenCmd;
import org.apache.cloudstack.api.command.admin.domain.ListDomainsCmd;

import java.util.List;

public interface DomainService {

    Domain createDomain(String name, Long parentId, String networkDomain, String domainUUID);

    Domain getDomain(long id);

    Domain getDomain(String uuid);

    Domain getDomainByName(String name, long parentId);

    /**
     * Return whether a domain is a child domain of a given domain.
     *
     * @param parentId
     * @param childId
     */
    boolean isChildDomain(Long parentId, Long childId);

    Pair<List<? extends Domain>, Integer> searchForDomains(ListDomainsCmd cmd) throws PermissionDeniedException;

    Pair<List<? extends Domain>, Integer> searchForDomainChildren(ListDomainChildrenCmd cmd) throws PermissionDeniedException;

    /**
     * find the domain by its path
     *
     * @param domainPath the path to use to lookup a domain
     * @return domainVO the domain with the matching path, or null if no domain with the given path exists
     */
    Domain findDomainByPath(String domainPath);
}
