package com.redhat.cloud.notifications.sender.components.tower;

/**
 *
 */
public class JobStatus {

    String message;
    Status status;

    public JobStatus(boolean wasSuccess, String message) {
        this.message = message;
        this.status = wasSuccess ? Status.OK : Status.FAIL;
    }

    enum Status {
        OK,
        FAIL;
    }

}
