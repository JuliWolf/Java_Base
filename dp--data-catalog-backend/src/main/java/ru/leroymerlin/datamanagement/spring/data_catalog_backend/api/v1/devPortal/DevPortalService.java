package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions.BusinessTermNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get.*;

/**
 * @author juliwolf
 */

public interface DevPortalService {
  GetBusinessFunctionBusinessTermsResponse getBusinessFunctionBusinessTerms (UUID businessFunctionId, Integer pageNumber, Integer pageSize);

  GetBusinessFunctionsResponse getBusinessFunctions (
    String businessFunctionName,
    String businessFunctionDescription,
    String businessFunctionDomainId,
    String businessOwnerLdap,
    String businessStructuralUnitNumber,
    Integer pageNumber,
    Integer pageSize
  );

  GetBusinessFunctionsResponse.GetBusinessFunctionResponse getBusinessFunctionById (UUID businessFunctionId);

  GetBusinessTermsResponse getBusinessTerms (String businessTermName, String businessTermTechnicalName, Integer pageNumber, Integer pageSize);

  GetBusinessTermsResponse.GetBusinessTermResponse getBusinessTermById (UUID businessTermId);

  BusinessTermAttributesResponse getBusinessTermAttributes (BusinessTermAttributeRequest businessTermAttributeRequest) throws BusinessTermNotFoundException;

  BusinessTermRelationshipsResponse getBusinessTermRelationships (BusinessTermRelationshipsRequest businessTermRelationshipsRequest) throws BusinessTermNotFoundException;
}

