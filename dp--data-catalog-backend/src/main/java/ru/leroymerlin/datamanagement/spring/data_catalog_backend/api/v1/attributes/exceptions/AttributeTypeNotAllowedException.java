package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions;

import java.util.Map;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.ErrorWithDetail;

/**
 * @author JuliWolf
 */
@Getter
public class AttributeTypeNotAllowedException extends RuntimeExceptionWithDetails implements ErrorWithDetail {
  private Map<String, Object> details;

  public AttributeTypeNotAllowedException () {
    super("This attribute type is not allowed to be used for this asset. ");
  }

  public AttributeTypeNotAllowedException (Map<String, Object> details) {
    this();

    this.details = details;
  }
}
