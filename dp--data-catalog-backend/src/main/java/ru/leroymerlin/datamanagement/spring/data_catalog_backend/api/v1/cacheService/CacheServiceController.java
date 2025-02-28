package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.cacheService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;

@RestController
@RequestMapping("/v1")
public class CacheServiceController {

  @Autowired
  private RoleActionCachingService roleActionCachingService;

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/clear")
  public ResponseEntity<Object> clearCache () {
    roleActionCachingService.clearCache();

    return ResponseEntity
      .ok()
      .contentType(MediaType.APPLICATION_JSON)
      .body("{\"result\":\"Role action cache successfully cleared.\"}");
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/cache")
  public ResponseEntity<Object> getCache () {
    return ResponseEntity
      .ok()
      .contentType(MediaType.APPLICATION_JSON)
      .body(roleActionCachingService.getCache());
  }
}
