//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.net.URISyntaxException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.exception.InternalErrorException;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

@ResourceWrapper(handles = AttachIsoCommand.class)
public final class LibvirtAttachIsoCommandWrapper
    extends CommandWrapper<AttachIsoCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final AttachIsoCommand command, final LibvirtComputingResource libvirtComputingResource) {
    try {
      final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

      final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
      libvirtComputingResource.attachOrDetachIso(conn, command.getVmName(), command.getIsoPath(), command.isAttach());
    } catch (final LibvirtException e) {
      return new Answer(command, false, e.toString());
    } catch (final URISyntaxException e) {
      return new Answer(command, false, e.toString());
    } catch (final InternalErrorException e) {
      return new Answer(command, false, e.toString());
    }

    return new Answer(command);
  }
}