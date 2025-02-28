package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;

/**
 * @author juliwolf
 */

@Service
public class ResponsibilitiesBulkService {
  private final ResponsibilityRepository responsibilityRepository;

  public ResponsibilitiesBulkService (ResponsibilityRepository responsibilityRepository) {
    this.responsibilityRepository = responsibilityRepository;
  }

  @Transactional
  public List<PostResponsibilityResponse> createResponsibilitiesBulk (
    List<PostResponsibilityRequest> responsibilitiesRequest,
    Map<UUID, Asset> assetsMap,
    Map<UUID, User> usersMap,
    Map<UUID, Role> rolesMap,
    Map<UUID, Group> groupsMap,
    User user
  ) {
    return responsibilitiesRequest.stream().map(request -> {
      Asset asset = assetsMap.get(request.getAsset_id());
      Role role = rolesMap.get(request.getRole_id());

      ResponsibleType responsibleType = ResponsibleType.valueOf(request.getResponsible_type());

      User responsibleUser = responsibleType.equals(ResponsibleType.USER)
        ? usersMap.get(request.getResponsible_id())
        : null;

      Group responsibleGroup = responsibleType.equals(ResponsibleType.GROUP)
        ? groupsMap.get(request.getResponsible_id())
        : null;

      Responsibility responsibility = responsibilityRepository.save(new Responsibility(
        responsibleUser,
        responsibleGroup,
        asset,
        role,
        responsibleType,
        user
      ));

      return new PostResponsibilityResponse(
        responsibility.getResponsibilityId(),
        asset.getAssetId(),
        role.getRoleId(),
        responsibleType,
        responsibleType.equals(ResponsibleType.USER) ? responsibleUser.getUserId() : responsibleGroup.getGroupId(),
        responsibility.getCreatedOn(),
        user.getUserId()
      );
    }).toList();
  }
}
