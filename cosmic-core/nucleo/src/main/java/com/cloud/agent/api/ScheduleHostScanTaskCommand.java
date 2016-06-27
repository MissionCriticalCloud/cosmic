//

//

package com.cloud.agent.api;

/*
 * This is used to schedule an explicit host scan in MS peers. Currently used during
 * add host operation so that correct MS can take host ownership. On receiving the
 * command the scan is scheduled immediately.
 */
public class ScheduleHostScanTaskCommand extends Command {
    public ScheduleHostScanTaskCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return false; // standalone command and can be executed independent of other commands
    }
}
