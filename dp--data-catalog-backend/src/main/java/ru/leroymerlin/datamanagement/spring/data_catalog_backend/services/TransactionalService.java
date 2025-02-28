package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.sql.SQLException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.TransactionalProcess;

/**
 * @author juliwolf
 */

@Service
public class TransactionalService {
  @Retryable(value = { SQLException.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void processTransaction (TransactionalProcess process) {
    process.execute();
  }
}
