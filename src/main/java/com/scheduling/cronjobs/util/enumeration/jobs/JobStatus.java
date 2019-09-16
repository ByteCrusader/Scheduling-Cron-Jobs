package com.scheduling.cronjobs.util.enumeration.jobs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobStatus {

    RUNNING("running"),
    STOPPED("stopped"),
    FINISHED("finished");

    private final String status;
}
