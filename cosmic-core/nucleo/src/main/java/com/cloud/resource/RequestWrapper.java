//

//

package com.cloud.resource;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestWrapper {

    private static final Logger s_logger = LoggerFactory.getLogger(RequestWrapper.class);

    protected Hashtable<Class<? extends ServerResource>, Hashtable<Class<? extends Command>, CommandWrapper>> resources = new Hashtable<>();

    /**
     * @param command to be executed.
     * @return an Answer for the executed command.
     */
    public abstract Answer execute(Command command, ServerResource serverResource);

    protected CommandWrapper<Command, Answer, ServerResource> retryWhenAllFails(final Command command, final Class<? extends ServerResource> resourceClass,
                                                                                final Hashtable<Class<? extends Command>, CommandWrapper> resourceCommands) {

        Class<? extends ServerResource> keepResourceClass = resourceClass;
        CommandWrapper<Command, Answer, ServerResource> commandWrapper = resourceCommands.get(command.getClass());
        while (commandWrapper == null) {
            // Could not find the command in the given resource, will traverse
            // the family tree.
            try {
                final Class<? extends ServerResource> resourceClass2 = (Class<? extends ServerResource>) keepResourceClass.getSuperclass();

                if (resourceClass2 == null) {
                    throw new NullPointerException("All the SERVER-RESOURCE hierarchy tree has been visited but no compliant key has been found for '" + command.getClass() + "'.");
                }

                final Hashtable<Class<? extends Command>, CommandWrapper> resourceCommands2 = retrieveResource(command,
                        (Class<? extends ServerResource>) keepResourceClass.getSuperclass());
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

    protected Hashtable<Class<? extends Command>, CommandWrapper> retrieveResource(final Command command, final Class<? extends ServerResource> resourceClass) {
        Class<? extends ServerResource> keepResourceClass = resourceClass;
        Hashtable<Class<? extends Command>, CommandWrapper> resource = resources.get(keepResourceClass);
        while (resource == null) {
            try {
                final Class<? extends ServerResource> keepResourceClass2 = (Class<? extends ServerResource>) keepResourceClass.getSuperclass();
                resource = resources.get(keepResourceClass2);

                keepResourceClass = keepResourceClass2;
            } catch (final ClassCastException e) {
                throw new NullPointerException("No key found for '" + command.getClass() + "' in the Map!");
            }
        }
        return resource;
    }

    protected CommandWrapper<Command, Answer, ServerResource> retrieveCommands(final Class<? extends Command> commandClass,
                                                                               final Hashtable<Class<? extends Command>, CommandWrapper> resourceCommands) {

        Class<? extends Command> keepCommandClass = commandClass;
        CommandWrapper<Command, Answer, ServerResource> commandWrapper = resourceCommands.get(keepCommandClass);
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

    protected Hashtable<Class<? extends Command>, CommandWrapper> processAnnotations(final Set<Class<? extends CommandWrapper>> wrappers) {
        final String errorMessage = "Error when adding Xen command to map ==> '{0}'. CommandWrapper class is ==> '{1}'";

        final Hashtable<Class<? extends Command>, CommandWrapper> commands = new Hashtable<>();

        for (final Class<? extends CommandWrapper> wrapper : wrappers) {
            final ResourceWrapper annotation = wrapper.getAnnotation(ResourceWrapper.class);
            if (annotation == null) {
                // Just in case people add classes without the annotation in the package and we don't see it.
                continue;
            }
            try {
                commands.put(annotation.handles(), wrapper.newInstance());
            } catch (final InstantiationException e) {
                s_logger.warn(MessageFormat.format(errorMessage, e.getLocalizedMessage(), wrapper.toString()));
            } catch (final IllegalAccessException e) {
                s_logger.warn(MessageFormat.format(errorMessage, e.getLocalizedMessage(), wrapper.toString()));
            }
        }

        return commands;
    }
}
