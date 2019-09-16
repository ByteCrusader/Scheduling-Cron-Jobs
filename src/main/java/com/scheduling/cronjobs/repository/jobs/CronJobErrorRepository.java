package com.scheduling.cronjobs.repository.jobs;

import com.scheduling.cronjobs.domain.jobs.CronJobError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CronJobErrorRepository
        extends JpaRepository<CronJobError, Long>, JpaSpecificationExecutor<CronJobError> {
}
