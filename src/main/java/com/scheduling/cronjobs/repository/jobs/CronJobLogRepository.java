package com.scheduling.cronjobs.repository.jobs;

import java.util.Optional;
import com.scheduling.cronjobs.domain.jobs.CronJobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CronJobLogRepository extends JpaRepository<CronJobLog, Long>, JpaSpecificationExecutor<CronJobLog> {

    Optional<CronJobLog> findByUniqueCode(String uniqueCode);
}
