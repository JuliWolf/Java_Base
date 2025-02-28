package ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants;

/**
 * @author JuliWolf
 */

public class AuthConstants {
  public static final String SITE_DOMAIN  = System.getenv("SITE_DOMAIN");

  public static final String JWT_SECRET_KEY = System.getenv("JWT_SECRET_KEY");

  public static final String HEADER_TOKEN = "X-Auth-Token";

  public static final String DEV_PORTAL_HEADER_TOKEN = "X-Api-Key";

  public static final String JWT_EXPIRED = "jwt-expired";
}
