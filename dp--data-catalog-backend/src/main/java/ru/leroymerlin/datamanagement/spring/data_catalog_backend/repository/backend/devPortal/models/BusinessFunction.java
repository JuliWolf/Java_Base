package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */

public interface BusinessFunction {
  String getBdIdText();

  default UUID getBdId () {
    if (StringUtils.isEmpty(getBdIdText())) return null;

    return UUID.fromString(getBdIdText());
  }

  String getBfName();

  String getBfDescription();

  String getBfSynonyms();

  default Set<String> getBfSynonymsSplitted () {
    if (StringUtils.isEmpty(getBfSynonyms())) return null;

    return Arrays.stream(getBfSynonyms().split("--")).collect(Collectors.toSet());
  }

  String getBfOwnerLdap();

  default Set<String> getBfOwnerLdapSplitted () {
    if (StringUtils.isEmpty(getBfOwnerLdap())) return null;

    return Arrays.stream(getBfOwnerLdap().split("--")).collect(Collectors.toSet());
  }

  String getBfDomainCode();

  default Set<String> getBfDomainCodeSplitted () {
    if (StringUtils.isEmpty(getBfDomainCode())) return null;

    return Arrays.stream(getBfDomainCode().split("--")).collect(Collectors.toSet());
  }
}
