package com.scheduling.cronjobs.service.jobs.impl;


import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import com.scheduling.cronjobs.domain.jobs.CronJob;
import com.scheduling.cronjobs.service.jobs.CronJobExecution;
import com.scheduling.cronjobs.service.jobs.CronJobInitializer;
import com.scheduling.cronjobs.service.jobs.CronJobService;
import com.scheduling.cronjobs.util.enumeration.jobs.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.expression.ExpressionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Класс управления доступными в системе Job-ами
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class CronJobInitializerImpl implements CronJobInitializer {

    private final CronJobService                    cronJobService;
    private final ThreadPoolTaskScheduler           poolTaskScheduler;
    private final Map<String, CronJobExecution>     cronJobExecutions;
    private final Map<Runnable, ScheduledFuture<?>> listenableFutureMap = new ConcurrentHashMap<>();

    /**
     * Инициализация активных Job по расписанию
     */
    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCronJobs() {
        final Collection<CronJob> cronJobs = cronJobService.getAllCronJobs();
        cronJobs.stream()
                .peek(cronJob -> {
                    if (JobStatus.RUNNING == cronJob.getJobStatus()) {
                        cronJob.setJobStatus(JobStatus.STOPPED);
                    }
                })
                .filter(CronJob::getActive)
                .forEach(cronJob -> {
                    val cronJobExecution = cronJobExecutions.get(cronJob.getCode());

                    if (Objects.isNull(cronJobExecution)) {
                        log.warn(String.format(
                                "Cron Job with code '%s' not exists or disable for current profile",
                                cronJob.getCode()
                        ));
                        return;
                    }

                    val future = poolTaskScheduler.schedule(
                            cronJobExecution,
                            new CronTrigger(cronJob.getJobSchedule())
                    );
                    this.listenableFutureMap.put(cronJobExecution, future);
                });
        log.info("CronJobs: enabled");
    }

    /**
     * Исключение Job из выполнения по cronJob коду
     *
     * @param cronJobCode cronJob код
     */
    @Override
    public void removeRemainingCronJob(final String cronJobCode) {
        val cronJob = cronJobService.getCronJobByCode(cronJobCode);

        if (JobStatus.RUNNING == cronJob.getJobStatus()) {
            cronJob.setJobStatus(JobStatus.STOPPED);
        }

        val cronJobExecution = Optional.ofNullable(cronJobExecutions.get(cronJob.getCode()))
                .orElseThrow(() -> new ExpressionException(
                        String.format(
                                "Cron Job with code '%s' not exists or disable for current profile",
                                cronJob.getCode()
                        )
                ));

        cronJob.setActive(false);
        val future = this.listenableFutureMap.get(cronJobExecution);

        if (Objects.nonNull(future)) {
            future.cancel(true);
        }

        this.listenableFutureMap.remove(cronJobExecution);
    }

    /**
     * Добавление Job на выполнение по расписанию по cronJob коду
     *
     * @param cronJobCode cronJob код
     */
    @Override
    public void startScheduledCronJob(final String cronJobCode) {
        val cronJob = cronJobService.getCronJobByCode(cronJobCode);

        if (JobStatus.RUNNING == cronJob.getJobStatus()) {
            cronJob.setJobStatus(JobStatus.STOPPED);
        }

        val cronJobExecution = Optional.ofNullable(cronJobExecutions.get(cronJob.getCode()))
                .orElseThrow(() -> new ExpressionException(
                        String.format(
                                "Cron Job with code '%s' not exists or disable for current profile",
                                cronJob.getCode()
                        )
                ));

        cronJob.setActive(true);
        val future = poolTaskScheduler.schedule(cronJobExecution, new CronTrigger(cronJob.getJobSchedule()));

        this.listenableFutureMap.put(cronJobExecution, future);
    }

    /**
     * Одиночный запуск Job не по расписанию по cronJob коду
     *
     * @param cronJobCode cronJob код
     */
    @Override
    public void startOnceCronJob(final String cronJobCode) {
        val cronJob = cronJobService.getCronJobByCode(cronJobCode);

        if (JobStatus.RUNNING != cronJob.getJobStatus()) {
            val cronJobExecution = Optional.ofNullable(cronJobExecutions.get(cronJob.getCode()))
                    .orElseThrow(() -> new ExpressionException(
                            String.format(
                                    "Cron Job with code '%s' not exists or disable for current profile",
                                    cronJob.getCode()
                            )
                    ));

            poolTaskScheduler.schedule(cronJobExecution, DateUtils.addMinutes(new Date(), 1));
        }
    }
}
