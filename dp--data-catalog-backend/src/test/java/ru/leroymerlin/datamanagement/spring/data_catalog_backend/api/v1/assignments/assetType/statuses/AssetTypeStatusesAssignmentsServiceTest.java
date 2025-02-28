package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.AssetTypeStatusesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypeInheritance.AssetTypeInheritanceRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.AssetTypeStatusAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.StatusTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class AssetTypeStatusesAssignmentsServiceTest extends ServiceWithUserIntegrationTest {

  @Autowired
  private AssetTypeStatusesAssignmentsService assetTypeStatusesAssignmentsService;

  @Autowired
  private AssetTypeStatusAssignmentRepository assetTypeStatusAssignmentRepository;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private StatusRepository statusRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AssetTypeInheritanceRepository assetTypeInheritanceRepository;

  private Status status;
  private AssetType assetType;

  @BeforeAll
  public void prepareAssetTypesAndStatuses () {
    status = statusRepository.save(new Status("status name", "status description", language, user));

    assetType = assetTypeRepository.save(new AssetType("asset type name", "description", "atn", "red", language, user));
  }

  @AfterAll
  public void clearAssetTypesAndStatuses () {
    assetTypeInheritanceRepository.deleteAll();
    statusRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearData () {
    assetRepository.deleteAll();
    assetTypeStatusAssignmentRepository.deleteAll();
  }

  @Test
  public void createAssetTypeAssignmentStatusesSuccessIntegrationTest () {
    try {
      List<PostAssetTypeStatusAssignmentRequest> statusesList = new ArrayList<>();
      statusesList.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

      PostAssetTypeStatusesAssignmentsRequest request = new PostAssetTypeStatusesAssignmentsRequest(statusesList);
      PostAssetTypeStatusesAssignmentsResponse assignments = assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(assetType.getAssetTypeId(), request, user);

      assertAll(
        () -> assertEquals(1, assignments.getAsset_status_assignment().size()),
        () -> assertEquals(status.getStatusId(), assignments.getAsset_status_assignment().get(0).getStatus_id())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeAssignmentStatusesStatusNotFoundIntegrationTest () {
    try {
      List<PostAssetTypeStatusAssignmentRequest> statusesList = new ArrayList<>();
      statusesList.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, new UUID(123, 123)));

      PostAssetTypeStatusesAssignmentsRequest request = new PostAssetTypeStatusesAssignmentsRequest(statusesList);

      assertThrows(StatusNotFoundException.class, () -> assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(assetType.getAssetTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeAssignmentStatusesAssetTypeNotFoundIntegrationTest () {
    try {
      List<PostAssetTypeStatusAssignmentRequest> statusesList = new ArrayList<>();
      statusesList.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, new UUID(123, 123)));

      PostAssetTypeStatusesAssignmentsRequest request = new PostAssetTypeStatusesAssignmentsRequest(statusesList);

      assertThrows(AssetTypeNotFoundException.class, () -> assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(new UUID(123, 123), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeAssignmentStatusesAssignmentAlreadyExistsIntegrationTest () {
    try {
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));
      List<PostAssetTypeStatusAssignmentRequest> statusesList = new ArrayList<>();
      statusesList.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

      PostAssetTypeStatusesAssignmentsRequest request = new PostAssetTypeStatusesAssignmentsRequest(statusesList);

      assertThrows(DataIntegrityViolationException.class, () -> assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(assetType.getAssetTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeAssignmentStatusesAssignmentCreateAssignmentsForChildrenInTreeIntegrationTest () {
    try {
      List<PostAssetTypeStatusAssignmentRequest> statusesList = new ArrayList<>();
      statusesList.add(new PostAssetTypeStatusAssignmentRequest(AssignmentStatusType.LIFECYCLE, status.getStatusId()));

      PostAssetTypeStatusesAssignmentsRequest request = new PostAssetTypeStatusesAssignmentsRequest(statusesList);

      AssetType BAssetType = assetTypeRepository.save(new AssetType("child asset type name", "description", "atn", "red", language, user));
      assetTypeInheritanceRepository.save(new AssetTypeInheritance(assetType, BAssetType, user));

      AssetType CAssetType = assetTypeRepository.save(new AssetType("C asset type name", "description", "atn", "red", language, user));
      assetTypeInheritanceRepository.save(new AssetTypeInheritance(BAssetType, CAssetType, user));

      assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(assetType.getAssetTypeId(), request, user);

      List<AssetTypeStatusAssignment> assignments = assetTypeStatusAssignmentRepository.findAll();
      List<AssetTypeStatusAssignment> inheritedAssignments = assignments.stream().filter(AssetTypeStatusAssignment::getIsInherited).toList();

      assertAll(
        () -> assertNotEquals(0, inheritedAssignments.size()),
        () -> assertEquals(assetType.getAssetTypeId(), inheritedAssignments.get(0).getParentAssetType().getAssetTypeId())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAssetTypeStatusesAssignmentsByParamsSuccessIntegrationTest () {
    try {
      AssetType firstAssetType = assetTypeRepository.save(new AssetType("first asset type name", "just normal", "atn", "red", language, user));
      AssetType secondAssetType = assetTypeRepository.save(new AssetType("with exotic name", "same desc", "atn", "red", language, user));
      AssetType thrirdAssetType = assetTypeRepository.save(new AssetType("bla bla", "random words", "atn", "red", language, user));

      Status firstStatus = statusRepository.save(new Status("status name default", "status description", language, user));
      Status secondStatus = statusRepository.save(new Status("some new ", "status description", language, user));
      Status thirdStatus = statusRepository.save(new Status("Hello there", "status description", language, user));

      Asset firstAssetTypeFirstStatus = assetRepository.save(new Asset("first asset type first status", firstAssetType, "this", language, null, firstStatus, user));
      assetRepository.save(new Asset("copy first asset type first status", firstAssetType, "this", language, null, firstStatus, user));
      Asset firstAssetTypeThirdStatus = assetRepository.save(new Asset("first asset type third status", firstAssetType, "this", language, thirdStatus, null, user));
      assetRepository.save(new Asset("copy first asset type third status", firstAssetType, "this", language, thirdStatus, null, user));

      Asset secondAssetTypeFirstStatus = assetRepository.save(new Asset("second asset type first status", secondAssetType, "this", language, firstStatus, null, user));
      Asset secondAssetTypeSecondStatus = assetRepository.save(new Asset("second asset type second status", secondAssetType, "this", language, null, secondStatus, user));
      Asset secondAssetTypeThirdStatus = assetRepository.save(new Asset("second asset type third status", secondAssetType, "this", language, null, thirdStatus, user));

      Asset thirdAssetTypeFirstStatus = assetRepository.save(new Asset("third asset type first status", thrirdAssetType, "this", language, null, firstStatus, user));
      Asset thirdAssetTypeThirdStatus = assetRepository.save(new Asset("third asset type third status", thrirdAssetType, "this", language, thirdStatus, null, user));

      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.STEWARDSHIP, status, user));

      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(firstAssetType, AssignmentStatusType.STEWARDSHIP, firstStatus, user));
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(firstAssetType, AssignmentStatusType.LIFECYCLE, thirdStatus, user));

      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(secondAssetType, AssignmentStatusType.LIFECYCLE, firstStatus, user));
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(secondAssetType, AssignmentStatusType.STEWARDSHIP, secondStatus, user));
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(secondAssetType, AssignmentStatusType.STEWARDSHIP, thirdStatus, user));

      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(thrirdAssetType, AssignmentStatusType.STEWARDSHIP, firstStatus, user));
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(thrirdAssetType, AssignmentStatusType.LIFECYCLE, thirdStatus, user));

      assertAll(
        () -> assertEquals(3, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(firstStatus.getStatusId(),  null, null, 0, 50).getResults().size()),
        () -> assertEquals(9, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  null, null, 0, 50).getResults().size()),
        () -> assertEquals(4, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  AssignmentStatusType.LIFECYCLE, null, 0, 50).getResults().size()),
        () -> assertEquals(5, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  AssignmentStatusType.STEWARDSHIP, null, 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  AssignmentStatusType.STEWARDSHIP, thrirdAssetType.getAssetTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  AssignmentStatusType.STEWARDSHIP, thrirdAssetType.getAssetTypeId(), 0, 50).getResults().get(0).getAsset_type_status_usage_count(), "stewardship status third asset type - status count"),
        () -> assertEquals(2, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  null, thrirdAssetType.getAssetTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  AssignmentStatusType.LIFECYCLE, thrirdAssetType.getAssetTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  AssignmentStatusType.LIFECYCLE, thrirdAssetType.getAssetTypeId(), 0, 50).getResults().get(0).getAsset_type_status_usage_count(), "lifecycle status third asset type - status count"),
        () -> assertEquals(3, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null,  null, secondAssetType.getAssetTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(secondStatus.getStatusId(),  null, secondAssetType.getAssetTypeId(), 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(secondStatus.getStatusId(),  null, secondAssetType.getAssetTypeId(), 0, 50).getResults().get(0).getAsset_type_status_usage_count(), "second status second asset type - status count"),
        () -> assertEquals(0, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(secondStatus.getStatusId(),  null, firstAssetType.getAssetTypeId(), 0, 50).getResults().size())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAssetTypeStatusesAssignmentsByParamsPaginationIntegrationTest () {
    generateAssignments(130);

    assertAll(
      () -> assertEquals(100, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null, null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null, null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(null, null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void getAssetTypeStatusesAssignmentsByParamsAssetTypeNotFoundIntegrationTest () {
    try {
      assertThrows(AssetTypeNotFoundException.class, () -> assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(new UUID(123, 123), null));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAssetTypeStatusesByParamsSuccessIntegrationTest () {
    try {
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));
      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.STEWARDSHIP, status, user));

      GetAssetTypeStatusesAssignmentsResponse allResults = assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(assetType.getAssetTypeId(), null);
      GetAssetTypeStatusesAssignmentsResponse lifecycleResult = assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(assetType.getAssetTypeId(), AssignmentStatusType.LIFECYCLE);
      GetAssetTypeStatusesAssignmentsResponse stewardshipResult = assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(assetType.getAssetTypeId(), AssignmentStatusType.STEWARDSHIP);

      assertAll(
        () -> assertEquals(2, allResults.getAsset_status_assignment().size()),
        () -> assertEquals(1, lifecycleResult.getAsset_status_assignment().size()),
        () -> assertEquals(1, stewardshipResult.getAsset_status_assignment().size())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAssetTypeStatusesAssignmentByIdAssetTypeStatusAssignmentNotFoundIntegrationTest () {
    try {
      assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetTypeStatusesAssignmentsService.deleteAssetTypeStatusAssignmentById(new UUID(123, 123), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAssetTypeStatusesAssignmentByIdAssetTypeStatusAssignmentAlreadyDeletedIntegrationTest () {
    try {
      AssetTypeStatusAssignment assignment = new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user);
      assignment.setIsDeleted(true);
      assetTypeStatusAssignmentRepository.save(assignment);

      assertThrows(AssetTypeStatusAssignmentNotFoundException.class, () -> assetTypeStatusesAssignmentsService.deleteAssetTypeStatusAssignmentById(assignment.getAssetTypeStatusAssignmentId(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAssetTypeStatusesAssignmentByIdAssignmentStatusIsUsedForAssetIntegrationTest () {
    try {
      AssetTypeStatusAssignment assignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));
      Asset asset = assetRepository.save(new Asset("asset name", assetType, "this", language, status, null, user));

      assertThrows(StatusTypeIsUsedForAssetException.class, () -> assetTypeStatusesAssignmentsService.deleteAssetTypeStatusAssignmentById(assignment.getAssetTypeStatusAssignmentId(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAssetTypeStatusesAssignmentByIdAssignmentStatusIsInheritedIntegrationTest () {
    try {
      AssetTypeStatusAssignment assignment = new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user);
      assignment.setIsInherited(true);
      assetTypeStatusAssignmentRepository.save(assignment);

      assertThrows(AssetTypeStatusAssignmentIsInheritedException.class, () -> assetTypeStatusesAssignmentsService.deleteAssetTypeStatusAssignmentById(assignment.getAssetTypeStatusAssignmentId(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void deleteAssetTypeStatusesAssignmentByIdChildAssignmentStatusIsUsedForAssetIntegrationTest () {
    try {
      AssetTypeStatusAssignment parentAssignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.LIFECYCLE, status, user));

      AssetType childAssetType = assetTypeRepository.save(new AssetType("child asset type", "desc", "acr", "red", language, user));
      Asset asset = assetRepository.save(new Asset("test asset name", childAssetType, "this", language, status, null, user));

      AssetTypeStatusAssignment childAssignment = new AssetTypeStatusAssignment(childAssetType, AssignmentStatusType.LIFECYCLE, status, user);
      childAssignment.setIsInherited(true);
      childAssignment.setParentAssetType(assetType);
      assetTypeStatusAssignmentRepository.save(childAssignment);

      assertThrows(StatusTypeIsUsedForAssetException.class, () -> assetTypeStatusesAssignmentsService.deleteAssetTypeStatusAssignmentById(parentAssignment.getAssetTypeStatusAssignmentId(), user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  private void generateAssignments (int count) {
    for (int i = 0; i < count; i++) {
      AssetType firstAssetType = assetTypeRepository.save(new AssetType("asset_type_" + i, "just normal", "atn", "red", language, user));

      Status firstStatus = statusRepository.save(new Status("status_"+i, "status description", language, user));

      assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(firstAssetType, AssignmentStatusType.LIFECYCLE, firstStatus, user));
    }
  }
}
