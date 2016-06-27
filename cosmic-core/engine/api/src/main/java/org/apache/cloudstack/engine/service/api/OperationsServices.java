package org.apache.cloudstack.engine.service.api;

import com.cloud.alert.Alert;

import java.net.URL;
import java.util.List;

public interface OperationsServices {
    //    List<AsyncJob> listJobs();
    //
    //    List<AsyncJob> listJobsInProgress();
    //
    //    List<AsyncJob> listJobsCompleted();
    //
    //    List<AsyncJob> listJobsCompleted(Long from);
    //
    //    List<AsyncJob> listJobsInWaiting();

    void cancelJob(String job);

    List<Alert> listAlerts();

    Alert getAlert(String uuid);

    void cancelAlert(String alert);

    void registerForAlerts();

    String registerForEventNotifications(String type, String topic, URL url);

    boolean deregisterForEventNotifications(String notificationId);

    /**
     * @return the list of event topics someone can register for
     */
    List<String> listEventTopics();
}
