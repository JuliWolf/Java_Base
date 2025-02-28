package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import logger.LoggerWrapper;

/**
 * @author juliwolf
 */

public class JWTUtils {
  public static Claims decodeJWT(String token, String certificateBase64) {
    try {
      LoggerWrapper.info("Decode JWT", JWTUtils.class.getName());

      byte[] certificateBytes = Base64.getDecoder().decode(certificateBase64);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      PublicKey publicKey = certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes)).getPublicKey();

      return Jwts.parser()
        .setSigningKey(publicKey)
        .parseClaimsJws(token)
        .getBody();
    } catch (CertificateException certificateException) {
      return null;
    }
  }
}
