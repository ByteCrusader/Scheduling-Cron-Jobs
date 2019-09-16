package com.scheduling.cronjobs.controller;

import com.scheduling.cronjobs.service.jobs.CronJobInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API для взаимодействия с фоновыми заданиями
 */
@RestController
@RequestMapping(path = "/example-api/cron-job")
@RequiredArgsConstructor
public class CronJobInitializerController {

    private final CronJobInitializer service;

    /**
     * Добавление Job на выполнение по расписанию по cronJob коду
     */
    @GetMapping(value = "/start/{cronJobCode}/scheduled")
    public ResponseEntity startScheduledCronJob(@PathVariable String cronJobCode) {
        service.startScheduledCronJob(cronJobCode);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Одиночный запуск Job не по расписанию по cronJob коду
     */
    @GetMapping(value = "/start/{cronJobCode}/once")
    public ResponseEntity startOnceCronJob(@PathVariable String cronJobCode) {
        service.startOnceCronJob(cronJobCode);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Исключение Job из выполнения по cronJob коду
     */
    @GetMapping(value = "/stop/{cronJobCode}")
    public ResponseEntity removeCronJob(@PathVariable String cronJobCode) {
        service.removeRemainingCronJob(cronJobCode);
        return new ResponseEntity(HttpStatus.OK);
    }
}
