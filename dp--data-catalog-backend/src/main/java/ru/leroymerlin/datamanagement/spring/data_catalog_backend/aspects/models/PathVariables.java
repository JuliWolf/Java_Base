package ru.leroymerlin.datamanagement.spring.data_catalog_backend.aspects.models;

import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PathVariables {
  private HashMap<String, String> variables;
}
