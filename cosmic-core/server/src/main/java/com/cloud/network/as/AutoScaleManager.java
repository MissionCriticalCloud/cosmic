package com.cloud.network.as;

public interface AutoScaleManager extends AutoScaleService {

    void cleanUpAutoScaleResources(Long accountId);

    void doScaleUp(long groupId, Integer numVm);

    void doScaleDown(long groupId);
}
