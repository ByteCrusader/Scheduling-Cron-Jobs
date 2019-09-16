package com.scheduling.cronjobs.domain.jobs;

import javax.persistence.Entity;
import com.scheduling.cronjobs.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Данные об ошибке, возникшей при обработке определенной сущности в ходе выполнения cron job
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CronJobError extends BaseEntity {

    /**
     * Идентификатор сущности
     */
    private String entityId;

    /**
     * Тип сущности
     */
    private String entityType;

    /**
     * Уникальный код лога для cron job
     */
    private String logUniqueCode;

    /**
     * Бизнес сообщение ошибки
     */
    private String message;

    /**
     * Начало stacktrace ошибки
     */
    private String stacktrace;
}
