package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums;

/**
 * @author juliwolf
 */

public enum JobStatus {
  NEW,

  DUMPED_DB,

  DB_PROCESS,

  STAGE_COMPLETE,

  API_START,

  SUCCESS
}
