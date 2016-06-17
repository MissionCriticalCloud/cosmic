package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.Hashtable;
import java.util.Set;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.RequestWrapper;
import com.cloud.resource.ServerResource;

import org.reflections.Reflections;

public class LibvirtRequestWrapper extends RequestWrapper {

  private static LibvirtRequestWrapper instance;

  static {
    instance = new LibvirtRequestWrapper();
  }

  Reflections baseWrappers = new Reflections("com.cloud.hypervisor.kvm.resource.wrapper");
  @SuppressWarnings("rawtypes")
  Set<Class<? extends CommandWrapper>> baseSet = baseWrappers.getSubTypesOf(CommandWrapper.class);

  private LibvirtRequestWrapper() {
    init();
  }

  @SuppressWarnings("rawtypes")
  private void init() {
    // LibvirtComputingResource commands
    final Hashtable<Class<? extends Command>, CommandWrapper> libvirtCommands = processAnnotations(baseSet);

    resources.put(LibvirtComputingResource.class, libvirtCommands);
  }

  public static LibvirtRequestWrapper getInstance() {
    return instance;
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public Answer execute(final Command command, final ServerResource serverResource) {
    final Class<? extends ServerResource> resourceClass = serverResource.getClass();

    final Hashtable<Class<? extends Command>, CommandWrapper> resourceCommands = retrieveResource(command,
        resourceClass);

    CommandWrapper<Command, Answer, ServerResource> commandWrapper = retrieveCommands(command.getClass(),
        resourceCommands);

    while (commandWrapper == null) {
      // Could not find the command in the given resource, will traverse the family tree.
      commandWrapper = retryWhenAllFails(command, resourceClass, resourceCommands);
    }

    return commandWrapper.execute(command, serverResource);
  }
}