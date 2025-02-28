package ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.WithMockCustomUserSecurityContextFactory;

/**
 * @author JuliWolf
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

  String username() default "test_username";

  String firstName() default "test_firstName";

  String[] allowedRoles() default {"Admin", "Extract Service"};

  boolean isAuthorized() default true;

  boolean hasUserDeniedRoleId () default false;
  boolean hasUserAllowedRoleId () default false;
  boolean hasGroupDeniedRoleId () default false;
  boolean hasGroupAllowedRoleId () default false;

  boolean hasUserDeniedAssetTypeId () default false;
  boolean hasUserAllowedAssetTypeId () default false;
  boolean hasGroupDeniedAssetTypeId () default false;
  boolean hasGroupAllowedAssetTypeId () default false;

  boolean hasUserDeniedAttributeTypeId () default false;
  boolean hasUserAllowedAttributeTypeId () default false;
  boolean hasGroupDeniedAttributeTypeId () default false;
  boolean hasGroupAllowedAttributeTypeId () default false;

  boolean hasUserDeniedRelationTypeId () default false;
  boolean hasUserAllowedRelationTypeId () default false;
  boolean hasGroupDeniedRelationTypeId () default false;
  boolean hasGroupAllowedRelationTypeId () default false;

  boolean hasUserAllAllowedRoleActions () default true;
  boolean hasGroupAllAllowedRoleActions () default false;

  boolean hasUserAllDeniedRoleActions () default false;
  boolean hasGroupAllDeniedRoleActions () default false;

  boolean hasUserAssetAllowedRoleActions () default true;
  boolean hasUserAssetDeniedRoleActions () default false;
}
