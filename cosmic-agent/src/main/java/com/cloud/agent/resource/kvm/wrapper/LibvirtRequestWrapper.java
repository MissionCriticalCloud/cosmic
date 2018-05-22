package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.AgentResource;
import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibvirtRequestWrapper {
    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtRequestWrapper.class);

    private static final LibvirtRequestWrapper instance;

    static {
        instance = new LibvirtRequestWrapper();
    }

    Reflections baseWrappers = new Reflections("com.cloud.agent.resource.kvm.wrapper");
    Set<Class<? extends LibvirtCommandWrapper>> baseSet = this.baseWrappers.getSubTypesOf(LibvirtCommandWrapper.class);

    private LibvirtRequestWrapper() {
        init();
    }

    private void init() {
        // LibvirtComputingResource commands
        final Hashtable<Class<? extends Command>, LibvirtCommandWrapper> libvirtCommands = processAnnotations(this.baseSet);

        this.resources.put(LibvirtComputingResource.class, libvirtCommands);
    }

    public static LibvirtRequestWrapper getInstance() {
        return instance;
    }

    public Answer execute(final Command command, final AgentResource serverResource) {
        final Class<? extends AgentResource> resourceClass = serverResource.getClass();

        final Hashtable<Class<? extends Command>, LibvirtCommandWrapper> resourceCommands = retrieveResource(command, resourceClass);

        LibvirtCommandWrapper<Command, Answer, AgentResource> commandWrapper = retrieveCommands(command.getClass(), resourceCommands);

        while (commandWrapper == null) {
            // Could not find the command in the given resource, will traverse the family tree.
            commandWrapper = retryWhenAllFails(command, resourceClass, resourceCommands);
        }

        return commandWrapper.execute(command, serverResource);
    }

    protected Hashtable<Class<? extends AgentResource>, Hashtable<Class<? extends Command>, LibvirtCommandWrapper>> resources = new Hashtable<>();

    protected LibvirtCommandWrapper<Command, Answer, AgentResource> retryWhenAllFails(final Command command, final Class<? extends AgentResource> resourceClass,
                                                                                      final Hashtable<Class<? extends Command>, LibvirtCommandWrapper> resourceCommands) {

        Class<? extends AgentResource> keepResourceClass = resourceClass;
        LibvirtCommandWrapper<Command, Answer, AgentResource> commandWrapper = resourceCommands.get(command.getClass());
        while (commandWrapper == null) {
            // Could not find the command in the given resource, will traverse
            // the family tree.
            try {
                final Class<? extends AgentResource> resourceClass2 = (Class<? extends AgentResource>) keepResourceClass.getSuperclass();

                if (resourceClass2 == null) {
                    throw new NullPointerException("All the SERVER-RESOURCE hierarchy tree has been visited but no compliant key has been found for '" + command.getClass() + "'.");
                }

                final Hashtable<Class<? extends Command>, LibvirtCommandWrapper> resourceCommands2 = retrieveResource(command,
                        (Class<? extends AgentResource>) keepResourceClass.getSuperclass());
                keepResourceClass = resourceClass2;

                commandWrapper = retrieveCommands(command.getClass(), resourceCommands2);
            } catch (final ClassCastException e) {
                throw new NullPointerException("No key found for '" + command.getClass() + "' in the Map!");
            } catch (final NullPointerException e) {
                throw e;
            }
        }
        return commandWrapper;
    }

    protected Hashtable<Class<? extends Command>, LibvirtCommandWrapper> retrieveResource(final Command command, final Class<? extends AgentResource> resourceClass) {
        Class<? extends AgentResource> keepResourceClass = resourceClass;
        Hashtable<Class<? extends Command>, LibvirtCommandWrapper> resource = this.resources.get(keepResourceClass);
        while (resource == null) {
            try {
                final Class<? extends AgentResource> keepResourceClass2 = (Class<? extends AgentResource>) keepResourceClass.getSuperclass();
                resource = this.resources.get(keepResourceClass2);

                keepResourceClass = keepResourceClass2;
            } catch (final ClassCastException e) {
                throw new NullPointerException("No key found for '" + command.getClass() + "' in the Map!");
            }
        }
        return resource;
    }

    protected LibvirtCommandWrapper<Command, Answer, AgentResource> retrieveCommands(final Class<? extends Command> commandClass,
                                                                                     final Hashtable<Class<? extends Command>, LibvirtCommandWrapper> resourceCommands) {

        Class<? extends Command> keepCommandClass = commandClass;
        LibvirtCommandWrapper<Command, Answer, AgentResource> commandWrapper = resourceCommands.get(keepCommandClass);
        while (commandWrapper == null) {
            try {
                final Class<? extends Command> commandClass2 = (Class<? extends Command>) keepCommandClass.getSuperclass();

                if (commandClass2 == null) {
                    throw new NullPointerException("All the COMMAND hierarchy tree has been visited but no compliant key has been found for '" + commandClass + "'.");
                }

                commandWrapper = resourceCommands.get(commandClass2);

                keepCommandClass = commandClass2;
            } catch (final ClassCastException e) {
                throw new NullPointerException("No key found for '" + keepCommandClass.getClass() + "' in the Map!");
            } catch (final NullPointerException e) {
                // Will now traverse all the resource hierarchy. Returning null
                // is not a problem.
                // It is all being nicely checked and in case we do not have a
                // resource, an Unsupported answer will be thrown by the base
                // class.
                return null;
            }
        }
        return commandWrapper;
    }

    protected Hashtable<Class<? extends Command>, LibvirtCommandWrapper> processAnnotations(final Set<Class<? extends LibvirtCommandWrapper>> wrappers) {
        final String errorMessage = "Error when adding Xen command to map ==> '{0}'. LibvirtCommandWrapper class is ==> '{1}'";

        final Hashtable<Class<? extends Command>, LibvirtCommandWrapper> commands = new Hashtable<>();

        for (final Class<? extends LibvirtCommandWrapper> wrapper : wrappers) {
            final ResourceWrapper annotation = wrapper.getAnnotation(ResourceWrapper.class);
            if (annotation == null) {
                // Just in case people add classes without the annotation in the package and we don't see it.
                continue;
            }
            try {
                commands.put(annotation.handles(), wrapper.newInstance());
            } catch (final InstantiationException | IllegalAccessException e) {
                s_logger.warn(MessageFormat.format(errorMessage, e.getLocalizedMessage(), wrapper.toString()));
            }
        }

        return commands;
    }
}
