package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface BusinessTermV1 {
  String getIdText();

  default UUID getId () {
    if (StringUtils.isEmpty(getIdText())) return null;

    return UUID.fromString(getIdText());
  }

  String getName();

  String getTechnicalName();

  String getDescription();

  String getOwnerLdap();

  String getSynonyms();

  default Set<String> getBfSynonymsSplitted () {
    if (StringUtils.isEmpty(getSynonyms())) return null;

    return Arrays.stream(getSynonyms().split("--")).collect(Collectors.toSet());
  }
}

