package com.scheduling.cronjobs.service.jobs;

public interface CronJobInitializer {

    /**
     * Инициализация активных Job по расписанию
     */
    void initializeCronJobs();

    /**
     * Исключение Job из выполнения по cronJob коду
     *
     * @param cronJobCode cronJob код
     */
    void removeRemainingCronJob(String cronJobCode);

    /**
     * Добавление Job на выполнение по расписанию по cronJob коду
     *
     * @param cronJobCode cronJob код
     */
    void startScheduledCronJob(String cronJobCode);

    /**
     * Одиночный запуск Job не по расписанию по cronJob коду
     *
     * @param cronJobCode cronJob код
     */
    void startOnceCronJob(final String cronJobCode);
}
