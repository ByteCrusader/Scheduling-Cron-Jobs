package com.scheduling.cronjobs.domain.jobs;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import com.scheduling.cronjobs.domain.BaseEntity;
import com.scheduling.cronjobs.util.enumeration.jobs.JobResult;
import com.scheduling.cronjobs.util.enumeration.jobs.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class CronJob extends BaseEntity {

    /**
     * Код cronJob
     */
    private String code;

    /**
     * Время запуска
     */
    private LocalDateTime lastStartTime;

    /**
     * Время окончания
     */
    private LocalDateTime lastFinishTime;

    /**
     * Время успешного окончания
     */
    private LocalDateTime lastSuccessStartTime;

    /**
     * Результат выполнения
     */
    @Enumerated(value = EnumType.STRING)
    private JobResult jobResult;

    /**
     * Статус Cron Job
     */
    @Enumerated(value = EnumType.STRING)
    private JobStatus jobStatus;

    /**
     * Расписание выполнение Cron Job
     */
    private String jobSchedule;

    /**
     * Активность Cron Job
     */
    private Boolean active;

}
