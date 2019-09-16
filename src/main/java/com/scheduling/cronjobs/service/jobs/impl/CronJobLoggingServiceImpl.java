package com.scheduling.cronjobs.service.jobs.impl;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import com.scheduling.cronjobs.domain.jobs.CronJobError;
import com.scheduling.cronjobs.domain.jobs.CronJobLog;
import com.scheduling.cronjobs.repository.jobs.CronJobErrorRepository;
import com.scheduling.cronjobs.repository.jobs.CronJobLogRepository;
import com.scheduling.cronjobs.service.jobs.CronJobLoggingService;
import com.scheduling.cronjobs.util.enumeration.jobs.JobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CronJobLoggingServiceImpl implements CronJobLoggingService {

    private final CronJobLogRepository   logRepository;
    private final CronJobErrorRepository errorRepository;

    @Override
    public void loggingStart(String cronJobCode, String uniqueLogCode, LocalDateTime startTime) {
        val startLog = CronJobLog.builder()
                .code(cronJobCode)
                .uniqueCode(uniqueLogCode)
                .startTime(startTime)
                .build();

        logRepository.save(startLog);
    }

    @Override
    public void loggingFinish(String cronJobCode,
                              String uniqueLogCode,
                              LocalDateTime finishTime,
                              List<CronJobError> errorList) {
        val cronJobLog = logRepository.findByUniqueCode(uniqueLogCode)
                .orElseThrow(EntityNotFoundException::new);

        cronJobLog.setFinishTime(finishTime);
        cronJobLog.setJobResult(CollectionUtils.isEmpty(errorList) ?
                JobResult.SUCCESS :
                JobResult.COMPLETE_WITH_ERRORS);
        cronJobLog.setErrors(errorList.size());

        logRepository.save(cronJobLog);

        errorList.forEach(entityError -> {
            entityError.setLogUniqueCode(uniqueLogCode);
            errorRepository.save(entityError);
        });
    }

    @Override
    public void loggingFail(String cronJobCode,
                            String uniqueLogCode,
                            LocalDateTime finishTime,
                            String businessMessage) {
        val cronJobLog = logRepository.findByUniqueCode(uniqueLogCode)
                .orElseThrow(EntityNotFoundException::new);

        cronJobLog.setFinishTime(finishTime);
        cronJobLog.setJobResult(JobResult.FAIL);
        cronJobLog.setErrors(1);

        logRepository.save(cronJobLog);

        val error = CronJobError.builder()
                .message(businessMessage)
                .logUniqueCode(uniqueLogCode)
                .build();

        errorRepository.save(error);
    }

    @Override
    public void loggingException(String entityId,
                                 String entityType,
                                 Exception e,
                                 List<CronJobError> errorList,
                                 String messageTemplate,
                                 Object... parameters) {
        val message = String.format(messageTemplate, parameters);

        log.debug(message, e);

        val entityError = CronJobError.builder()
                .entityId(entityId)
                .entityType(entityType)
                .message(message)
                .stacktrace(e.getMessage())
                .build();

        errorList.add(entityError);
    }
}
