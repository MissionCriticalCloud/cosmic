package com.cloud.vm;

import com.cloud.api.BaseCmd.HTTPMethod;
import com.cloud.api.command.admin.vm.AssignVMCmd;
import com.cloud.api.command.admin.vm.RecoverVMCmd;
import com.cloud.api.command.user.vm.AddNicToVMCmd;
import com.cloud.api.command.user.vm.DeployVMCmd;
import com.cloud.api.command.user.vm.DestroyVMCmd;
import com.cloud.api.command.user.vm.RebootVMCmd;
import com.cloud.api.command.user.vm.RemoveNicFromVMCmd;
import com.cloud.api.command.user.vm.ResetVMPasswordCmd;
import com.cloud.api.command.user.vm.ResetVMSSHKeyCmd;
import com.cloud.api.command.user.vm.RestoreVMCmd;
import com.cloud.api.command.user.vm.ScaleVMCmd;
import com.cloud.api.command.user.vm.StartVMCmd;
import com.cloud.api.command.user.vm.UpdateDefaultNicForVMCmd;
import com.cloud.api.command.user.vm.UpdateVMCmd;
import com.cloud.api.command.user.vm.UpdateVmNicIpCmd;
import com.cloud.api.command.user.vm.UpgradeVMCmd;
import com.cloud.api.command.user.vmgroup.CreateVMGroupCmd;
import com.cloud.api.command.user.vmgroup.DeleteVMGroupCmd;
import com.cloud.db.model.Zone;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.network.Network.IpAddresses;
import com.cloud.offering.ServiceOffering;
import com.cloud.storage.StoragePool;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.ExecutionException;

import java.util.List;
import java.util.Map;

public interface UserVmService {

    /**
     * Destroys one virtual machine
     *
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    UserVm destroyVm(DestroyVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Destroys one virtual machine
     *
     * @param vmId the id of the virtual machine.
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    UserVm destroyVm(long vmId) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Resets the password of a virtual machine.
     *
     * @param cmd - the command specifying vmId, password
     * @return the VM if reset worked successfully, null otherwise
     */
    UserVm resetVMPassword(ResetVMPasswordCmd cmd, String password) throws ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Resets the SSH Key of a virtual machine.
     *
     * @param cmd - the command specifying vmId, Keypair name
     * @return the VM if reset worked successfully, null otherwise
     */
    UserVm resetVMSSHKey(ResetVMSSHKeyCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException;

    UserVm startVirtualMachine(StartVMCmd cmd) throws ExecutionException, ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException, ResourceAllocationException;

    UserVm rebootVirtualMachine(RebootVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException;

    UserVm updateVirtualMachine(UpdateVMCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Adds a NIC on the given network to the virtual machine
     *
     * @param cmd the command object that defines the vm and the given network
     * @return the vm object if successful, null otherwise
     */
    UserVm addNicToVirtualMachine(AddNicToVMCmd cmd);

    /**
     * Removes a NIC on the given network from the virtual machine
     *
     * @param cmd the command object that defines the vm and the given network
     * @return the vm object if successful, null otherwise
     */
    UserVm removeNicFromVirtualMachine(RemoveNicFromVMCmd cmd);

    /**
     * Updates default Nic to the given network for given virtual machine
     *
     * @param cmd the command object that defines the vm and the given network
     * @return the vm object if successful, null otherwise
     */
    UserVm updateDefaultNicForVirtualMachine(UpdateDefaultNicForVMCmd cmd);

    /**
     * Updated the ip address on the given NIC to the virtual machine
     *
     * @param cmd the command object that defines the ip address and the given nic
     * @return the vm object if successful, null otherwise
     */
    UserVm updateNicIpForVirtualMachine(UpdateVmNicIpCmd cmd);

    UserVm recoverVirtualMachine(RecoverVMCmd cmd) throws ResourceAllocationException;

    /**
     * Creates a User VM in Advanced Zone in the database and returns the VM to the caller.
     *
     * @param zone                - availability zone for the virtual machine
     * @param serviceOffering     - the service offering for the virtual machine
     * @param template            - the template for the virtual machine
     * @param networkIdList       - list of network ids used by virtual machine
     * @param hostName            - host name for the virtual machine
     * @param displayName         - an optional user generated name for the virtual machine
     * @param diskOfferingId      - the ID of the disk offering for the virtual machine. If the
     *                            template is of ISO format, the diskOfferingId is for the root
     *                            disk volume. Otherwise this parameter is used to indicate the
     *                            offering for the data disk volume. If the templateId parameter
     *                            passed is from a Template object, the diskOfferingId refers to
     *                            a DATA Disk Volume created. If the templateId parameter passed
     *                            is from an ISO object, the diskOfferingId refers to a ROOT
     *                            Disk Volume created
     * @param diskSize            - the arbitrary size for the DATADISK volume. Mutually
     *                            exclusive with diskOfferingId
     * @param group               - an optional group for the virtual machine
     * @param hypervisor          - the hypervisor on which to deploy the virtual machine
     * @param userData            - an optional binary data that can be sent to the virtual
     *                            machine upon a successful deployment. This binary data must be
     *                            base64 encoded before adding it to the request. Currently only
     *                            HTTP GET is supported. Using HTTP GET (via querystring), you
     *                            can send up to 2KB of data after base64 encoding
     * @param sshKeyPair          - name of the ssh key pair used to login to the virtual
     *                            machine
     * @param requestedIps        TODO
     * @param defaultIps          TODO
     * @param displayVm           - Boolean flag whether to the display the vm to the end user or not
     * @param affinityGroupIdList
     * @param customId
     * @return UserVm object if successful.
     * @throws InsufficientCapacityException if there is insufficient capacity to deploy the VM.
     * @throws ConcurrentOperationException  if there are multiple users working on the same VM or in the
     *                                       same environment.
     * @throws ResourceUnavailableException  if the resources required to deploy the VM is not currently
     *                                       available.
     */
    UserVm createAdvancedVirtualMachine(Zone zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, Account owner,
                                        String hostName, String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor, HTTPMethod httpmethod,
                                        String userData, String sshKeyPair, Map<Long, IpAddresses> requestedIps, IpAddresses defaultIps, Boolean displayVm, String keyboard, List<Long>
                                                affinityGroupIdList, Map<String, String> customParameters, String customId, DiskControllerType diskControllerType)
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, ResourceAllocationException;

    /**
     * Starts the virtual machine created from createVirtualMachine.
     *
     * @param cmd Command to deploy.
     * @return UserVm object if successful.
     * @throws InsufficientCapacityException if there is insufficient capacity to deploy the VM.
     * @throws ConcurrentOperationException  if there are multiple users working on the same VM.
     * @throws ResourceUnavailableException  if the resources required the deploy the VM is not currently available.
     */
    UserVm startVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException;

    /**
     * Creates a vm group.
     */
    InstanceGroup createVmGroup(CreateVMGroupCmd cmd);

    boolean deleteVmGroup(DeleteVMGroupCmd cmd);

    /**
     * upgrade the service offering of the virtual machine
     *
     * @param cmd - the command specifying vmId and new serviceOfferingId
     * @return the vm
     * @throws ResourceAllocationException
     */
    UserVm upgradeVirtualMachine(UpgradeVMCmd cmd) throws ResourceAllocationException;

    UserVm stopVirtualMachine(long vmId, boolean forced) throws ConcurrentOperationException;

    UserVm getUserVm(long vmId);

    /**
     * Migrate the given VM to the destination host provided. The API returns the migrated VM if migration succeeds.
     * Only Root
     * Admin can migrate a VM.
     *
     * @return VirtualMachine migrated VM
     * @throws ManagementServerException        in case we get error finding the VM or host or access errors or other internal errors.
     * @throws ConcurrentOperationException     if there are multiple users working on the same VM.
     * @throws ResourceUnavailableException     if the destination host to migrate the VM is not currently available.
     * @throws VirtualMachineMigrationException if the VM to be migrated is not in Running state
     */
    VirtualMachine migrateVirtualMachine(Long vmId, Host destinationHost) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException;

    /**
     * Migrate the given VM with its volumes to the destination host. The API returns the migrated VM if it succeeds.
     * Only root admin can migrate a VM.
     *
     * @return VirtualMachine migrated VM
     * @throws ManagementServerException        in case we get error finding the VM or host or access errors or other internal errors.
     * @throws ConcurrentOperationException     if there are multiple users working on the same VM.
     * @throws ResourceUnavailableException     if the destination host to migrate the VM is not currently available.
     * @throws VirtualMachineMigrationException if the VM to be migrated is not in Running state
     */
    VirtualMachine migrateVirtualMachineWithVolume(Long vmId, Host destinationHost, Map<String, String> volumeToPool) throws ResourceUnavailableException, ConcurrentOperationException,
            ManagementServerException, VirtualMachineMigrationException;

    UserVm moveVMToUser(AssignVMCmd moveUserVMCmd) throws ResourceAllocationException, ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    VirtualMachine vmStorageMigration(Long vmId, StoragePool destPool);

    UserVm restoreVM(RestoreVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException;

    UserVm upgradeVirtualMachine(ScaleVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException;

    UserVm expungeVm(long vmId) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Finds and returns an encrypted password for a VM.
     *
     * @return Base64 encoded userdata
     */
    String getVmUserData(long vmId);
}
