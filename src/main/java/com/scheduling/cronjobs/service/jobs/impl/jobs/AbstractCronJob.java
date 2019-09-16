package com.scheduling.cronjobs.service.jobs.impl.jobs;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import com.scheduling.cronjobs.domain.jobs.CronJob;
import com.scheduling.cronjobs.service.jobs.CronJobExecution;
import com.scheduling.cronjobs.service.jobs.CronJobLoggingService;
import com.scheduling.cronjobs.service.jobs.CronJobService;
import com.scheduling.cronjobs.util.enumeration.jobs.JobResult;
import com.scheduling.cronjobs.util.enumeration.jobs.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCronJob implements CronJobExecution {

    protected final PlatformTransactionManager transactionManager;
    protected final CronJobService             cronJobService;
    protected final CronJobLoggingService      loggingService;

    /**
     * Метод содержащий системную логику Job-а
     *
     * @param cronJobCode     уникальный cron job код
     * @param isInTransaction флаг запуска бизнес логики в транзкии или без
     */
    public void run(final String cronJobCode, boolean isInTransaction) {
        log.info(String.format("Start CronJob with code '%s'", cronJobCode));
        /* Фромирование уникального кода для лога cron job */
        val startDateTime = LocalDateTime.now(ZoneOffset.UTC);
        val uniqueLogCode = String.format(
                "%s_%s",
                cronJobCode,
                startDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        /* At first, we set out cronJob as Running */
        this.launchCronJob(cronJobCode, uniqueLogCode, startDateTime);

        if (isInTransaction) {
            /* After that, our load SF Job Request process is started with another transaction */
            this.launchBusinessLogicWithTransaction(cronJobCode, uniqueLogCode);
        } else {
            /* After that, our load SF Job Request process is started without transaction */
            this.launchBusinessLogicWithoutTransaction(cronJobCode, uniqueLogCode);
        }
    }

    /**
     * Метод запуска cron job
     */
    private void launchCronJob(final String cronJobCode,
                               final String uniqueLogCode,
                               final LocalDateTime startDateTime) {
        loggingService.loggingStart(cronJobCode, uniqueLogCode, startDateTime);

        final TransactionDefinition setRunningTransaction = new DefaultTransactionDefinition();
        final TransactionStatus setRunningStatus = transactionManager.getTransaction(setRunningTransaction);
        try {
            val cronJob = cronJobService.getCronJobByCode(cronJobCode);
            if (Objects.isNull(cronJob)) {
                throw new EntityNotFoundException(
                        String.format("CronJob with code '%s' not found", cronJobCode)
                );
            }

            if (JobStatus.RUNNING.equals(cronJob.getJobStatus())) {
                log.info(String.format("Stop CronJob with code '%s', Reason: CronJob is already running", cronJobCode));
                transactionManager.rollback(setRunningStatus);
                loggingService.loggingFail(
                        cronJobCode,
                        uniqueLogCode,
                        LocalDateTime.now(ZoneOffset.UTC),
                        "CronJob is already running"
                );
                return;
            }

            cronJob.setJobStatus(JobStatus.RUNNING);
            cronJob.setLastStartTime(startDateTime);
            log.info(String.format("Set CronJob with code '%s' as Running", cronJobCode));
            transactionManager.commit(setRunningStatus);
        } catch (Exception e) {
            log.info(String.format("Error on setting CronJob with code '%s' as Running", cronJobCode));
            log.info(e.getMessage(), e);
            transactionManager.rollback(setRunningStatus);
            loggingService.loggingFail(cronJobCode, uniqueLogCode, LocalDateTime.now(ZoneOffset.UTC), e.getMessage());
            throw e;
        }
    }

    /**
     * Метод запуска бизнес-логики cron job в единой транзакции
     */
    private void launchBusinessLogicWithTransaction(final String cronJobCode, final String uniqueLogCode) {
        final TransactionDefinition updateSFJobsTransaction = new DefaultTransactionDefinition();
        final TransactionStatus updateSFJobsStatus = transactionManager.getTransaction(updateSFJobsTransaction);
        try {
            val cronJob = cronJobService.getCronJobByCode(cronJobCode);

            /* Run cron job business logic */
            val errorList = this.perform(cronJob);

            val finishDateTime = LocalDateTime.now(ZoneOffset.UTC);
            cronJob.setJobStatus(JobStatus.FINISHED);
            cronJob.setLastFinishTime(finishDateTime);
            cronJob.setLastSuccessStartTime(cronJob.getLastStartTime());
            cronJob.setJobResult(CollectionUtils.isEmpty(errorList) ?
                    JobResult.SUCCESS :
                    JobResult.COMPLETE_WITH_ERRORS);
            log.info(String.format("Successfully finish run CronJob with code '%s'", cronJobCode));
            transactionManager.commit(updateSFJobsStatus);

            loggingService.loggingFinish(cronJobCode, uniqueLogCode, finishDateTime, errorList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(updateSFJobsStatus);

            /* If our transaction fails - roll them and mark our job as failed */
            final TransactionDefinition rollBackJoStatuses = new DefaultTransactionDefinition();
            final TransactionStatus rollBackJobStatus = transactionManager.getTransaction(rollBackJoStatuses);
            log.error(String.format("Stop CronJob with code '%s', Reason: %s", cronJobCode, e.getMessage()), e);
            val cronJob = cronJobService.getCronJobByCode(cronJobCode);
            val finishDateTime = LocalDateTime.now(ZoneOffset.UTC);
            cronJob.setJobStatus(JobStatus.FINISHED);
            cronJob.setLastFinishTime(finishDateTime);
            cronJob.setJobResult(JobResult.FAIL);
            transactionManager.commit(rollBackJobStatus);

            loggingService.loggingFail(cronJobCode, uniqueLogCode, finishDateTime, e.getMessage());
        }
    }

    /**
     * Метод запуска бизнес-логики cron job без транзакции
     * для обеспечения кастомной атомарности внутри бизнес-логики
     */
    private void launchBusinessLogicWithoutTransaction(String cronJobCode, final String uniqueLogCode) {
        try {
            /* Run business logic without open any transaction */
            val errorList = this.perform(cronJobService.getCronJobByCode(cronJobCode));
            /* Open transaction for update cron job information */
            val updateSFJobsTransaction = new DefaultTransactionDefinition();
            val updateSFJobsStatus = transactionManager.getTransaction(updateSFJobsTransaction);

            val cronJob = cronJobService.getCronJobByCode(cronJobCode);
            val finishDateTime = LocalDateTime.now(ZoneOffset.UTC);
            cronJob.setJobStatus(JobStatus.FINISHED);
            cronJob.setLastFinishTime(finishDateTime);
            cronJob.setLastSuccessStartTime(cronJob.getLastStartTime());
            cronJob.setJobResult(CollectionUtils.isEmpty(errorList) ?
                    JobResult.SUCCESS :
                    JobResult.COMPLETE_WITH_ERRORS);
            log.info(String.format("Successfully finish run CronJob with code '%s'", cronJobCode));
            transactionManager.commit(updateSFJobsStatus);

            loggingService.loggingFinish(cronJobCode, uniqueLogCode, finishDateTime, errorList);
        } catch (Exception e) {
            val rollBackJoStatuses = new DefaultTransactionDefinition();
            val rollBackJobStatus = transactionManager.getTransaction(rollBackJoStatuses);

            val cronJob = cronJobService.getCronJobByCode(cronJobCode);
            val finishDateTime = LocalDateTime.now(ZoneOffset.UTC);
            cronJob.setJobStatus(JobStatus.FINISHED);
            cronJob.setLastFinishTime(LocalDateTime.now(ZoneOffset.UTC));
            cronJob.setJobResult(JobResult.FAIL);
            log.error(String.format("Stop CronJob with code '%s', Reason: %s", cronJobCode, e.getMessage()), e);
            transactionManager.commit(rollBackJobStatus);

            loggingService.loggingFail(cronJobCode, uniqueLogCode, finishDateTime, e.getMessage());
        }
    }

    /**
     * Метод содержащий бизнес-логику Job-a
     *
     * @param cronJob необходимый Job
     * @return лист ошибок, возникший при выполнении, или empty лист
     */
    public abstract List perform(final CronJob cronJob);
}
