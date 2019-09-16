package com.scheduling.cronjobs.service.jobs.impl;

import java.util.List;
import com.scheduling.cronjobs.domain.jobs.CronJob;
import com.scheduling.cronjobs.repository.jobs.CronJobRepository;
import com.scheduling.cronjobs.service.jobs.CronJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CronJobServiceImpl implements CronJobService {

    private final CronJobRepository cronJobRepository;

    @Override
    public List<CronJob> getAllCronJobs() {
        return cronJobRepository.findAll();
    }

    @Override
    public CronJob getCronJobByCode(final String code) {
        return cronJobRepository.findCronJobByCode(code).orElse(null);
    }
}
