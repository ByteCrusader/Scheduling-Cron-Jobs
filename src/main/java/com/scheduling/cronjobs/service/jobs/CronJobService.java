package com.scheduling.cronjobs.service.jobs;

import java.util.List;
import com.scheduling.cronjobs.domain.jobs.CronJob;

public interface CronJobService {

    /**
     * Получить список фоновых заданий
     *
     * @return фоновые задания
     */
    List<CronJob> getAllCronJobs();

    /**
     * Получить объект фонового задания по коду
     *
     * @param code уникальный код задания
     * @return объект фонового задания
     */
    CronJob getCronJobByCode(String code);
}
