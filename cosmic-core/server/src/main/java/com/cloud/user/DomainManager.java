package com.cloud.user;

import com.cloud.api.command.admin.domain.UpdateDomainCmd;
import com.cloud.domain.DomainVO;
import com.cloud.legacymodel.domain.Domain;

import java.util.List;
import java.util.Set;

public interface DomainManager extends DomainService {
    public static final String MESSAGE_ADD_DOMAIN_EVENT = "Message.AddDomain.Event";
    public static final String MESSAGE_REMOVE_DOMAIN_EVENT = "Message.RemoveDomain.Event";

    Set<Long> getDomainChildrenIds(String parentDomainPath);

    Domain createDomain(String name, Long parentId, Long ownerId, String networkDomain, String domainUUID, String email, String slackChannelName);

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
