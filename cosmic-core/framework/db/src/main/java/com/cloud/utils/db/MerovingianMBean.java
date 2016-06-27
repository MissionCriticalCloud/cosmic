package com.cloud.utils.db;

import java.util.List;
import java.util.Map;

public interface MerovingianMBean {

    List<Map<String, String>> getAllLocks();

    List<Map<String, String>> getLocksAcquiredByThisServer();

    boolean releaseLockAsLastResortAndIReallyKnowWhatIAmDoing(String key);

    void cleanupForServer(long msId);
}
