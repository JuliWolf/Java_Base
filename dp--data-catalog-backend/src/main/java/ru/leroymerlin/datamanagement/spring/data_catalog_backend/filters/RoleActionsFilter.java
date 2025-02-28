package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters;

import java.io.IOException;
import java.util.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.cachedRequest.CachedBodyHttpServletRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security.models.RoleActionEntityName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionTypeName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.models.RelationTypeComponentWithRelationType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationComponentAndAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responseModels.roleAction.RoleActionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeCardHeaderAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.AssetTypeStatusesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeAllowedValueNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchBulkAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypeComponents.RelationTypeComponentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.RelationConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.SubscriptionsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.SubscriptionNotFoundException;

/**
 * @author juliwolf
 */

@Order(3)
@Component
public class RoleActionsFilter extends OncePerRequestFilter {
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  private final RoleActionsDAO roleActionsDAO;
  private final AssetsDAO assetsDAO;
  private final AttributesDAO attributesDAO;
  private final AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO;
  private final AssetTypeStatusesAssignmentsDAO assetTypeStatusesAssignmentsDAO;
  private final RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO;
  private final RelationTypeComponentsDAO relationTypeComponentsDAO;
  private final AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO;
  private final ResponsibilitiesDAO responsibilitiesDAO;
  private final GlobalResponsibilitiesDAO globalResponsibilitiesDAO;
  private final RelationsDAO relationsDAO;
  private final SubscriptionsDAO subscriptionsDAO;
  private final RelationAttributesDAO relationAttributesDAO;
  private final RelationComponentAttributesDAO relationComponentAttributesDAO;
  private final AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO;

  public RoleActionsFilter (
    RoleActionsDAO roleActionsDAO,
    AssetsDAO assetsDAO,
    AttributesDAO attributesDAO,
    AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO,
    AssetTypeStatusesAssignmentsDAO assetTypeStatusesAssignmentsDAO,
    RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO,
    RelationTypeComponentsDAO relationTypeComponentsDAO,
    AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO,
    ResponsibilitiesDAO responsibilitiesDAO,
    GlobalResponsibilitiesDAO globalResponsibilitiesDAO,
    RelationsDAO relationsDAO,
    SubscriptionsDAO subscriptionsDAO,
    RelationAttributesDAO relationAttributesDAO,
    RelationComponentAttributesDAO relationComponentAttributesDAO,
    AssetTypeCardHeaderAssignmentDAO assetTypeCardHeaderAssignmentDAO
  ) {
    this.roleActionsDAO = roleActionsDAO;
    this.assetsDAO = assetsDAO;
    this.attributesDAO = attributesDAO;
    this.assetTypeAttributeTypesAssignmentsDAO = assetTypeAttributeTypesAssignmentsDAO;
    this.assetTypeStatusesAssignmentsDAO = assetTypeStatusesAssignmentsDAO;
    this.relationTypeAttributeTypesAssignmentsDAO = relationTypeAttributeTypesAssignmentsDAO;
    this.relationTypeComponentsDAO = relationTypeComponentsDAO;
    this.attributeTypesAllowedValuesDAO = attributeTypesAllowedValuesDAO;
    this.responsibilitiesDAO = responsibilitiesDAO;
    this.globalResponsibilitiesDAO = globalResponsibilitiesDAO;
    this.relationsDAO = relationsDAO;
    this.subscriptionsDAO = subscriptionsDAO;
    this.relationAttributesDAO = relationAttributesDAO;
    this.relationComponentAttributesDAO = relationComponentAttributesDAO;
    this.assetTypeCardHeaderAssignmentDAO = assetTypeCardHeaderAssignmentDAO;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    LoggerWrapper.info(
      "Start role actions filter",
      RoleActionsFilter.class.getName()
    );

    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      filterChain.doFilter(request, response);

      return;
    }

    EntityNameType requestEntityTypeName = computeEntityName(request);

    if (requestEntityTypeName == null) {
      filterChain.doFilter(request, response);

      return;
    }

    CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

    RequestValues requestValues = new RequestValues();
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    List<EntityNameType> entityTypeNames = computeEntityNamesForDefaultMethod(request, requestEntityTypeName);
    List<ActionTypeName> actionTypeNames = computeActionTypes(request, entityTypeNames);

    parseRequestParams(wrappedRequest, requestValues, userDetails);
    parsePathVariablesFromUrl(wrappedRequest, requestValues, userDetails);

    loadUserRoleActions(authentication, actionTypeNames, entityTypeNames, requestValues);

    if (requestValues.getAssetIds().isEmpty()) {
      filterChain.doFilter(wrappedRequest, response);

      return;
    }

    loadAssetRoleActions(authentication, actionTypeNames, requestEntityTypeName, requestValues);

    filterChain.doFilter(wrappedRequest, response);
  }

  private void parseRequestParams (CachedBodyHttpServletRequest request, RequestValues requestValues, AuthUserDetails userDetails) {
    try {
      if (request.getMethod().equals("GET")) {
        parseQueryString(request, requestValues);
      } else {
        parseRequestBody(request, requestValues, userDetails);
      }
    } catch (IOException ignored) {
    }
  }

  private void parseRequestBody (CachedBodyHttpServletRequest request, RequestValues requestValues, AuthUserDetails userDetails) throws IOException {
    BodyValueCallback defaultCallback = (body, values, _details) -> {
      Map<String, Object> jsonRequest = new ObjectMapper().readValue(body, Map.class);

      jsonRequest.entrySet().stream()
        .filter(entry -> isSearchEntry(entry.getKey(), entry.getValue(), values))
        .forEach(entry -> {
          values.putValue(entry.getKey(), UUID.fromString((String) entry.getValue()));
        });
    };

    BodyValueCallback relationsCallback = (body, values, _details) -> {
      PostRelationsRequest relationsRequest = new ObjectMapper().readValue(body, PostRelationsRequest.class);

      List<PostRelationRequest> requestList = relationsRequest.getComponent();
      requestList.forEach(component -> {
        if (UUIDUtils.isValidUUID(component.getAsset_id())) {
          values.putValue(values.ASSET_ID_KEY, UUID.fromString(component.getAsset_id()));
        }
      });
    };

    BodyValueCallback assetsBulkCallback = (body, values, _details) -> {
      List<PostOrPatchAssetRequest> assetsRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});

      assetsRequestList.forEach(item -> {
        if (UUIDUtils.isValidUUID(item.getAsset_type_id())) {
          values.putValue(values.ASSET_TYPE_KEY, UUID.fromString(item.getAsset_type_id()));
        }
      });
    };

    BodyValueCallback updateAssetsBulkCallback = (body, values, details) -> {
      List<PatchAssetRequest> assetsRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});
      List<Asset> assets = assetsDAO.findAllByAssetIds(assetsRequestList.stream()
        .map(PatchAssetRequest::getAsset_id)
        .filter(Objects::nonNull)
        .toList());

      assets.forEach(item -> {
        values.putValue(values.ASSET_TYPE_KEY, item.getAssetType().getAssetTypeId());
        values.putValue(values.ASSET_ID_KEY, item.getAssetId());

        details.setConnectedValue(item.getAssetId().toString(), RoleActionEntityName.ASSET_TYPE, item.getAssetType().getAssetTypeId().toString());
      });
    };

    BodyValueCallback deleteAssetsBulkCallback = (body, values, details) -> {
      List<UUID> assetsRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});
      List<Asset> assets = assetsDAO.findAllByAssetIds(assetsRequestList);

      assets.forEach(item -> {
        values.putValue(values.ASSET_TYPE_KEY, item.getAssetType().getAssetTypeId());
        values.putValue(values.ASSET_ID_KEY, item.getAssetId());

        details.setConnectedValue(item.getAssetId().toString(), RoleActionEntityName.ASSET_TYPE, item.getAssetType().getAssetTypeId().toString());
      });
    };

    BodyValueCallback attributesBulkCallback = (body, values, _details) -> {
      List<PostAttributeRequest> attributesRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});

      attributesRequestList.forEach(item -> {
        if (UUIDUtils.isValidUUID(item.getAttribute_type_id())) {
          values.putValue(values.ATTRIBUTE_TYPE_KEY, UUID.fromString(item.getAttribute_type_id()));
        }

        if (item.getAsset_id() != null) {
          values.putValue(values.ASSET_ID_KEY, item.getAsset_id());
        }
      });
    };

    BodyValueCallback updateAttributesBulkCallback = (body, values, details) -> {
      List<PatchBulkAttributeRequest> attributesRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});
      List<Attribute> attributes = attributesDAO.findAllByAttributeIds(attributesRequestList.stream().map(PatchBulkAttributeRequest::getAttribute_id).toList());

      attributes.forEach(item -> {
        values.putValue(values.ATTRIBUTE_TYPE_KEY, item.getAttributeType().getAttributeTypeId());
        values.putValue(values.ASSET_ID_KEY, item.getAsset().getAssetId());

        details.setConnectedValue(item.getAttributeId().toString(), RoleActionEntityName.ATTRIBUTE_TYPE, item.getAttributeType().getAttributeTypeId().toString());
      });
    };

    BodyValueCallback deleteAttributesBulkCallback = (body, values, details) -> {
      List<UUID> attributesRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});
      List<Attribute> attributes = attributesDAO.findAllByAttributeIds(attributesRequestList);

      attributes.forEach(item -> {
        values.putValue(values.ATTRIBUTE_TYPE_KEY, item.getAttributeType().getAttributeTypeId());
        values.putValue(values.ASSET_ID_KEY, item.getAsset().getAssetId());

        details.setConnectedValue(item.getAttributeId().toString(), RoleActionEntityName.ATTRIBUTE_TYPE, item.getAttributeType().getAttributeTypeId().toString());
      });
    };

    BodyValueCallback relationsBulkCallback = (body, values, details) -> {
      List<PostRelationsRequest> relationsRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});

      relationsRequestList.forEach(item -> {
        if (UUIDUtils.isValidUUID(item.getRelation_type_id())) {
          values.putValue(values.RELATION_TYPE_KEY, UUID.fromString(item.getRelation_type_id()));
        }

        item.getComponent().forEach(component-> {
          if (UUIDUtils.isValidUUID(component.getAsset_id())) {
            values.putValue(values.ASSET_ID_KEY, UUID.fromString(component.getAsset_id()));
          }
        });
      });
    };

    BodyValueCallback deleteRelationsBulkCallback = (body, values, details) -> {
      List<UUID> relationsRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});
      List<RelationWithRelationComponentAndAsset> relationComponents = relationsDAO.findAllByRelationComponentsIds(relationsRequestList);

      relationComponents.forEach(item -> {
        values.putValue(values.RELATION_TYPE_KEY, item.getRelationTypeId());
        values.putValue(values.ASSET_ID_KEY, item.getAssetId());

        details.setConnectedValue(item.getRelationId().toString(), RoleActionEntityName.RELATION_TYPE, item.getRelationTypeId().toString());
      });
    };

    BodyValueCallback responsibilitiesCallback = (body, values, details) -> {
      List<PostResponsibilityRequest> responsbilitiesRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});

      responsbilitiesRequestList.forEach(item -> {
        if (item.getRole_id() != null) {
          values.putValue(values.ROLE_KEY, item.getRole_id());
        }

        if (item.getAsset_id() != null) {
          values.putValue(values.ASSET_ID_KEY, item.getAsset_id());
        }
      });
    };

    BodyValueCallback deleteResponsibilitiesCallback = (body, values, details) -> {
      List<UUID> responsbilitiesRequestList = new ObjectMapper().readValue(body, new TypeReference<>() {});
      List<Responsibility> responsibilities = responsibilitiesDAO.findAllByResponsibilitiesIds(responsbilitiesRequestList);

      responsibilities.forEach(item -> {
        values.putValue(values.ROLE_KEY, item.getRole().getRoleId());
        values.putValue(values.ASSET_ID_KEY, item.getAsset().getAssetId());

        details.setConnectedValue(item.getResponsibilityId().toString(), RoleActionEntityName.ROLE, item.getRole().getRoleId().toString());
      });
    };

    List<BodyValue> pathPatterns = List.of(
      new BodyValue("POST", "/v1/assets/bulk", request, assetsBulkCallback),
      new BodyValue("PATCH", "/v1/assets/bulk", request, updateAssetsBulkCallback),
      new BodyValue("DELETE", "/v1/assets/bulk", request, deleteAssetsBulkCallback),

      new BodyValue("POST", "/v1/attributes/bulk", request, attributesBulkCallback),
      new BodyValue("PATCH", "/v1/attributes/bulk", request, updateAttributesBulkCallback),
      new BodyValue("DELETE", "/v1/attributes/bulk", request, deleteAttributesBulkCallback),

      new BodyValue("POST", "/v1/relations", request, relationsCallback),

      new BodyValue("POST", "/v1/responsibilities/bulk", request, responsibilitiesCallback),
      new BodyValue("DELETE", "/v1/responsibilities/bulk", request, deleteResponsibilitiesCallback),

      new BodyValue("POST", "/v1/relations/bulk", request, relationsBulkCallback),
      new BodyValue("DELETE", "/v1/relations/bulk", request, deleteRelationsBulkCallback),

      new BodyValue(null, null, request, defaultCallback)
    );

    for (int i = 0; i < pathPatterns.size(); i++) {
      BodyValue pattern = pathPatterns.get(i);

      if (StringUtils.isEmpty(pattern.method()) && StringUtils.isEmpty(pattern.pathPattern())) {
        pattern.callback().parseBody(request.getCachedBody(), requestValues, userDetails);

        return;
      }

      if (isPatternRequest(pattern.method(), pattern.pathPattern(), request)) {
        pattern.callback().parseBody(request.getCachedBody(), requestValues, userDetails);
      }
    }
  }

  private void parseQueryString (HttpServletRequest request, RequestValues requestValues) {
    if (StringUtils.isEmpty(request.getQueryString())) {
      return;
    }

    for (String str : request.getQueryString().split("&")) {
      String[] split = str.split("=");

      if (split.length == 1) continue;

      String key = split[0];
      String value = split[1];

      if (isSearchEntry(key, value, requestValues)) {
        requestValues.putValue(key, UUID.fromString(value));
      }
    }
  }

  private List<EntityNameType> computeEntityNamesForDefaultMethod (HttpServletRequest request, EntityNameType entityNameType) {
    List<EntityNameType> entityNameTypes = new ArrayList<>();

    entityNameTypes.add(entityNameType);

    List<EntityName> list = List.of(
      new EntityName(EntityNameType.ASSET, "DELETE", EntityNameType.ASSET_TYPE),
      new EntityName(EntityNameType.ASSET, "PATCH", EntityNameType.ASSET_TYPE)
    );

    for (EntityName entityName : list) {
      if (entityName.method() != null) {
        if (!entityNameType.equals(entityName.entityNameTypeFromRequest())) continue;

        if (!request.getMethod().equals(entityName.method())) continue;

        entityNameTypes.add(entityName.additionalEntityType());
      }
    }

    return entityNameTypes;
  }

  private EntityNameType computeEntityName (HttpServletRequest request) {
    String path = request.getServletPath();
    List<String> splitPath = Arrays.stream(path.split("/")).filter(s -> StringUtils.isNotEmpty(s) && !s.equals("v1")).toList();

    EntityNameType entityNameType = null;
    for (String s : splitPath) {
      Optional<EntityNameType> optionalEntityNameType = Arrays.stream(EntityNameType.getValues())
        .filter(nameType -> nameType.getConnectedValues().contains(s)).findFirst();

      if (optionalEntityNameType.isPresent()) {
        entityNameType = optionalEntityNameType.get();
        break;
      }
    }

    return entityNameType;
  }

  private List<ActionTypeName> computeActionTypes (HttpServletRequest request,  List<EntityNameType> entityTypeNames) {
    List<ActionTypeName> actionTypeNames = new ArrayList<>();
    List<EntityNameType> assignEntityNames = List.of(EntityNameType.RESPONSIBILITY, EntityNameType.RESPONSIBILITY_GLOBAL);

    switch (request.getMethod()) {
      case "GET" -> actionTypeNames.add(ActionTypeName.VIEW);
      case "POST" -> {
        ActionTypeName actionTypeName = assignEntityNames.stream().anyMatch(entityTypeNames::contains)
          ? ActionTypeName.GRANT
          : ActionTypeName.ADD;

        actionTypeNames.add(actionTypeName);
      }
      case "PATCH" -> actionTypeNames.add(ActionTypeName.EDIT);
      case "DELETE" -> {
        ActionTypeName actionTypeName = assignEntityNames.stream().anyMatch(entityTypeNames::contains)
          ? ActionTypeName.REVOKE
          : ActionTypeName.DELETE;

        actionTypeNames.add(actionTypeName);
      }
    }

    return actionTypeNames;
  }

  private void parsePathVariablesFromUrl (HttpServletRequest request, RequestValues requestValues, AuthUserDetails userDetails) {
    PathVariableCallback assetTypeAttributeTypeAssignmentIdCallback = (uuid, key, result, details) -> {
      try {
        AssetTypeAttributeTypeAssignment assignment = assetTypeAttributeTypesAssignmentsDAO.findAssetTypeAttributeTypeAssignmentById(uuid, false);
        UUID assetTypeId = assignment.getAssetType().getAssetTypeId();

        requestValues.putValue(requestValues.ASSET_TYPE_KEY, assetTypeId);

        details.setConnectedValue(RoleActionEntityName.ASSET_TYPE, assetTypeId.toString());
      } catch (AssetTypeAttributeTypeAssignmentNotFoundException ignored) {
      }
    };

    PathVariableCallback assetTypeStatusAssignmentIdCallback = (uuid, key, result, details) -> {
      try {
        AssetTypeStatusAssignment assignment = assetTypeStatusesAssignmentsDAO.findAssetTypeStatusAssignmentsById(uuid);
        UUID assetTypeId = assignment.getAssetType().getAssetTypeId();

        requestValues.putValue(requestValues.ASSET_TYPE_KEY, assetTypeId);

        details.setConnectedValue(RoleActionEntityName.ASSET_TYPE, assetTypeId.toString());
      } catch (AssetTypeStatusAssignmentNotFoundException ignored) {}
    };

    PathVariableCallback relationTypeAttributeTypeAssignmentIdCallback = (uuid, key, result, details) -> {
      try {
        RelationTypeAttributeTypeAssignment assignment = relationTypeAttributeTypesAssignmentsDAO.findRelationTypeAttributeTypeAssignmentById(uuid, false);
        UUID relationTypeId = assignment.getRelationType().getRelationTypeId();

        requestValues.putValue(requestValues.RELATION_TYPE_KEY, relationTypeId);

        details.setConnectedValue(RoleActionEntityName.RELATION_TYPE, relationTypeId.toString());
      } catch (RelationTypeAttributeTypeAssignmentNotFound ignored) {}
    };

    PathVariableCallback relationTypeComponentIdCallback = (uuid, key, result, details) -> {
      try {
        RelationTypeComponent relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentById(uuid, false);
        UUID relationTypeId = relationTypeComponent.getRelationType().getRelationTypeId();

        requestValues.putValue(requestValues.RELATION_TYPE_KEY, relationTypeId);

        details.setConnectedValue(RoleActionEntityName.RELATION_TYPE, relationTypeId.toString());
      } catch (RelationTypeComponentNotFoundException ignored) {}
    };

    PathVariableCallback attributeTypeAllowedValueIdCallback = (uuid, key, result, details) -> {
      try {
        AttributeTypeAllowedValue attributeTypeAllowedValue = attributeTypesAllowedValuesDAO.findAttributeTypeAllowedValueById(uuid);
        UUID attributeTypeId = attributeTypeAllowedValue.getAttributeType().getAttributeTypeId();

        requestValues.putValue(requestValues.ATTRIBUTE_TYPE_KEY, attributeTypeId);

        details.setConnectedValue(RoleActionEntityName.ATTRIBUTE_TYPE, attributeTypeId.toString());
      } catch (AttributeTypeAllowedValueNotFoundException ignored) {}
    };

    PathVariableCallback relationTypeComponentAssetTypeAssignmentCallback = (uuid, key, result, details) -> {
      try {
        RelationTypeComponentWithRelationType relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentByRelationTypeComponentAssetTypeAssignmentId(uuid);
        UUID relationTypeId = relationTypeComponent.getRelationTypeId();

        requestValues.putValue(requestValues.RELATION_TYPE_KEY, relationTypeId);

        details.setConnectedValue(RoleActionEntityName.RELATION_TYPE, relationTypeId.toString());
      } catch (RelationTypeAttributeTypeAssignmentNotFound ignored) {}
    };

    PathVariableCallback relationTypeComponentAttributeTypeAssignmentCallback = (uuid, key, result, details) -> {
      try {
        RelationTypeComponentWithRelationType relationTypeComponent = relationTypeComponentsDAO.findRelationTypeComponentByRelationTypeComponentAttributeTypeAssignmentId(uuid);
        UUID relationTypeId = relationTypeComponent.getRelationTypeId();

        requestValues.putValue(requestValues.RELATION_TYPE_KEY, relationTypeId);

        details.setConnectedValue(RoleActionEntityName.RELATION_TYPE, relationTypeId.toString());
      } catch (RelationTypeComponentAttributeTypeAssignmentNotFound ignored) {}
    };

    PathVariableCallback assetCallback = (uuid, key, result, details) -> {
      try {
        Asset asset = assetsDAO.findAssetById(uuid);
        UUID assetTypeId = asset.getAssetType().getAssetTypeId();

        requestValues.putValue(requestValues.ASSET_ID_KEY, asset.getAssetId());
        requestValues.putValue(requestValues.ASSET_TYPE_KEY, assetTypeId);

        details.setConnectedValue(RoleActionEntityName.ASSET_TYPE, assetTypeId.toString());
      } catch (AssetNotFoundException ignored) {}
    };

    PathVariableCallback attributeCallback = (uuid, key, result, details) -> {
      try {
        Attribute attribute = attributesDAO.findAttributeById(uuid);
        UUID assetId = attribute.getAsset().getAssetId();
        UUID attributeTypeId = attribute.getAttributeType().getAttributeTypeId();

        requestValues.putValue(requestValues.ASSET_ID_KEY, assetId);
        requestValues.putValue(requestValues.ATTRIBUTE_TYPE_KEY, attributeTypeId);

        details.setConnectedValue(RoleActionEntityName.ATTRIBUTE_TYPE, attributeTypeId.toString());
      } catch (AttributeNotFoundException ignored) {}
    };

    PathVariableCallback responsibilityCallback = (uuid, key, result, details) -> {
      try {
        Responsibility responsibility = responsibilitiesDAO.findResponsibilityById(uuid, false);
        UUID assetId = responsibility.getAsset().getAssetId();
        UUID roleId = responsibility.getRole().getRoleId();

        requestValues.putValue(requestValues.ASSET_ID_KEY, assetId);
        requestValues.putValue(requestValues.ROLE_KEY, roleId);

        details.setConnectedValue(RoleActionEntityName.ROLE, roleId.toString());
      } catch (ResponsibilityNotFoundException ignored) {}
    };

    PathVariableCallback roleActionsCallback = (uuid, key, result, details) -> {

      try {
        RoleAction roleActionById = roleActionsDAO.findRoleActionById(uuid);
        UUID roleId = roleActionById.getRole().getRoleId();

        requestValues.putValue(requestValues.ROLE_KEY, roleId);

        details.setConnectedValue(RoleActionEntityName.ROLE, roleId.toString());
      } catch (RoleActionNotFoundException ignored) {}
    };

    PathVariableCallback globalResponsibilityCallback = (uuid, key, result, details) -> {
      try {
        GlobalResponsibility globalResponsibility = globalResponsibilitiesDAO.findGlobalResponsibilityById(uuid);
        UUID roleId = globalResponsibility.getRole().getRoleId();

        requestValues.putValue(requestValues.ROLE_KEY, roleId);

        details.setConnectedValue(RoleActionEntityName.ROLE, roleId.toString());
      } catch (GlobalResponsibilityNotFoundException ignored) {}
    };

    PathVariableCallback relationsCallback = (uuid, key, result, details) -> {
      try {
        Relation relation = relationsDAO.findRelationById(uuid);
        UUID relationTypeId = relation.getRelationType().getRelationTypeId();

        requestValues.putValue(requestValues.RELATION_TYPE_KEY, relationTypeId);

        details.setConnectedValue(RoleActionEntityName.RELATION_TYPE, relationTypeId.toString());
      } catch (RelationNotFoundException ignored) {}
    };

    PathVariableCallback modifyRelationsCallback = (uuid, key, result, details) -> {
      try {
        RelationConnectedValues connectedValues = relationsDAO.findAllRelationAssetIds(uuid);
        UUID relationTypeId = connectedValues.getRelationTypeId();

        requestValues.putValue(requestValues.RELATION_TYPE_KEY, relationTypeId);
        requestValues.addAllAssetIds(connectedValues.getAssetIds());

        details.setConnectedValue(RoleActionEntityName.RELATION_TYPE, relationTypeId.toString());
      } catch (GlobalResponsibilityNotFoundException ignored) {}
    };

    PathVariableCallback subscriptionCallback = (uuid, key, result, details) -> {
      try {
        Subscription subscription = subscriptionsDAO.findSubscriptionById(uuid);
        UUID ownerUserId = subscription.getOwnerUser().getUserId();

        details.setConnectedValue(RoleActionEntityName.OWNER_ID, ownerUserId.toString());
      } catch (SubscriptionNotFoundException ignored) {}
    };

    PathVariableCallback relationAttributeCallback = (uuid, key, result, details) -> {
      try {
        RelationAttribute relationAttribute = relationAttributesDAO.findRelationAttributeById(uuid, false);
        UUID attributeTypeId = relationAttribute.getAttributeType().getAttributeTypeId();

        requestValues.putValue(requestValues.ATTRIBUTE_TYPE_KEY, attributeTypeId);

        details.setConnectedValue(RoleActionEntityName.ATTRIBUTE_TYPE, attributeTypeId.toString());
      } catch (RelationAttributeNotFoundException ignored) {}
    };

    PathVariableCallback relationComponentAttributeCallback = (uuid, key, result, details) -> {
      try {
        RelationComponentAttribute relationComponentAttribute = relationComponentAttributesDAO.findRelationComponentAttributeById(uuid, false);
        UUID attributeTypeId = relationComponentAttribute.getAttributeType().getAttributeTypeId();

        requestValues.putValue(requestValues.ATTRIBUTE_TYPE_KEY, attributeTypeId);

        details.setConnectedValue(RoleActionEntityName.ATTRIBUTE_TYPE, attributeTypeId.toString());
      } catch (RelationComponentAttributeNotFoundException ignored) {}
    };

    PathVariableCallback assetTypeCardHeaderAssignmentIdCallback = (uuid, key, result, details) -> {
      try {
        AssetTypeCardHeaderAssignment assignment = assetTypeCardHeaderAssignmentDAO.findAssetTypeCardHeaderAssignmentById(uuid);
        UUID assetTypeId = assignment.getAssetType().getAssetTypeId();

        requestValues.putValue(requestValues.ASSET_TYPE_KEY, assetTypeId);

        details.setConnectedValue(RoleActionEntityName.ASSET_TYPE, assetTypeId.toString());
      } catch (AssetTypeCardHeaderAssignmentNotFoundException ignored) {}
    };

    parsePathVariables(List.of(
      new PathVariable("assetId", "/v1/assets/{assetId}", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}", request, "PATCH", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}", request, "DELETE", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/path", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/header", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/links", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/children", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/changeHistory", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/relationTypes", request, "GET", null, assetCallback),

      new PathVariable("assetId", "/v1/assets/{assetId}/customViews/{customViewId}/tableRows", request, "GET", null, assetCallback),
      new PathVariable("assetId", "/v1/assets/{assetId}/customViews/{customViewId}/headerRows", request, "GET", null, assetCallback),

      new PathVariable("attributeId", "/v1/attributes/{attributeId}", request, "GET", null, attributeCallback),
      new PathVariable("attributeId", "/v1/attributes/{attributeId}", request, "PATCH", null, attributeCallback),
      new PathVariable("attributeId", "/v1/attributes/{attributeId}", request, "DELETE", null, attributeCallback),

      new PathVariable("relationAttributeId", "/v1/relationAttributes/{relationAttributeId}", request, "GET", null, relationAttributeCallback),
      new PathVariable("relationAttributeId", "/v1/relationAttributes/{relationAttributeId}", request, "PATCH", null, relationAttributeCallback),
      new PathVariable("relationAttributeId", "/v1/relationAttributes/{relationAttributeId}", request, "DELETE", null, relationAttributeCallback),

      new PathVariable("relationComponentAttributeId", "/v1/relationComponentAttributes/{relationComponentAttributeId}", request, "GET", null, relationComponentAttributeCallback),
      new PathVariable("relationComponentAttributeId", "/v1/relationComponentAttributes/{relationComponentAttributeId}", request, "PATCH", null, relationComponentAttributeCallback),
      new PathVariable("relationComponentAttributeId", "/v1/relationComponentAttributes/{relationComponentAttributeId}", request, "DELETE", null, relationComponentAttributeCallback),

	    new PathVariable("subscriptionId", "/v1/subscriptions/{subscriptionId}", request, "PATCH", null, subscriptionCallback),
      new PathVariable("subscriptionId", "/v1/subscriptions/{subscriptionId}", request, "DELETE", null, subscriptionCallback),
      
      new PathVariable("relationId", "/v1/relations/{relationId}", request, "GET", null, relationsCallback),
      new PathVariable("relationId", "/v1/relations/{relationId}", request, "DELETE", null, modifyRelationsCallback),
      new PathVariable("relationId", "/v1/relations/{relationId}/attributes", request, "GET", null, relationsCallback),

      new PathVariable("roleActionId", "/v1/roleActions/{roleActionId}", request, "DELETE", null, roleActionsCallback),

      new PathVariable("responsibilityId", "/v1/responsibilities/{responsibilityId}", request, "GET", null, responsibilityCallback),
      new PathVariable("responsibilityId", "/v1/responsibilities/{responsibilityId}", request, "DELETE", null, responsibilityCallback),

      new PathVariable("globalResponsibilityId", "/v1/globalResponsibilities/{globalResponsibilityId}", request, "GET", null, globalResponsibilityCallback),
      new PathVariable("globalResponsibilityId", "/v1/globalResponsibilities/{globalResponsibilityId}", request, "DELETE", null, globalResponsibilityCallback),

      new PathVariable("relationTypeComponentId", "/v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch", request, "POST", null, relationTypeComponentIdCallback),
      new PathVariable("relationTypeComponentId", "/v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes", request, "GET", null, relationTypeComponentIdCallback),
      new PathVariable("relationTypeComponentAttributeTypeAssignment", "/v1/assignments/relationTypeComponent/attributeType/{relationTypeComponentAttributeTypeAssignment}", request, "DELETE", null, relationTypeComponentAttributeTypeAssignmentCallback),

      new PathVariable("relationTypeComponentId", "/v1/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch", request, "POST", null, relationTypeComponentIdCallback),
      new PathVariable("relationTypeComponentId", "/v1/assignments/relationTypeComponent/{relationTypeComponentId}/assetType", request, "GET", null, relationTypeComponentIdCallback),
      new PathVariable("relationTypeComponentAssetTypeAssignment", "/v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}", request, "DELETE", null, relationTypeComponentAssetTypeAssignmentCallback),

      new PathVariable("assetTypeStatusAssignmentId", "/v1/assignments/assetType/status/{assetTypeStatusAssignmentId}", request, "DELETE", null, assetTypeStatusAssignmentIdCallback),
      new PathVariable("assetTypeCardHeaderAssignmentId", "/v1/assignments/assetType/assetCardHeader/{assetTypeCardHeaderAssignmentId}", request, "DELETE", null, assetTypeCardHeaderAssignmentIdCallback),
      new PathVariable("assetTypeAttributeTypeAssignmentId", "/v1/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId}", request, "DELETE", null, assetTypeAttributeTypeAssignmentIdCallback),
      new PathVariable("relationTypeAttributeTypeAssignmentId", "/v1/assignments/relationType/attributeType/{relationTypeAttributeTypeAssignmentId}", request, "DELETE", null, relationTypeAttributeTypeAssignmentIdCallback),

      new PathVariable("attributeTypeAllowedValueId", "/v1/attributeTypes/allowedValues/{attributeTypeAllowedValueId}", request, "DELETE", null, attributeTypeAllowedValueIdCallback)

    ), requestValues, userDetails);
  }

  private void parsePathVariables (List<PathVariable> pathVariables, RequestValues requestValues, AuthUserDetails userDetails) {
    pathVariables.forEach(pathVariable -> {
      if (!isPatternRequest(pathVariable.method(), pathVariable.pathPattern(), pathVariable.request())) return;

      Map<String, String> map = pathMatcher.extractUriTemplateVariables(pathVariable.pathPattern(), pathVariable.request().getServletPath());

      if (!map.containsKey(pathVariable.pathVariable())) return;

      if (!UUIDUtils.isValidUUID(map.get(pathVariable.pathVariable()))) return;

      UUID uuid = UUID.fromString(map.get(pathVariable.pathVariable()));
      if (pathVariable.callback() == null) {
        requestValues.putValue(pathVariable.pathVariableKey(), uuid);
        return;
      }

      pathVariable.callback().loadData(uuid, pathVariable.pathVariableKey(), requestValues, userDetails);
    });
  }

  private boolean isSearchEntry (String key, Object value, RequestValues requestValues) {
    boolean isKeySearchField = requestValues.SEARCH_FIELDS.contains(key);

    if (!isKeySearchField) return false;

    String keyValue = (String) value;
    if (StringUtils.isEmpty(keyValue)) {
      return false;
    }

    return UUIDUtils.isValidUUID(keyValue);
  }

  private boolean isPatternRequest (String patternMethod, String pathPattern, HttpServletRequest request) {
    if (patternMethod != null) {
      if (!request.getMethod().equals(patternMethod)) return false;
    }

    if (!isPatternMatch(pathPattern, request.getServletPath())) return false;

    return true;
  }

  private void loadUserRoleActions (
    Authentication authentication,
    List<ActionTypeName> actionTypeNames,
    List<EntityNameType> entityTypeNames,
    RequestValues requestValues
  ) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
    User user = userDetails.getUser();

    List<RoleActionResponse> roleActions = roleActionsDAO.findAllByUserIdActionTypesAndEntityName(
      true,
      user.getUserId(),
      actionTypeNames,
      entityTypeNames,
      requestValues.getRoleIds(),
      requestValues.getAssetTypeIds(),
      requestValues.getAttributeTypeIds(),
      requestValues.getRelationTypeIds()
    );

    userDetails.setUserRoleActions(roleActions);

    if (roleActions.isEmpty() || containsDenied(roleActions, requestValues)) {
      List<RoleActionResponse> groupRoleAction = roleActionsDAO.findAllByUserIdActionTypesAndEntityName(
        false,
        user.getUserId(),
        actionTypeNames,
        entityTypeNames,
        requestValues.getRoleIds(),
        requestValues.getAssetTypeIds(),
        requestValues.getAttributeTypeIds(),
        requestValues.getRelationTypeIds()
      );

      userDetails.setGroupRoleActions(groupRoleAction);
    }
  }

  private void loadAssetRoleActions (
    Authentication authentication,
    List<ActionTypeName> actionTypeNames,
    EntityNameType entityNameType,
    RequestValues requestValues
  ) {
    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
    User user = userDetails.getUser();

    List<RoleActionResponse> roleActions = roleActionsDAO.findAllByUserIdAndAssetIdResponsibilities(
      true,
      user.getUserId(),
      requestValues.getAssetIds(),
      actionTypeNames,
      entityNameType,
      requestValues.getRoleIds(),
      requestValues.getAssetTypeIds(),
      requestValues.getAttributeTypeIds(),
      requestValues.getRelationTypeIds()
    );

    userDetails.setAssetUserRoleActions(roleActions);

    if (roleActions.isEmpty() || containsDenied(roleActions, requestValues)) {
      List<RoleActionResponse> groupRoleActions = roleActionsDAO.findAllByUserIdAndAssetIdResponsibilities(
        false,
        user.getUserId(),
        requestValues.getAssetIds(),
        actionTypeNames,
        entityNameType,
        requestValues.getRoleIds(),
        requestValues.getAssetTypeIds(),
        requestValues.getAttributeTypeIds(),
        requestValues.getRelationTypeIds()
      );

      userDetails.setAssetGroupRoleActions(groupRoleActions);
    }
  }

  private boolean isPatternMatch (String pattern, String path) {
    return pathMatcher.match(pattern, path);
  }

  private boolean containsDenied (List<RoleActionResponse> roleActions, RequestValues requestValues) {
    return roleActions.stream().anyMatch(roleActionResponse ->
      roleActionResponse.getPermissionType().equals(PermissionType.DENY) && hasValuesFromRequestMap(roleActionResponse, requestValues)
    );
  }

  private boolean hasValuesFromRequestMap (RoleActionResponse roleAction, RequestValues requestValues) {
    boolean hasValue = false;

    if (!requestValues.getRoleIds().isEmpty()) {
      hasValue = roleAction.getRoleId() != null;
    }

    if (!requestValues.getAssetTypeIds().isEmpty()) {
      hasValue = roleAction.getAssetTypeId() != null;
    }

    if (!requestValues.getAttributeTypeIds().isEmpty()) {
      hasValue = roleAction.getAttributeTypeId() != null;
    }

    if (!requestValues.getRelationTypeIds().isEmpty()) {
      hasValue = roleAction.getRelationTypeId() != null;
    }

    return hasValue;
  }

  @Override
  protected boolean shouldNotFilter (HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/v1");
  }
}
