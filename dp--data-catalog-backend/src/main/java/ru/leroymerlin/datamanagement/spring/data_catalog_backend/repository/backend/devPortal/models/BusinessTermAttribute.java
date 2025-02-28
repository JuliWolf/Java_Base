package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface BusinessTermAttribute {
  String getBaIdText();

  default UUID getBaId () {
    if (StringUtils.isEmpty(getBaIdText())) return null;

    return UUID.fromString(getBaIdText());
  }

  String getBaName();

  String getBaTechName();

  String getBaDefinition();

  String getBaDataType();

  String getBaConfidentiality();

  String getBaPrimaryKeyValue();

  default boolean getBaPrimaryKey () {
    if (StringUtils.isEmpty(getBaPrimaryKeyValue())) return false;

    return getBaPrimaryKeyValue().equals("PK");
  }

  String getBaSynonyms();

  default Set<String> getBaSynonymsSplitted () {
    if (StringUtils.isEmpty(getBaSynonyms())) return null;

    return Arrays.stream(getBaSynonyms().split("--")).collect(Collectors.toSet());
  }
}
