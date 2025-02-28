package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.CacheResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;

/**
 * @author juliwolf
 */

@Service
public class RoleActionCachingService {
  public final static String CACHE_NAME = "roleActions";

  private final Cache cache;

  public RoleActionCachingService (CacheManager cacheManager) {
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  public void clearCache () {
    cache.clear();
  }

  public List<CacheResponse> getCache () {
    ConcurrentHashMap<SimpleKey, List<RoleActionResponse>> values = (ConcurrentHashMap) cache.getNativeCache();

    List<CacheResponse> list = values.entrySet().stream().map((entry) -> {
      CacheResponse cacheResponse = new CacheResponse();
      cacheResponse.setKey(entry.getKey().toString());
      cacheResponse.setRoleActionResponses(entry.getValue());

      return cacheResponse;
    }).toList();

    return list;
  }

  public void evictByValueInKey(String searchKey) {
    LoggerWrapper.info("Trying to evict key by searchKey=" + searchKey, RoleActionCachingService.class.getName());

    List<SimpleKey> keys = getCacheKeyBySearchString(searchKey);

    if (keys.isEmpty()) return;

    keys.forEach(cache::evict);
  }

  public void evictByRoleActionId (UUID roleActionId) {
    if (cache == null) return;

    LoggerWrapper.info("Trying to evict key by roleActionId=" + roleActionId, RoleActionCachingService.class.getName());

    ConcurrentHashMap<SimpleKey, List<RoleActionResponse>> values = (ConcurrentHashMap) cache.getNativeCache();
    values.forEach((key, roleActions) -> {
      if (roleActions.stream().anyMatch(roleActionResponse -> roleActionResponse.getRoleActionId().equals(roleActionId))) {
        cache.evict(key);
      }
    });
  }

  public void evictByRoleId (UUID roleId) {
    if (cache == null) return;

    LoggerWrapper.info("Trying to evict key by roleId=" + roleId, RoleActionCachingService.class.getName());

    ConcurrentHashMap<SimpleKey, List<RoleActionResponse>> values = (ConcurrentHashMap) cache.getNativeCache();
    values.forEach((key, roleActions) -> {
      if (roleActions.stream().anyMatch(roleActionResponse -> roleActionResponse.getRoleId().equals(roleId))) {
        cache.evict(key);
      }
    });
  }

  public List<SimpleKey> getCacheKeyBySearchString (String searchKey) {
    if (cache == null) return null;

    ConcurrentHashMap<SimpleKey, List<RoleActionResponse>> values = (ConcurrentHashMap) cache.getNativeCache();

    ArrayList<SimpleKey> simpleKeys = new ArrayList<>();
    for (Map.Entry<SimpleKey, List<RoleActionResponse>> entry : values.entrySet()) {
      if (entry.getKey().toString().contains(searchKey)) {
        simpleKeys.add(entry.getKey());
      }
    }

    return simpleKeys;
  }
}
