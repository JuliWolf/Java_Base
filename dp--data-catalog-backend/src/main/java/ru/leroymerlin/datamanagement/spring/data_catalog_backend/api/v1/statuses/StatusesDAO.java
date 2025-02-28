package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Status;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

@Service
public class StatusesDAO {
  @Autowired
  protected StatusRepository statusRepository;

  public Status findStatusById (UUID statusId) throws StatusNotFoundException {
    Optional<Status> status = statusRepository.findById(statusId);

    if (status.isEmpty()) {
      throw new StatusNotFoundException(statusId);
    }

    if (status.get().getIsDeleted()) {
      throw new StatusNotFoundException(statusId);
    }

    return status.get();
  }

  public List<Status> findAllByStatusIds (List<UUID> statusIds) {
    return statusRepository.findAllByStatusIds(statusIds);
  }
}
