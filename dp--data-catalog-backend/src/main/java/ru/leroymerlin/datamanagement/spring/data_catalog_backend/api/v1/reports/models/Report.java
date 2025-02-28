package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

public abstract class Report {
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class UserLdap {
    private String ldap;
  }
}
