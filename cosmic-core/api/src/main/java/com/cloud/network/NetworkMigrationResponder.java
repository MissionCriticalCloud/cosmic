package com.cloud.network;

import com.cloud.deploy.DeployDestination;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;

/**
 * NetworkGuru and NetworkElements that implement this interface
 * will be called during Virtual Machine migration.
 */
public interface NetworkMigrationResponder {
    /**
     * Prepare for migration.
     * <p>
     * This method will be called per nic before the vm migration.
     *
     * @param nic
     * @param network
     * @param vm
     * @param dest
     * @param context
     * @return true when operation was successful.
     */
    public boolean prepareMigration(NicProfile nic, Network network, VirtualMachineProfile vm, DeployDestination dest, ReservationContext context);

    /**
     * Cancel for migration preparation.
     * <p>
     * This method will be called per nic when the entire vm migration
     * process failed and need to release the resouces that was
     * allocated at the migration preparation.
     *
     * @param nic     destination nic
     * @param network destination network
     * @param vm      destination vm profile
     * @param src     The context nic migrates from.
     * @param dst     The context nic migrates to.
     */
    public void rollbackMigration(NicProfile nic, Network network, VirtualMachineProfile vm, ReservationContext src, ReservationContext dst);

    /**
     * Commit the migration resource.
     * <p>
     * This method will be called per nic when the entire vm migration
     * process was successful. This is useful to release the resource of
     * source deployment where vm has left.
     *
     * @param nic     source nic
     * @param network source network
     * @param vm      source vm profile
     * @param src     the context nic migrates from.
     * @param dst     the context nic migrates to.
     */
    public void commitMigration(NicProfile nic, Network network, VirtualMachineProfile vm, ReservationContext src, ReservationContext dst);
}
