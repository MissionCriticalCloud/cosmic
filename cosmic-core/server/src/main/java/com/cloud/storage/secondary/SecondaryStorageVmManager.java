package com.cloud.storage.secondary;

import com.cloud.host.HostVO;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.utils.component.Manager;
import com.cloud.vm.SecondaryStorageVmVO;

import java.util.List;

public interface SecondaryStorageVmManager extends Manager {

    int DEFAULT_SS_VM_RAMSIZE = 512;            // 512M
    int DEFAULT_SS_VM_MTUSIZE = 1500;
    int DEFAULT_SS_VM_CAPACITY = 50;            // max command execution session per SSVM
    int DEFAULT_STANDBY_CAPACITY = 10;          // standy capacity to reserve per zone

    String ALERT_SUBJECT = "secondarystoragevm-alert";

    SecondaryStorageVmVO startSecStorageVm(long ssVmVmId);

    boolean stopSecStorageVm(long ssVmVmId);

    boolean rebootSecStorageVm(long ssVmVmId);

    boolean destroySecStorageVm(long ssVmVmId);

    void onAgentConnect(Long dcId, StartupCommand cmd);

    boolean generateFirewallConfiguration(Long agentId);

    boolean generateVMSetupCommand(Long hostId);

    Pair<HostVO, SecondaryStorageVmVO> assignSecStorageVm(long zoneId, Command cmd);

    boolean generateSetupCommand(Long hostId);

    List<HostVO> listUpAndConnectingSecondaryStorageVmHost(Long dcId);

    HostVO pickSsvmHost(HostVO ssHost);
}
