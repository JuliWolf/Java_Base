package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.DevPortalRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions.BusinessFunctionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions.BusinessTermNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class DevPortalServiceImpl implements DevPortalService {
  private final DevPortalRepository devPortalRepository;


  public DevPortalServiceImpl (
    DevPortalRepository devPortalRepository
  ) {
    this.devPortalRepository = devPortalRepository;
  }

  @Override
  public GetBusinessFunctionBusinessTermsResponse getBusinessFunctionBusinessTerms (
    UUID businessFunctionId,
    Integer pageNumber,
    Integer pageSize
    ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<UUID> businessTermsIds = devPortalRepository.findAllBusinessTermsByBusinessFunctionIdPageable(
      businessFunctionId,
      PageRequest.of(pageNumber, pageSize, Sort.by("rc2.asset_id").ascending())
    );

    return new GetBusinessFunctionBusinessTermsResponse(
      businessTermsIds.getTotalElements(),
      pageSize,
      pageNumber,
      businessTermsIds.stream().map(businessTermId -> {
        GetBusinessFunctionBusinessTermsResponse.GetBusinessTermResponse businessTermResponse = new GetBusinessFunctionBusinessTermsResponse.GetBusinessTermResponse();

        businessTermResponse.setBusinessTermId(businessTermId);

        return businessTermResponse;
      }).toList()
    );
  }

  @Override
  public GetBusinessFunctionsResponse getBusinessFunctions (
    String businessFunctionName,
    String businessFunctionDescription,
    String businessFunctionDomainId,
    String businessOwnerLdap,
    String businessStructuralUnitNumber,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<BusinessFunction> businessFunctions = devPortalRepository.findAllBusinessFunctionPageable(
      businessFunctionName,
      businessFunctionDescription,
      businessFunctionDomainId,
      businessOwnerLdap,
      PageRequest.of(pageNumber, pageSize, Sort.by("bf_id").ascending())
    );

    List<GetBusinessFunctionsResponse.GetBusinessFunctionResponse> businessFunctionResponseList = businessFunctions.map(function -> new GetBusinessFunctionsResponse.GetBusinessFunctionResponse(
      function.getBdId(),
      function.getBfName(),
      function.getBfDescription(),
      function.getBfSynonymsSplitted(),
      null,
      function.getBfOwnerLdapSplitted(),
      function.getBfDomainCodeSplitted()
    )).toList();

    return new GetBusinessFunctionsResponse(
      businessFunctions.getTotalElements(),
      pageSize,
      pageNumber,
      businessFunctionResponseList
    );
  }

  @Override
  public GetBusinessFunctionsResponse.GetBusinessFunctionResponse getBusinessFunctionById (UUID businessFunctionId) throws BusinessFunctionNotFoundException {
    Optional<BusinessFunction> businessFunctionOptional = devPortalRepository.findBusinessFunctionById(businessFunctionId);

    if (businessFunctionOptional.isEmpty()) {
      throw new BusinessFunctionNotFoundException();
    }

    BusinessFunction function = businessFunctionOptional.get();

    return new GetBusinessFunctionsResponse.GetBusinessFunctionResponse(
      function.getBdId(),
      function.getBfName(),
      function.getBfDescription(),
      function.getBfSynonymsSplitted(),
      null,
      function.getBfOwnerLdapSplitted(),
      function.getBfDomainCodeSplitted()
    );
  }

  @Override
  public GetBusinessTermsResponse getBusinessTerms (
    String businessTermName,
    String businessTermTechnicalName,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<BusinessTermV1> businessTerms = devPortalRepository.findAllBusinessTermsPageableV1(
      businessTermName,
      businessTermTechnicalName,
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_id").ascending())
    );

    List<GetBusinessTermsResponse.GetBusinessTermResponse> businessTermsList = businessTerms.map(this::mapBusinessTerm).stream().toList();

    return new GetBusinessTermsResponse(
      businessTerms.getTotalElements(),
      pageSize,
      pageNumber,
      businessTermsList
    );
  }

  @Override
  public GetBusinessTermsResponse.GetBusinessTermResponse getBusinessTermById (
    UUID businessTermId
  ) throws BusinessTermNotFoundException {
    Optional<BusinessTermV1> optionalBusinessTerm = devPortalRepository.findBusinessTermByIdV1(businessTermId);

    if (optionalBusinessTerm.isEmpty()) {
      throw new BusinessTermNotFoundException();
    }

    return mapBusinessTerm(optionalBusinessTerm.get());
  }

  @Override
  public BusinessTermAttributesResponse getBusinessTermAttributes (
    BusinessTermAttributeRequest businessTermAttributeRequest
  ) throws BusinessTermNotFoundException {
    Boolean isBusinessTermExists = devPortalRepository.isBusinessTermExists(businessTermAttributeRequest.getBusinessTermId());

    if (!isBusinessTermExists) {
      throw new BusinessTermNotFoundException();
    }

    int pageSize = PageableUtils.getPageSize(businessTermAttributeRequest.getPageSize());
    int pageNumber = PageableUtils.getPageNumber(businessTermAttributeRequest.getPageNumber());

    Page<BusinessTermAttribute> businessAttributesResponse = devPortalRepository.findAllBusinessTermAttributesPageable(
      businessTermAttributeRequest.getBusinessTermId(),
      businessTermAttributeRequest.getBusinessAttributeName(),
      businessTermAttributeRequest.getBusinessAttributeDataType() != null ? businessTermAttributeRequest.getBusinessAttributeDataType().toString() : null,
      businessTermAttributeRequest.getBusinessAttributeTechnicalName(),
      businessTermAttributeRequest.getBusinessAttributeConfidentiality() != null ? businessTermAttributeRequest.getBusinessAttributeConfidentiality().toString() : null,
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_id").ascending())
    );

    List<BusinessTermAttributesResponse.GetBusinessAttributeResponse> businessAttributes = businessAttributesResponse.map(response -> new BusinessTermAttributesResponse.GetBusinessAttributeResponse(
      response.getBaId(),
      response.getBaName(),
      response.getBaTechName(),
      response.getBaDefinition(),
      response.getBaSynonymsSplitted(),
      businessTermAttributeRequest.getBusinessTermId(),
      response.getBaDataType(),
      response.getBaConfidentiality(),
      response.getBaPrimaryKey()
    )).stream().toList();

    return new BusinessTermAttributesResponse(
      businessAttributesResponse.getTotalElements(),
      pageSize,
      pageNumber,
      businessAttributes
    );
  }

  @Override
  public BusinessTermRelationshipsResponse getBusinessTermRelationships (
    BusinessTermRelationshipsRequest businessTermRelationshipsRequest
  ) throws BusinessTermNotFoundException
  {
    Boolean isBusinessTermExists = devPortalRepository.isBusinessTermExists(businessTermRelationshipsRequest.getBusinessTermId());

    if (!isBusinessTermExists) {
      throw new BusinessTermNotFoundException();
    }

    int pageSize = PageableUtils.getPageSize(businessTermRelationshipsRequest.getPageSize());
    int pageNumber = PageableUtils.getPageNumber(businessTermRelationshipsRequest.getPageNumber());

    Page<BusinessRelationship> businessRelationshipsResponse = devPortalRepository.findAllBusinessTermRelationshipsPageable(
      businessTermRelationshipsRequest.getBusinessTermId(),
      businessTermRelationshipsRequest.getBusinessTermRelationshipName(),
      businessTermRelationshipsRequest.getBusinessTermRelationCardinality() != null ? businessTermRelationshipsRequest.getBusinessTermRelationCardinality().getValue() : null,
      businessTermRelationshipsRequest.getBusinessTermRelationshipTechnicalName(),
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_id").ascending())
    );

    List<BusinessTermRelationshipsResponse.BusinessTermRelationshipResponse> businessRelationship = businessRelationshipsResponse.map(response -> new BusinessTermRelationshipsResponse.BusinessTermRelationshipResponse(
      businessTermRelationshipsRequest.getBusinessTermId(),
      response.getBusinessTermRelationId(),
      response.getBusinessTermRelationshipName(),
      response.getBusinessTermRelationshipTechnicalName(),
      response.getRelatedBusinessTermId(),
      response.getBusinessTermRelationCardinality()
    )).stream().toList();

    return new BusinessTermRelationshipsResponse(
      businessRelationshipsResponse.getTotalElements(),
      pageSize,
      pageNumber,
      businessRelationship
    );
  }

  private GetBusinessTermsResponse.GetBusinessTermResponse mapBusinessTerm (BusinessTermV1 businessTerm) {
    return new GetBusinessTermsResponse.GetBusinessTermResponse(
      businessTerm.getId(),
      businessTerm.getName(),
      businessTerm.getTechnicalName(),
      businessTerm.getDescription(),
      businessTerm.getBfSynonymsSplitted(),
      businessTerm.getOwnerLdap()
    );
  }
}
