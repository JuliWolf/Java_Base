package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.caching;

import org.springframework.cache.concurrent.ConcurrentMapCache;

/**
 * @author juliwolf
 */

public class SizeLimitedMapCache extends ConcurrentMapCache {

  private final int maxSize;

  public SizeLimitedMapCache (String name, boolean allowNullValues, int maxSize) {
    super(name, allowNullValues);
    this.maxSize = maxSize;
  }

  @Override
  public void put(Object key, Object value) {
    if (super.getNativeCache().size() >= maxSize) {
      // Remove the eldest entry (FIFO)
      super.getNativeCache().remove(super.getNativeCache().keySet().iterator().next());
    }
    super.put(key, value);
  }
}
