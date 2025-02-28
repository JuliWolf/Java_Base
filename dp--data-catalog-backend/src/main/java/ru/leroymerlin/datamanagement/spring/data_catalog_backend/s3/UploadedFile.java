package ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadedFile {
  private String fileName;

  private String fileUrl;
}
