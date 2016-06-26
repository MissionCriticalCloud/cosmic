//

//

package com.cloud.network.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.RequestWrapper;
import com.cloud.resource.ServerResource;

import java.util.Hashtable;
import java.util.Set;

import org.reflections.Reflections;

public class NiciraNvpRequestWrapper extends RequestWrapper {

    private static final NiciraNvpRequestWrapper instance;

    static {
        instance = new NiciraNvpRequestWrapper();
    }

    Reflections baseWrappers = new Reflections("com.cloud.network.resource.wrapper");
    Set<Class<? extends CommandWrapper>> baseSet = baseWrappers.getSubTypesOf(CommandWrapper.class);

    private NiciraNvpRequestWrapper() {
        init();
    }

    private void init() {
        // NiciraNvpResource commands
        final Hashtable<Class<? extends com.cloud.agent.api.Command>, CommandWrapper> niciraCommands = processAnnotations(baseSet);

        resources.put(NiciraNvpResource.class, niciraCommands);
    }

    public static NiciraNvpRequestWrapper getInstance() {
        return instance;
    }

    @Override
    public Answer execute(final com.cloud.agent.api.Command command, final ServerResource serverResource) {
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
