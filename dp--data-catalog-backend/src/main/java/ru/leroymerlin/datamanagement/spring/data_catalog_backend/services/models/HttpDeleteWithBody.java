package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models;

import java.net.URI;
import lombok.NoArgsConstructor;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * @author juliwolf
 */

@NoArgsConstructor
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
  public static final String METHOD_NAME = "DELETE";

  public HttpDeleteWithBody(URI uri) {
    this.setURI(uri);
  }

  public HttpDeleteWithBody(String uri) {
    this.setURI(URI.create(uri));
  }
  @Override
  public String getMethod () {
    return "DELETE";
  }
}
