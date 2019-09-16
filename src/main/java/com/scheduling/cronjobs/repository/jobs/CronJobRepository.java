package com.scheduling.cronjobs.repository.jobs;

import java.util.Optional;
import com.scheduling.cronjobs.domain.jobs.CronJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CronJobRepository extends JpaRepository<CronJob, Long>, JpaSpecificationExecutor<CronJob> {

    Optional<CronJob> findCronJobByCode(final String code);
}
