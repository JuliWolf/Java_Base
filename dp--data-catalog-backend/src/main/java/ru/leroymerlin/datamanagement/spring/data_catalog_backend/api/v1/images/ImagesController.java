package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions.WrongFileSizeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions.WrongFileTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.models.ImageResponse;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class ImagesController {
  @Autowired
  private ImagesService imagesService;

  private final String[] ALLOWED_FILE_TYPES = new String[]{ "image/jpeg", "image/png", "image/tiff"};

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ImageResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/images", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> uploadImage (
    @RequestPart(name = "file") MultipartFile file
  ) {
    try {
      if (!Arrays.asList(ALLOWED_FILE_TYPES).contains(file.getContentType())) {
        throw new WrongFileTypeException(file.getContentType());
      }

      ImageResponse response = imagesService.uploadImage(file);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (WrongFileTypeException wrongFileTypeException) {
      LoggerWrapper.error("Wrong file type in POST /v1/images: " + wrongFileTypeException.getMessage(),
        wrongFileTypeException.getStackTrace(),
        null,
        ImagesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Wrong file format"));
    } catch (WrongFileSizeException wrongFileSizeException) {
      LoggerWrapper.error("Size exceeds restriction in POST /v1/images: " + wrongFileSizeException.getMessage(),
        wrongFileSizeException.getStackTrace(),
        null,
        ImagesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Size exceeds restriction"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/images: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ImagesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ImageResponse.class)))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/images")
  public ResponseEntity<Object> getImages () {
    try {
      List<ImageResponse> images = imagesService.getImages();

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(images);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/images: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ImagesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
