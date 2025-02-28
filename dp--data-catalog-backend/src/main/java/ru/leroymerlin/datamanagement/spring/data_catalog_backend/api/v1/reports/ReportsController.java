package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports;

import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.InvalidReviewStatusValueException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportReviewRequestAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.exceptions.ReportReviewRequestNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.ReportReviewRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PatchReportRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PatchReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.reports.models.post.PostReportReviewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class ReportsController {
  @Autowired
  private ReportsService reportsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ReportResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/reports/{reportDataCatalogId}")
  public ResponseEntity<Object> getReportById (
    @PathVariable(value = "reportDataCatalogId") String reportId
  ) {
    try {
      UUID uuid = UUID.fromString(reportId);
      ReportResponse report = reportsService.getReportById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(report);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid reports id '" + reportId + "' in GET /v1/reports/{reportDataCatalogId}",
        illegalArgumentException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested report not found"));
    } catch (ReportNotFoundException reportNotFoundException) {
      LoggerWrapper.error("Report with id '" + reportId + "' not found in GET /v1/reports/{reportDataCatalogId}",
        reportNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportNotFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/reports/{reportDataCatalogId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetReportsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping("/reports/{reportDataCatalogId}")
  public ResponseEntity<Object> updateReportById (
    Authentication userData,
    @PathVariable(value = "reportDataCatalogId") String reportId,
    @RequestBody PatchReportRequest request
  ) {
    try {
      if (StringUtils.isEmpty(request.getReviewStatus())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Some of required fields are empty."));
      }

      ReviewStatus reviewStatusValue = ReviewStatus.getReviewStatus(request.getReviewStatus());
      UUID uuid = UUID.fromString(reportId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      ReportResponse report = reportsService.updateReportById(uuid, reviewStatusValue, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(report);
    } catch (InvalidReviewStatusValueException invalidReviewStatusValueException) {
      LoggerWrapper.error("Invalid review status value in PATCH /v1/reports/{reportDataCatalogId}",
        invalidReviewStatusValueException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid reports id '" + reportId + "' in PATCH /v1/reports/{reportDataCatalogId}",
        illegalArgumentException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested report not found"));
    } catch (ReportNotFoundException reportNotFoundException) {
      LoggerWrapper.error("Report with id '" + reportId + "' not found in PATCH /v1/reports/{reportDataCatalogId}",
        reportNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportNotFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/reports/{reportDataCatalogId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetReportsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/reports")
  public ResponseEntity<Object> getReportsByParams (
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @RequestParam(value = "reportName", required = false) String reportName,
    @RequestParam(value = "reviewStatus", required = false) String reviewStatus,
    @RequestParam(value = "reportConfidentiality", required = false) ReportConfidentiality reportConfidentiality,
    @RequestParam(value = "uniqueUsersLastMonthMin", required = false) Long uniqueUsersLastMonthMin,
    @RequestParam(value = "uniqueUsersLastMonthMax", required = false) Long uniqueUsersLastMonthMax,
    @RequestParam(value = "reportLastModifiedDateMin", required = false) String reportLastModifiedDateMin,
    @RequestParam(value = "reportLastModifiedDateMax", required = false) String reportLastModifiedDateMax,
    @RequestParam(value = "hasDataCatalogDescription", required = false) Boolean hasDataCatalogDescription
  ) {
    try {
      Timestamp reportLastModifiedDateMinTimestamp = null;
      if (StringUtils.isNotEmpty(reportLastModifiedDateMin)) {
        reportLastModifiedDateMinTimestamp = new Timestamp(Long.parseLong(reportLastModifiedDateMin));
      }

      Timestamp reportLastModifiedDateMaxTimestamp = null;
      if (StringUtils.isNotEmpty(reportLastModifiedDateMax)) {
        reportLastModifiedDateMaxTimestamp = new Timestamp(Long.parseLong(reportLastModifiedDateMax));
      }

      ReviewStatus reviewStatusValue = null;
      if (StringUtils.isNotEmpty(reviewStatus)) {
        reviewStatusValue = ReviewStatus.getReviewStatus(reviewStatus);
      }

      GetReportsResponse reports = reportsService.getReportsByParams(
        new ReportsGetParams(
          pageSize,
          pageNumber,
          reportName,
          reviewStatusValue,
          reportConfidentiality,
          uniqueUsersLastMonthMin,
          uniqueUsersLastMonthMax,
          reportLastModifiedDateMinTimestamp,
          reportLastModifiedDateMaxTimestamp,
          hasDataCatalogDescription
        )
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(reports);
    } catch (InvalidReviewStatusValueException invalidReviewStatusValueException) {
      LoggerWrapper.error("Invalid review status value in GET /v1/reports",
        invalidReviewStatusValueException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/reports: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ReportReviewRequestResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/report-review-requests")
  public ResponseEntity<Object> createReportReviewRequest (
    Authentication userData,
    @RequestBody PostReportReviewRequest request
  ) {
    try {
      boolean isReviewerReportEmpty = request.getReviewedReport() == null || request.getReviewedReport().getReportDataCatalogId() == null;
      if (
        isReviewerReportEmpty &&
        StringUtils.isEmpty(request.getReportReviewRequestId())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Some of required fields are empty."));
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      ReportReviewRequestResponse reportReviewRequest = reportsService.createReportReviewRequest(request, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(reportReviewRequest);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Report with id " + request.getReportReviewRequestId() + " not found in POST /v1/report-review-requests",
        illegalArgumentException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested report not found"));
    } catch (ReportNotFoundException reportNotFoundException) {
      LoggerWrapper.error("Report with id " + request.getReportReviewRequestId() + " not found in POST /v1/report-review-requests",
        reportNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportNotFoundException.getMessage()));
    } catch (ReportReviewRequestAlreadyExistsException reportReviewRequestAlreadyExistsException) {
      LoggerWrapper.error("Report review request already exists in POST /v1/report-review-requests",
        reportReviewRequestAlreadyExistsException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportReviewRequestAlreadyExistsException.getMessage()));
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error("User not found in POST /v1/report-review-requests",
        userNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(userNotFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/report-review-requests: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ReportReviewRequestResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping("/report-review-requests/{reportReviewRequestId}")
  public ResponseEntity<Object> updateReportReviewRequest (
    Authentication userData,
    @RequestBody PatchReportReviewRequest request,
    @PathVariable(value = "reportReviewRequestId") String reportReviewRequestId
  ) {
    try {
      boolean isReviewerEmpty = request.getHasReviewer() == null || StringUtils.isEmpty(request.getHasReviewer().getLdap());
      if (
        isReviewerEmpty &&
        StringUtils.isEmpty(request.getReportReviewRequestClosedDate())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Some of required fields are empty."));
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      ReportReviewRequestResponse reportReview = reportsService.updateReportReviewRequest(reportReviewRequestId, request, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(reportReview);
    } catch (ReportReviewRequestNotFoundException reportReviewRequestNotFoundException) {
      LoggerWrapper.error("Report review request with id '" + reportReviewRequestId + "' not found in PATCH /v1/report-review-requests/{reportReviewRequestId}",
        reportReviewRequestNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportReviewRequestNotFoundException.getMessage()));
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error("User not found in PATCH /v1/report-review-requests/{reportReviewRequestId}",
        userNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(userNotFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/report-review-requests/reportReviewRequestId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetReportReviewRequestsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/report-review-requests")
  public ResponseEntity<Object> getReportReviewRequestByParams (
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @RequestParam(value = "hasReviewer.ldap", required = false) String reviewerLdap,
    @RequestParam(value = "reviewReport.reportDataCatalogId", required = false) String reportDataCatalogId
  ) {
    try {
      GetReportReviewRequestsResponse reportReviews = reportsService.getReportReviewRequestsByParams(
        pageSize,
        pageNumber,
        reviewerLdap,
        reportDataCatalogId
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(reportReviews);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/report-review-requests: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ReportReviewRequestResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/report-review-requests/{reportReviewRequestId}")
  public ResponseEntity<Object> getReportReviewRequestById (
    @PathVariable(value = "reportReviewRequestId") String reportReviewRequestId
  ) {
    try {
      ReportReviewRequestResponse reportReview = reportsService.getReportReviewRequestById(reportReviewRequestId);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(reportReview);
    } catch (ReportReviewRequestNotFoundException reportReviewRequestNotFoundException) {
      LoggerWrapper.error("Report review request with id '" + reportReviewRequestId + "' not found in GET /v1/report-review-requests/{reportReviewRequestId}",
        reportReviewRequestNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportReviewRequestNotFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/report-review-requests/reportReviewRequestId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SuccessResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/report-review-requests/{reportReviewRequestId}")
  public ResponseEntity<Object> deleteReportReviewRequestById (
    Authentication userData,
    @PathVariable(value = "reportReviewRequestId") String reportReviewRequestId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      reportsService.deleteReportReviewRequestById(reportReviewRequestId, userDetails.getUser());

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NO_CONTENT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse());
    } catch (ReportReviewRequestNotFoundException reportReviewRequestNotFoundException) {
      LoggerWrapper.error("Report review request with id '" + reportReviewRequestId + "' not found in DELETE /v1/report-review-requests/reportReviewRequestId}",
        reportReviewRequestNotFoundException.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(reportReviewRequestNotFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/report-review-requests/reportReviewRequestId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ReportsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
