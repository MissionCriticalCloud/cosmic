//

//

package com.cloud.network.utils;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.resource.ServerResource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRetryUtility {

    private static final Logger s_logger = LoggerFactory.getLogger(CommandRetryUtility.class);

    private static final int ZERO = 0;
    private static final CommandRetryUtility instance;

    static {
        instance = new CommandRetryUtility();
    }

    private final ConcurrentHashMap<com.cloud.agent.api.Command, Integer> commandsToRetry;
    private ServerResource serverResource;

    private CommandRetryUtility() {
        commandsToRetry = new ConcurrentHashMap<>();
    }

    public static CommandRetryUtility getInstance() {
        return instance;
    }

    public void setServerResource(final ServerResource serverResource) {
        this.serverResource = serverResource;
    }

    public boolean addRetry(final Command command, final int retries) {
        if (commandsToRetry.containsKey(command)) {
            // A retry already exists for this command, do not add it again.
            // Once all retries are executed, the command will be removed from the map.
            return false;
        }
        commandsToRetry.put(command, retries);
        return true;
    }

    public Answer retry(final Command command, final Class<? extends Answer> answerClass, final Exception error) {
        if (commandsToRetry.containsKey(command)) {
            Integer numRetries = commandsToRetry.get(command);

            if (numRetries > ZERO) {
                commandsToRetry.put(command, --numRetries);

                s_logger.warn("Retrying " + command.getClass().getSimpleName() + ". Number of retries remaining: " + numRetries);

                return serverResource.executeRequest(command);
            } else {
                commandsToRetry.remove(command);
            }
        }
        try {
            final Constructor<? extends Answer> answerConstructor = answerClass.getConstructor(Command.class, Exception.class);
            return answerConstructor.newInstance(command, error);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return Answer.createUnsupportedCommandAnswer(command);
        }
    }
}
