package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.scheduler;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import logger.LoggerWrapper;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;

/**
 * @author juliwolf
 */

@Service
public class ExtractScheduler {

  public static final ZoneId UTC_ZONE_ID = ZoneId.of(ZoneOffset.UTC.getId());

  private static final String maxTimeoutMinutesString = System.getenv("EXTRACT_MAX_TIMEOUT_MINUTES");
  private static final Integer maxTimeoutMinutes = (maxTimeoutMinutesString != null && maxTimeoutMinutesString.matches("[0-9.]+"))
    ? Integer.parseInt(maxTimeoutMinutesString)
    : 100000;

  private final ExtractJobRepository extractJobRepository;

  private final JobsProcessService jobsProcessService;

  public ExtractScheduler (
    ExtractJobRepository extractJobRepository,
    JobsProcessService jobsProcessService
  ) {
    this.extractJobRepository = extractJobRepository;
    this.jobsProcessService = jobsProcessService;
  }

  @Scheduled(cron = "${extract.scheduler.jobExecutor.interval-in-cron}")
  @SchedulerLock(name = "schedulerJobExecutor")
  public void schedulerJobExecutor () {
    LoggerWrapper.info("Running new schedulerJobExecutor");

    jobsProcessService.updateExpiredJobs(UTC_ZONE_ID, maxTimeoutMinutes);

    List<ExtractJob> jobs = extractJobRepository.findActiveJobs();
    Map<UUID, ExtractJob> jobsById = jobs.stream()
      .collect(Collectors.toMap(ExtractJob::getJobId, job -> job));

    jobsProcessService.computeCheckSums(jobsById);

    jobsProcessService.checkOutdatedFunctions(jobsById);

    jobsProcessService.executeFunctions(jobsById);

    jobsProcessService.executeUpdate();
  }

  @Scheduled(cron = "${extract.scheduler.clearer.interval-in-cron}")
  @SchedulerLock(name = "schedulerClearerExecutor")
  public void schedulerClearerExecutor () {
    LoggerWrapper.info("Running new schedulerClearerExecutor");

    jobsProcessService.clearDataOfFinishedJobs(UTC_ZONE_ID);
  }
}
