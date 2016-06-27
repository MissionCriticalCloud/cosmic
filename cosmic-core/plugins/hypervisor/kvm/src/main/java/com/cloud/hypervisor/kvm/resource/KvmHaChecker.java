package com.cloud.hypervisor.kvm.resource;

import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmHaChecker extends KvmHaBase implements Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(KvmHaChecker.class);
    private final List<NfsStoragePool> pools;
    private final String hostIp;
    private final long heartbeatTimeoutSeconds = 360;

    public KvmHaChecker(final List<NfsStoragePool> pools, final String hostIp) {
        this.pools = pools;
        this.hostIp = hostIp;
    }

    @Override
    public Boolean call() throws Exception {
        return checkingHb();
    }

    private Boolean checkingHb() {
        final List<Boolean> results = new ArrayList<>();
        for (final NfsStoragePool pool : pools) {

            final Script cmd = new Script(heartBeatPath, heartbeatTimeoutSeconds, logger);
            cmd.add("-i", pool.innetPoolIp);
            cmd.add("-p", pool.innerPoolMountSourcePath);
            cmd.add("-m", pool.innerMountDestPath);
            cmd.add("-h", hostIp);
            cmd.add("-r");
            cmd.add("-t", String.valueOf(heartBeatUpdateFreq));
            final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
            final String result = cmd.execute(parser);
            logger.debug("pool: " + pool.innetPoolIp);
            logger.debug("reture: " + result);
            logger.debug("parser: " + parser.getLine());
            if (result == null && parser.getLine().contains("> DEAD <")) {
                logger.debug("read heartbeat failed: " + result);
                results.add(false);
            } else {
                results.add(true);
            }
        }

        for (final Boolean r : results) {
            if (r) {
                return true;
            }
        }

        return false;
    }
}
