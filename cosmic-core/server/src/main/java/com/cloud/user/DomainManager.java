package com.cloud.user;

import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import org.apache.cloudstack.api.command.admin.domain.UpdateDomainCmd;

import java.util.List;
import java.util.Set;

public interface DomainManager extends DomainService {
    public static final String MESSAGE_ADD_DOMAIN_EVENT = "Message.AddDomain.Event";
    public static final String MESSAGE_REMOVE_DOMAIN_EVENT = "Message.RemoveDomain.Event";

    Set<Long> getDomainChildrenIds(String parentDomainPath);

    Domain createDomain(String name, Long parentId, Long ownerId, String networkDomain, String domainUUID);

    Set<Long> getDomainParentIds(long domainId);

    boolean removeDomain(long domainId);

    List<? extends Domain> findInactiveDomains();

    boolean deleteDomain(DomainVO domain, Boolean cleanup);

    boolean deleteDomain(long domainId, Boolean cleanup);

    /**
     * update an existing domain
     *
     * @param cmd - the command containing domainId and new domainName
     * @return Domain object if the command succeeded
     */
    Domain updateDomain(UpdateDomainCmd cmd);
}
