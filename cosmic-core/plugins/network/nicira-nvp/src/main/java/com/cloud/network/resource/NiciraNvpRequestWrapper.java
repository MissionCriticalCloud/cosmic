package com.cloud.network.resource;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.RequestWrapper;
import com.cloud.common.resource.ServerResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

import java.util.Hashtable;
import java.util.Set;

import org.reflections.Reflections;

public class NiciraNvpRequestWrapper extends RequestWrapper {

    private static final NiciraNvpRequestWrapper instance;

    static {
        instance = new NiciraNvpRequestWrapper();
    }

    Reflections baseWrappers = new Reflections("com.cloud.network.resource.wrapper");
    Set<Class<? extends CommandWrapper>> baseSet = this.baseWrappers.getSubTypesOf(CommandWrapper.class);

    private NiciraNvpRequestWrapper() {
        init();
    }

    private void init() {
        // NiciraNvpResource commands
        final Hashtable<Class<? extends Command>, CommandWrapper> niciraCommands = processAnnotations(this.baseSet);

        this.resources.put(NiciraNvpResource.class, niciraCommands);
    }

    public static NiciraNvpRequestWrapper getInstance() {
        return instance;
    }

    @Override
    public Answer execute(final Command command, final ServerResource serverResource) {
        final Class<? extends ServerResource> resourceClass = serverResource.getClass();

        final Hashtable<Class<? extends Command>, CommandWrapper> resourceCommands = retrieveResource(command, resourceClass);

        CommandWrapper<Command, Answer, ServerResource> commandWrapper = retrieveCommands(command.getClass(), resourceCommands);

        while (commandWrapper == null) {
            //Could not find the command in the given resource, will traverse the family tree.
            commandWrapper = retryWhenAllFails(command, resourceClass, resourceCommands);
        }

        return commandWrapper.execute(command, serverResource);
    }
}
