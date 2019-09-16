package com.scheduling.cronjobs.domain.jobs;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import com.scheduling.cronjobs.domain.BaseEntity;
import com.scheduling.cronjobs.util.enumeration.jobs.JobResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Данные логирования выполнения задания по расписанию cron job
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CronJobLog extends BaseEntity {

    /**
     * Код cron job
     */
    private String code;

    /**
     * Уникальный код лога для cron job
     */
    private String uniqueCode;

    /**
     * Время начала выполнения cron job
     */
    private LocalDateTime startTime;

    /**
     * Время окончания выполнения cron job
     */
    private LocalDateTime finishTime;

    /**
     * Результат выполнения cron job
     */
    @Enumerated(value = EnumType.STRING)
    private JobResult jobResult;

    /**
     * Количество ошибок, возникших при работе cron job
     */
    private Integer errors;
}
