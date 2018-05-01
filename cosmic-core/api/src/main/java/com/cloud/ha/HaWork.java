package com.cloud.ha;

import com.cloud.vm.VirtualMachine;

import java.util.Date;

public interface HaWork {

    long getId();

    long getInstanceId();

    HaWorkType getWorkType();

    Long getServerId();

    VirtualMachine.Type getType();

    Date getCreated();

    HaWorkStep getStep();

    VirtualMachine.State getPreviousState();

    Date getDateTaken();

    long getHostId();

    int getTimesTried();

    long getUpdateTime();

    long getTimeToTry();

    enum HaWorkType {
        Migration,  // Migrating VMs off of a host.
        Stop,       // Stops a VM for storage pool migration purposes.  This should be obsolete now.
        CheckStop,  // Checks if a VM has been stopped.
        ForceStop,  // Force a VM to stop even if the states don't allow it.  Use this only if you know the VM is stopped on the physical hypervisor.
        Destroy,    // Destroy a VM.
        HA          // Restart a VM.
    }

    enum HaWorkStep {
        Scheduled,
        Investigating,
        Fencing,
        Stopping,
        Restarting,
        Migrating,
        Cancelled,
        Done,
        Error
    }
}
