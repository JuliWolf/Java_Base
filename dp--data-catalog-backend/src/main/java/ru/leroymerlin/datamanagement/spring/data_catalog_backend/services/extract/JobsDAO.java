package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions.ExtractJobNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class JobsDAO {
  @Autowired
  protected ExtractJobRepository extractJobRepository;

  public ExtractJob findExtractJobById (UUID extractJobId) throws ExtractJobNotFoundException {
    Optional<ExtractJob> extractJob = extractJobRepository.findById(extractJobId);

    if (extractJob.isEmpty()) {
      throw new ExtractJobNotFoundException();
    }

    return extractJob.get();
  }
}
