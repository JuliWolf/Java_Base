package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportReviewRequestNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportReviewRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.GetReportReviewRequestsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.GetReportsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.ReportsGetParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.ReviewStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PatchReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PostReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;

/**
 * @author juliwolf
 */

public interface ReportsService {
  GetReportsResponse getReportsByParams (ReportsGetParams reportsGetParams);

  ReportResponse getReportById (UUID reportId) throws ReportNotFoundException;

  ReportResponse updateReportById (UUID reportId, ReviewStatus reviewStatus, User user) throws ReportNotFoundException;

  GetReportReviewRequestsResponse getReportReviewRequestsByParams (Integer pageSize, Integer pageNumber, String reviewerLdap, String reportDataCatalogId);

  ReportReviewRequestResponse getReportReviewRequestById (String reportReviewRequestId) throws ReportReviewRequestNotFoundException;

  ReportReviewRequestResponse updateReportReviewRequest (String reportReviewRequestId, PatchReportReviewRequest request, User user) throws ReportReviewRequestNotFoundException;

  void deleteReportReviewRequestById (String reportReviewRequestId, User user);

  ReportReviewRequestResponse createReportReviewRequest (PostReportReviewRequest request, User user) throws ReportNotFoundException, UserNotFoundException;
}
