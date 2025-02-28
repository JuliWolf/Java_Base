package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.time.DateUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;

/**
 * @author JuliWolf
 */
public class JWTUtilsStub {
  private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS256;

  public static String generateJWTToken(String secretKey, String username) {
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, ALGORITHM.getJcaName());

    Map<String, Object> claims = new HashMap<>();
    claims.put("username", username);

    Date expirationDate = DateUtils.addDays(new Date(), 1);

    return Jwts
        .builder()
        .setClaims(claims)
        .setExpiration(expirationDate)
        .signWith(ALGORITHM, signingKey)
        .compact();
  }

  public static DefaultClaims decodeJWT(String jwt, String secretKey) {
    return (DefaultClaims) Jwts.parser()
        .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
        .parse(jwt)
        .getBody();
  }
}
