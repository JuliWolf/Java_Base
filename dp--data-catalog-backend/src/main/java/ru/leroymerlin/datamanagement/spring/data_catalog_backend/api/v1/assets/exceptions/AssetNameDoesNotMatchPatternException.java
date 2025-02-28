package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;

/**
 * @author JuliWolf
 */
@Getter
public class AssetNameDoesNotMatchPatternException extends RuntimeExceptionWithDetails {
  private Map<String, Object> details;

  public AssetNameDoesNotMatchPatternException (String pattern) {
    super("Asset name doesn't match '" + pattern +"' pattern");
  }

  public AssetNameDoesNotMatchPatternException (String pattern, Map<String, Object> details) {
    this(pattern);

    this.details = details;
  }
}
