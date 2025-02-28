package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificatesResponse {
  private List<CertificateResponse> keys = new ArrayList<>();

  public CertificateResponse getRS256Certificate () {
    return keys.stream().filter(certificate -> certificate.alg.equals("RS256")).findFirst().orElse(null);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CertificateResponse {
    private String alg;

    private List<String> x5c = new ArrayList<>();

    public String getFirstCertificate () {
      return x5c.get(0);
    }
  }
}
