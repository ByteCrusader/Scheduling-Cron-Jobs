package com.scheduling.cronjobs.service.jobs;

import java.time.LocalDateTime;
import java.util.List;
import com.scheduling.cronjobs.domain.jobs.CronJobError;

/**
 * Сервис журналирования работы заданий по расписанию
 */
public interface CronJobLoggingService {

    /**
     * Логирование запуска задания по расписанию в БД
     *
     * @param cronJobCode   код задания по расписанию
     * @param uniqueLogCode уникальный код лога для задания
     * @param startTime     дата и время запуска задания
     */
    void loggingStart(String cronJobCode, String uniqueLogCode, LocalDateTime startTime);

    /**
     * Логирование завершения задания по расписанию в БД
     *
     * @param cronJobCode   код задания по расписанию
     * @param uniqueLogCode уникальный код лога для задания
     * @param finishTime    дата и время завершения задания
     * @param errorList     список ошибок, возникших во время работы задания
     */
    void loggingFinish(String cronJobCode,
                       String uniqueLogCode,
                       LocalDateTime finishTime,
                       List<CronJobError> errorList);

    /**
     * Логирование падения задания по расписанию в БД
     *
     * @param cronJobCode     код задания по расписанию
     * @param uniqueLogCode   уникальный код лога для задания
     * @param finishTime      дата и время завершения задания
     * @param businessMessage бизнес сообщение ошибки задания
     */
    void loggingFail(String cronJobCode, String uniqueLogCode, LocalDateTime finishTime, String businessMessage);

    /**
     * Логировать сообщение об ошибке
     *
     * @param entityId            идентификатор сущности
     * @param entityType          тип сущности
     * @param e                   ошибка
     * @param errorList           список ошибок
     * @param messageTemplateName название шаблона сообщения
     * @param parameters          параметры для шаблона
     */
    void loggingException(String entityId,
                          String entityType,
                          Exception e,
                          List<CronJobError> errorList,
                          String messageTemplateName,
                          Object... parameters);
}
