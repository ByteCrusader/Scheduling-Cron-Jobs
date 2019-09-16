package com.scheduling.cronjobs.util.enumeration.jobs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobResult {

    SUCCESS("success"),
    FAIL("fail"),
    COMPLETE_WITH_ERRORS("complete with errors");

    private final String result;

}
