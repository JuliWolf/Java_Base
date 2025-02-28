package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface BusinessTermV2 {
  String getIdText();

  default UUID getId () {
    if (StringUtils.isEmpty(getIdText())) return null;

    return UUID.fromString(getIdText());
  }

  String getName();

  String getTechnicalName();

  String getDefinition();

  String getOwnerLdaps();

  default Set<String> getOwnerLdapsSplitted () {
    if (StringUtils.isEmpty(getOwnerLdaps())) return null;

    return Arrays.stream(getOwnerLdaps().split(",")).collect(Collectors.toSet());
  }

  String getDigitalProducts();

  default Set<String> getDigitalProductsSplitted () {
    if (StringUtils.isEmpty(getDigitalProducts())) return null;

    return Arrays.stream(getDigitalProducts().split(",")).collect(Collectors.toSet());
  }

  String getSynonyms();

  default Set<String> getBfSynonymsSplitted () {
    if (StringUtils.isEmpty(getSynonyms())) return null;

    return Arrays.stream(getSynonyms().split(",")).collect(Collectors.toSet());
  }
}
