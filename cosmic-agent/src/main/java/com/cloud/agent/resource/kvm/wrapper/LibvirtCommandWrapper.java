package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.AgentResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public abstract class LibvirtCommandWrapper<T extends Command, A extends Answer, R extends AgentResource> {

    /**
     * @param T is the command to be used.
     * @param R is the resource base to be used.
     * @return A and the Answer from the command.
     */
    public abstract A execute(T command, R serverResource);
}
