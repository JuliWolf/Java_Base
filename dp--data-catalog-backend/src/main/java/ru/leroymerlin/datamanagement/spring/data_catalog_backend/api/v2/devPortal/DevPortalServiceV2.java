package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.exceptions.BusinessTermNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.models.get.*;

/**
 * @author juliwolf
 */

public interface DevPortalServiceV2 {
  GetBusinessTermsResponse getBusinessTerms (String businessTermName, String businessTermTechnicalName, Integer pageNumber, Integer pageSize);

  GetBusinessTermsResponse.GetBusinessTermResponse getBusinessTermById (UUID businessTermId);

  BusinessTermAttributesResponse getBusinessTermAttributes (BusinessTermAttributeRequest businessTermAttributeRequest) throws BusinessTermNotFoundException;

  BusinessTermRelationshipsResponse getBusinessTermRelationships (BusinessTermRelationshipsRequest businessTermRelationshipsRequest) throws BusinessTermNotFoundException;
}

