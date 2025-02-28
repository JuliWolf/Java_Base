package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestObject {
  private String name;

  private Integer age;

  private Boolean isYoung;
}
