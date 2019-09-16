package com.scheduling.cronjobs.service.jobs.impl.jobs;

import java.util.ArrayList;
import java.util.List;
import com.scheduling.cronjobs.domain.jobs.CronJob;
import com.scheduling.cronjobs.domain.jobs.CronJobError;
import com.scheduling.cronjobs.service.jobs.CronJobLoggingService;
import com.scheduling.cronjobs.service.jobs.CronJobService;
import com.scheduling.cronjobs.service.jobs.CronJobTask;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionSystemException;

import static com.scheduling.cronjobs.util.constants.CronJobHelper.EXAMPLE_CRON_JOB;

/**
 * Фоновое задание, созданное для примера работы системы
 */
@CronJobTask(EXAMPLE_CRON_JOB)
@Slf4j
public class ExampleCronJob extends AbstractCronJob {

    private final CronJobLoggingService cronJobLoggingService;

    public ExampleCronJob(PlatformTransactionManager transactionManager,
                          CronJobLoggingService cronJobLoggingService,
                          CronJobService cronJobService) {
        super(transactionManager, cronJobService, cronJobLoggingService);
        this.cronJobLoggingService = cronJobLoggingService;
    }

    @Override
    public void run() {
        super.run(EXAMPLE_CRON_JOB, true);
    }

    @Override
    public List perform(final CronJob cronJob) {
        val errorList = new ArrayList<CronJobError>();

        try {

            log.info("Add some logic into this case");

        } catch (TransactionSystemException e) {
            cronJobLoggingService.loggingException(
                    null,
                    CronJob.class.getTypeName(),
                    e,
                    errorList,
                    "Example cron job logic failed"
            );
        }

        log.info("ExampleCronJob: Finish Example Cron Job");

        return errorList;
    }
}
