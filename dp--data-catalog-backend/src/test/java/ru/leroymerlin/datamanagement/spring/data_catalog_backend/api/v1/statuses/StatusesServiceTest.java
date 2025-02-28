package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.StatusesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Status;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.StatusResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.get.GetStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PatchStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class StatusesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private StatusesService statusesService;

  @Autowired
  private StatusRepository statusRepository;

  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;

  PostStatusRequest request = new PostStatusRequest("new status", "new description");

  @AfterEach
  public void clearTables() {
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    statusRepository.deleteAll();
  }

  @Test
  public void createStatusSuccessIntegrationTest () {
    try {
      PostStatusResponse response = statusesService.createStatus(request, user);

      assertAll(
        () -> assertEquals(response.getCreated_by(), user.getUserId()),
        () -> assertEquals(response.getStatus_name(), request.getStatus_name())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createStatusWithExistingStatusNameIntegrationTest () {
    try {
      statusRepository.save(new Status(request.getStatus_name(), request.getStatus_description(), language, user));

      assertThrows(DataIntegrityViolationException.class, () ->
        statusesService.createStatus(request, user)
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateStatusSuccessTest () {
    try {
      Status status = statusRepository.save(new Status("some status name", "some description", language, user));
      PatchStatusRequest request = new PatchStatusRequest(Optional.of("new status"), Optional.of("new description"));

      StatusResponse updatedStatus = statusesService.updateStatus(status.getStatusId(), request, user);

      assertAll(
        () -> assertEquals(request.getStatus_name().get(), updatedStatus.getStatus_name()),
        () -> assertNotNull(updatedStatus.getLast_modified_by()),
        () -> assertEquals(user.getUserId(), updatedStatus.getLast_modified_by())
      );

      PatchStatusRequest request1 = new PatchStatusRequest(Optional.of("another status"), null);
      StatusResponse updatedStatus1 = statusesService.updateStatus(status.getStatusId(), request1, user);

      assertAll(
        () -> assertEquals(request1.getStatus_name().get(), updatedStatus1.getStatus_name()),
        () -> assertEquals(updatedStatus.getStatus_description(), updatedStatus1.getStatus_description())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateStatusStatusNotFoundIntegrationTest () {
    try {
      PatchStatusRequest request = new PatchStatusRequest(Optional.of("new status"), Optional.of("new description"));

      assertThrows(StatusNotFoundException.class, () ->
        statusesService.updateStatus(new UUID(123, 123), request, user)
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateStatusWithExistingStatusNameIntegrationTest () {
    try {
      PatchStatusRequest request = new PatchStatusRequest(Optional.of("new status"), Optional.of("new description"));

      statusRepository.save(new Status(request.getStatus_name().get(), request.getStatus_description().get(), language, user));
      Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

      assertThrows(DataIntegrityViolationException.class, () ->
        statusesService.updateStatus(status.getStatusId(), request, user)
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateStatusClearStatusNameIntegrationTest () {
    PatchStatusRequest request = new PatchStatusRequest(Optional.empty(), Optional.of("new description"));
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    StatusResponse statusResponse = statusesService.updateStatus(status.getStatusId(), request, user);

    assertEquals(status.getStatusName(), statusResponse.getStatus_name());
  }

  @Test
  public void updateStatusChangeStatusNameIntegrationTest () {
    PatchStatusRequest request = new PatchStatusRequest(Optional.of("some new name"), Optional.of("new description"));
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    StatusResponse statusResponse = statusesService.updateStatus(status.getStatusId(), request, user);

    assertEquals("some new name", statusResponse.getStatus_name());
  }

  @Test
  public void updateStatusDoNothingWithStatusNameIntegrationTest () {
    PatchStatusRequest request = new PatchStatusRequest(null, Optional.of("new description"));
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    StatusResponse statusResponse = statusesService.updateStatus(status.getStatusId(), request, user);

    assertEquals(status.getStatusName(), statusResponse.getStatus_name());
  }

  @Test
  public void updateStatusClearDescriptionIntegrationTest () {
    PatchStatusRequest request = new PatchStatusRequest(Optional.of("some new name"), Optional.empty());
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    StatusResponse statusResponse = statusesService.updateStatus(status.getStatusId(), request, user);

    assertNull(statusResponse.getStatus_description());
  }

  @Test
  public void updateStatusChangeDescriptionIntegrationTest () {
    PatchStatusRequest request = new PatchStatusRequest(Optional.of("some new name"), Optional.of("new description"));
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    StatusResponse statusResponse = statusesService.updateStatus(status.getStatusId(), request, user);

    assertEquals("new description", statusResponse.getStatus_description());
  }

  @Test
  public void updateStatusDoNothingWithDescriptionIntegrationTest () {
    PatchStatusRequest request = new PatchStatusRequest(Optional.of("some new name"), Optional.of("new description"));
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    StatusResponse statusResponse = statusesService.updateStatus(status.getStatusId(), request, user);

    assertEquals("new description", statusResponse.getStatus_description());
  }

  @Test
  public void getStatusByIdSuccessIntegrationTest () {
    try {
      Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

      StatusResponse response = statusesService.getStatusById(status.getStatusId());

      assertEquals(response.getStatus_name(), status.getStatusName());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getStatusByIdStatusNotFoundIntegrationTest () {
    assertThrows(StatusNotFoundException.class, () ->
      statusesService.getStatusById(new UUID(123, 123))
    );
  }

  @Test
  public void getStatusesByParamsEmptyResultsIntegrationTest () {
    try {
      GetStatusesResponse response = statusesService.getStatusesByParams(null, null, 0, 50);

      assertEquals(0, response.getResults().size());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getStatusesByParamsPaginationIntegrationTest () {
    generateStatuses(130);

    assertAll(
      () -> assertEquals(1, statusesService.getStatusesByParams("110", null, 0, 50).getResults().size()),
      () -> assertEquals(100, statusesService.getStatusesByParams(null, null, 0, 150).getResults().size()),
      () -> assertEquals(0, statusesService.getStatusesByParams(null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, statusesService.getStatusesByParams(null, null, 0, 50).getTotal()),
      () -> assertEquals(11, statusesService.getStatusesByParams("11", null, 2, 50).getTotal())
    );
  }

  @Test
  public void getStatusesByParamsIntegrationTest () {
    Status status = statusRepository.save(new Status("status_1", "description_1", language, user));
    Status status2 = statusRepository.save(new Status("status_2", "description_2", language, user));
    statusRepository.save(new Status("AnOtHer", "strange DESC", language, user));

    AssetType firstAssetType = assetTypeRepository.save(new AssetType("first asset type", "desc", "at", "red", language, user));
    assetRepository.save(new Asset("first asset", firstAssetType, "test", language, status, null, user));
    Asset secondAsset = assetRepository.save(new Asset("second asset", firstAssetType, "test", language, status2, null, user));

    AssetType secondAssetType = assetTypeRepository.save(new AssetType("second asset type", "desc", "at", "red", language, user));
    assetRepository.save(new Asset("second asset second asset type", secondAssetType, "test", language, status, null, user));

    GetStatusesResponse descResponse = statusesService.getStatusesByParams(null, "desc", 0, 50);
    GetStatusesResponse firstStatusNameResponse = statusesService.getStatusesByParams("1", null, 0, 50);
    GetStatusesResponse secondStatusDescriptionResponse = statusesService.getStatusesByParams(null, "2", 0, 50);
    GetStatusesResponse aLetterResponse = statusesService.getStatusesByParams("A", null, 0, 50);

    assertAll(
      () -> assertEquals(3, statusesService.getStatusesByParams(null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, descResponse.getResults().size()),
      () -> assertEquals(1, firstStatusNameResponse.getResults().size()),
      () -> assertEquals(status.getStatusId(), firstStatusNameResponse.getResults().get(0).getStatus_id()),
      () -> assertEquals(2, firstStatusNameResponse.getResults().get(0).getStatus_usage_count()),
      () -> assertEquals(1, secondStatusDescriptionResponse.getResults().size()),
      () -> assertEquals(status2.getStatusId(), secondStatusDescriptionResponse.getResults().get(0).getStatus_id()),
      () -> assertEquals(1, secondStatusDescriptionResponse.getResults().get(0).getStatus_usage_count()),
      () -> assertEquals(3, aLetterResponse.getResults().size())
    );
  }

  @Test
  public void deleteStatusByIdSuccessIntegrationTest () {
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    statusesService.deleteStatusById(status.getStatusId(), user);

    Optional<Status> foundStatus = statusRepository.findById(status.getStatusId());

    assertAll(
      () -> assertTrue(foundStatus.get().getIsDeleted()),
      () -> assertEquals(foundStatus.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  @Test
  public void deleteStatusByIdAlreadyDeletedIntegrationTest () {
    Status status = new Status("another status", "another status desc", language, user);
    status.setIsDeleted(true);
    statusRepository.save(status);

    assertThrows(StatusNotFoundException.class, () -> statusesService.deleteStatusById(status.getStatusId(), user));
  }

  @Test
  public void deleteStatusByIdStatusNotFoundIntegrationTest () {
    assertThrows(StatusNotFoundException.class, () -> statusesService.deleteStatusById(new UUID(123, 123), user));
  }

  @Test
  public void deleteStatusByIdStatusIsUsedInAssetIntegrationTest () {
    Status status = statusRepository.save(new Status("another status", "another status desc", language, user));

    AssetType assetType = assetTypeRepository.save(new AssetType("asset type", "desc", "at", "red", language, user));
    Asset asset = assetRepository.save(new Asset("asset name", assetType, "test", language, status, null, user));

    assertThrows(StatusIsUsedForAssetException.class, () -> statusesService.deleteStatusById(status.getStatusId(), user));
  }

  private void generateStatuses (int count) {
    for (int i = 0; i < count; i++) {
      statusRepository.save(new Status("status_" + i, "desc_" + i, language, user));
    }
  }
}
