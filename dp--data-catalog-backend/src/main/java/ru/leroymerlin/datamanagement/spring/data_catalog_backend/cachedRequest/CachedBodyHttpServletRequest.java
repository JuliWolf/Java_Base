package ru.leroymerlin.datamanagement.spring.data_catalog_backend.cachedRequest;

import java.io.*;
import org.springframework.util.StreamUtils;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * @author juliwolf
 */

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
  private byte[] cachedBody;

  public byte[] getCachedBody () {
    return cachedBody;
  }

  public CachedBodyHttpServletRequest (HttpServletRequest request) throws IOException {
    super(request);

    InputStream requestInputStream = request.getInputStream();
    this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new CachedBodyServletInputStream(this.cachedBody);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    // Create a reader from cachedContent
    // and return it
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
    return new BufferedReader(new InputStreamReader(byteArrayInputStream));
  }
}
