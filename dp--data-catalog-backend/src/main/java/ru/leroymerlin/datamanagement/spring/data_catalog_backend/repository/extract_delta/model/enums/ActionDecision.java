package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum ActionDecision {
  I("POST"),

  U("PATCH"),

  N("NO ACTION"),

  D("DELETE");

  private String value;

  ActionDecision (String value) {
    this.value = value;
  }
}
