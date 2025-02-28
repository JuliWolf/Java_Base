package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;


import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.get.GetJobsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateResponse;

/**
 * @author juliwolf
 */

public interface JobsService {
  GetJobsResponse extractJobs();

  PostExecuteUpdateResponse executeUpdate (PostExecuteUpdateRequest request, User user);
}
