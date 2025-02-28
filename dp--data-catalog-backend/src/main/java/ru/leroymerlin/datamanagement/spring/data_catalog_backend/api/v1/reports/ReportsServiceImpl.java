package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.AttributeValueValidator;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models.AssetReport;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models.ReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportReviewRequestAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportReviewRequestNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.Report;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportReviewRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.GetReportReviewRequestsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.GetReportsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.ReportsGetParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.ReviewStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PatchReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PostReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersDAO;

/**
 * @author juliwolf
 */

@Service
public class ReportsServiceImpl implements ReportsService {
  @Autowired
  private AssetsDAO assetsDAO;

  @Autowired
  private AttributesDAO attributesDAO;

  @Autowired
  private AttributesService attributesService;

  @Autowired
  private AttributeTypesDAO attributeTypesDAO;

  @Autowired
  private UsersDAO usersDAO;

  @Autowired
  private ResponsibilitiesDAO responsibilitiesDAO;

  @Autowired
  private ResponsibilitiesService responsibilitiesService;

  @Autowired
  private LanguageService languageService;

  @Autowired
  private AttributeValueValidator attributeValueValidator;

  @Autowired
  private RolesDAO rolesDAO;

  @Override
  public GetReportsResponse getReportsByParams (ReportsGetParams reportsGetParams) {
    Integer pageSize = PageableUtils.getPageSize(reportsGetParams.getPageSize());
    Integer pageNumber = PageableUtils.getPageNumber(reportsGetParams.getPageNumber());

    Page<AssetReport> response = assetsDAO.getReportsByParamsPageable(
      reportsGetParams.getReviewStatus(),
      reportsGetParams.getHasDataCatalogDescription(),
      reportsGetParams.getReportName(),
      reportsGetParams.getUniqueUsersLastMonthMin(),
      reportsGetParams.getUniqueUsersLastMonthMax(),
      reportsGetParams.getReportLastModifiedDateMin(),
      reportsGetParams.getReportLastModifiedDateMax(),
      reportsGetParams.getReportConfidentiality(),
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_id").ascending())
    );

    List<ReportResponse> reports = response.stream().map(this::mapAssetReport).toList();

    return new GetReportsResponse(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      reports
    );
  }

  @Override
  public ReportResponse getReportById (UUID reportId) throws ReportNotFoundException {
    Optional<AssetReport> optionalReport = assetsDAO.getReportsById(reportId);

    if (optionalReport.isEmpty()) {
      throw new ReportNotFoundException();
    }

    return mapAssetReport(optionalReport.get());
  }

  @Override
  @Transactional
  public ReportResponse updateReportById (
    UUID reportId,
    ReviewStatus reviewStatus,
    User user
  ) throws ReportNotFoundException {
    Boolean isReportExists = assetsDAO.isReportExistById(reportId);

    if (!isReportExists) {
      throw new ReportNotFoundException();
    }

    assetsDAO.updateReportReviewStatus(reportId, reviewStatus);

    return getReportById(reportId);
  }

  @Override
  public GetReportReviewRequestsResponse getReportReviewRequestsByParams (
    Integer pageSize,
    Integer pageNumber,
    String reviewerLdap,
    String reportDataCatalogId
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<ReportReviewRequest> response = attributesDAO.getReportReviewRequestsByParamsPageable(
      reviewerLdap,
      reportDataCatalogId,
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_id").ascending())
    );

    List<ReportReviewRequestResponse> reviewRequests = response.stream().map(this::mapAReportReviewRequest).toList();

    return new GetReportReviewRequestsResponse(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      reviewRequests
    );
  }

  @Override
  public ReportReviewRequestResponse getReportReviewRequestById (String reportReviewRequestId) throws ReportReviewRequestNotFoundException {
    Optional<ReportReviewRequest> optionalReportReviewRequest = attributesDAO.getReportReviewRequestByReportId(reportReviewRequestId);

    if (optionalReportReviewRequest.isEmpty()) {
      throw new ReportReviewRequestNotFoundException();
    }

    return mapAReportReviewRequest(optionalReportReviewRequest.get());
  }

  @Override
  @Transactional
  public ReportReviewRequestResponse createReportReviewRequest (
    PostReportReviewRequest request,
    User user
  ) throws
    UserNotFoundException,
    ReportNotFoundException,
    ReportReviewRequestAlreadyExistsException
  {
    Optional<Asset> optionalAsset = assetsDAO.findAssetByReportId(request.getReviewedReport().getReportDataCatalogId());

    if (optionalAsset.isEmpty()) {
      throw new ReportNotFoundException();
    }

    Boolean isReportReviewExists = attributesDAO.isReportReviewExistsByReportId(request.getReportReviewRequestId());
    if (isReportReviewExists) {
      throw new ReportReviewRequestAlreadyExistsException();
    }

    Asset asset = optionalAsset.get();
    AttributeType attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString("20e752bb-5e3c-45b6-9141-979be8aa874d"), false);
    createAttribute(request.getReportReviewRequestId(), attributeType, asset, user);

    User reviewer = findReviewerByUsername(request.getHasReviewer());
    if (reviewer != null) {
      createResponsibility(Optional.empty(), asset, reviewer, user);
    }

    return getReportReviewRequestById(request.getReportReviewRequestId());
  }

  @Override
  @Transactional
  public ReportReviewRequestResponse updateReportReviewRequest (
    String reportReviewRequestId,
    PatchReportReviewRequest request,
    User user
  ) throws ReportReviewRequestNotFoundException
  {
    Optional<Attribute> optionalAttribute = attributesDAO.findReportRequestByReportReviewId(reportReviewRequestId);

    if (optionalAttribute.isEmpty()) {
      throw new ReportReviewRequestNotFoundException();
    }

    Attribute attribute = optionalAttribute.get();

    User reviewer = findReviewerByUsername(request.getHasReviewer());
    recreateResponsibilityIfFound(attribute.getAsset(), reviewer, user);

    if (StringUtils.isNotEmpty(request.getReportReviewRequestClosedDate())) {
      recreateAttribute(request.getReportReviewRequestClosedDate(), attribute.getAsset(), user);
    }

    return getReportReviewRequestById(reportReviewRequestId);
  }

  @Override
  @Transactional
  public void deleteReportReviewRequestById (String reportReviewRequestId, User user) {
    Optional<Attribute> optionalAttribute = attributesDAO.findReportRequestByReportReviewId(reportReviewRequestId);

    if (optionalAttribute.isEmpty()) {
      throw new ReportReviewRequestNotFoundException();
    }

    Attribute attribute = optionalAttribute.get();
    List<UUID> attributesToDelete = new java.util.ArrayList<>(List.of(attribute.getAttributeId()));
    UUID attributeTypeId = UUID.fromString("685be2d1-9f50-4ec7-a137-aa2bbfe8c0fb");

    Optional<Attribute> optionalAssetAttribute = attributesDAO.findFirstAttributeByParams(attribute.getAsset().getAssetId(), attributeTypeId);

    if (optionalAssetAttribute.isPresent()) {
      Attribute assetAttribute = optionalAssetAttribute.get();

      attributesToDelete.add(assetAttribute.getAttributeId());
    }

    deleteResponsibility(attribute.getAsset(), user);
    attributesDAO.deleteAllByAttributeIds(attributesToDelete, user);

    attributesDAO.clearLinks(attributesToDelete, user);
  }

  private ReportResponse mapAssetReport (AssetReport report) {
    return new ReportResponse(
      report.getReportId(),
      report.getReportName(),
      report.getReportDataCatalogId(),
      null,
      report.getReportDescription(),
      report.getReportLink(),
      report.getReportTemplate(),
      report.getReportDataCatalogLink(),
      report.getHasDataCatalogDescription(),
      report.getReviewStatus(),
      report.getReportConfidentiality(),
      report.getReportTrainingMaterials(),
      report.getUniqueUsersLastMonth(),
      report.getReportLastModifiedDate(),
      report.getTechnicalOwner(),
      report.getReportReviewRequestId()
    );
  }

  private ReportReviewRequestResponse mapAReportReviewRequest (ReportReviewRequest reviewRequest) {
    return new ReportReviewRequestResponse(
      reviewRequest.getReportReviewRequestId(),
      reviewRequest.getReportReviewRequestCreationDate(),
      reviewRequest.getReportReviewRequestClosedDate(),
      reviewRequest.getLdap(),
      reviewRequest.getReportDataCatalogId()
    );
  }

  private User findReviewerByUsername (Report.UserLdap userLdap) {
    boolean hasReviewer = userLdap != null && StringUtils.isNotEmpty(userLdap.getLdap());

    if (!hasReviewer) return null;

    Optional<User> optionalUser = usersDAO.getUserByUsernameAndIsDeletedFalse(userLdap.getLdap());

    if (optionalUser.isPresent()) return optionalUser.get();

    throw new UserNotFoundException();
  }

    private void recreateResponsibilityIfFound (Asset asset, User reviewer, User user) {
    Optional<Responsibility> optionalResponsibility = deleteResponsibility(asset, user);

    createResponsibility(optionalResponsibility, asset, reviewer, user);
  }

  private void createResponsibility (Optional<Responsibility> responsibility, Asset asset, User reviewer, User user) {
    Role role;

    if (responsibility.isPresent()) {
      role = responsibility.get().getRole();
    } else {
      role = rolesDAO.findRoleById(UUID.fromString("dd2fce3b-ad2c-42f1-ba53-941ceae7be0d"));
    }

    responsibilitiesDAO.saveResponsibility(new Responsibility(
      reviewer,
      null,
      asset,
      role,
      ResponsibleType.USER,
      user
    ));
  }

  private Optional<Responsibility> deleteResponsibility (Asset asset, User user) {
    Optional<Responsibility> optionalResponsibility = responsibilitiesDAO.findResponsibilityByParams(null, null, asset.getAssetId(), UUID.fromString("dd2fce3b-ad2c-42f1-ba53-941ceae7be0d"), null, null);

    if (optionalResponsibility.isEmpty()) return Optional.empty();

    Responsibility responsibility = optionalResponsibility.get();

    responsibilitiesService.deleteResponsibilityById(responsibility.getResponsibilityId(), user);

    return Optional.of(responsibility);
  }

  private void recreateAttribute (String value, Asset asset, User user) {
    UUID attributeTypeId = UUID.fromString("685be2d1-9f50-4ec7-a137-aa2bbfe8c0fb");
    Optional<Attribute> optionalAttribute = attributesDAO.findFirstAttributeByParams(asset.getAssetId(), attributeTypeId);
    AttributeType attributeType;

    if (optionalAttribute.isPresent()) {
      Attribute attribute = optionalAttribute.get();

      attributesService.deleteAttributeById(attribute.getAttributeId(), user);
      attributesDAO.flush();

      attributeType = attribute.getAttributeType();
    } else {
      attributeType = attributeTypesDAO.findAttributeTypeById(attributeTypeId, false);
    }

    createAttribute(value, attributeType, asset, user);
  }

   private void createAttribute (String value, AttributeType attributeType, Asset asset, User user) {
     Language ru = languageService.getLanguage("ru");

     Attribute attribute = new Attribute(
       attributeType,
       asset,
       ru,
       user
     );

     attributeValueValidator.setAttributeValueByType(attribute, value, attributeType.getAttributeKindType());

     attributesDAO.saveAttribute(attribute);
   }
}
