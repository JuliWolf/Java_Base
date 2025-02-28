package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.exceptions.BusinessTermNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.DevPortalRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models.BusinessRelationship;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models.BusinessTermAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models.BusinessTermV2;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;

/**
 * @author juliwolf
 */

@Service
public class DevPortalServiceV2Impl implements DevPortalServiceV2 {
  private final DevPortalRepository devPortalRepository;

  public DevPortalServiceV2Impl (
    DevPortalRepository devPortalRepository
  ) {
    this.devPortalRepository = devPortalRepository;
  }

  @Override
  public GetBusinessTermsResponse getBusinessTerms (
    String businessTermName,
    String businessTermTechnicalName,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber, 1);

    Page<BusinessTermV2> businessTerms = devPortalRepository.findAllBusinessTermsPageableV2(
      businessTermName,
      businessTermTechnicalName,
      PageRequest.of(pageNumber, pageSize, Sort.by("asset_id").ascending())
    );

    List<GetBusinessTermsResponse.GetBusinessTermResponse> businessTermsList = businessTerms.map(this::mapBusinessTerm).stream().toList();

    return new GetBusinessTermsResponse(
      businessTerms.getTotalElements(),
      pageSize,
      ++pageNumber,
      businessTermsList
    );
  }

  @Override
  public GetBusinessTermsResponse.GetBusinessTermResponse getBusinessTermById (
    UUID businessTermId
  ) throws BusinessTermNotFoundException {
    Optional<BusinessTermV2> optionalBusinessTerm = devPortalRepository.findBusinessTermByIdV2(businessTermId);

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
    int pageNumber = PageableUtils.getPageNumber(businessTermAttributeRequest.getPageNumber(), 1);

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
      ++pageNumber,
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
    int pageNumber = PageableUtils.getPageNumber(businessTermRelationshipsRequest.getPageNumber(), 1);

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
      ++pageNumber,
      businessRelationship
    );
  }

  private GetBusinessTermsResponse.GetBusinessTermResponse mapBusinessTerm (BusinessTermV2 businessTerm) {
    return new GetBusinessTermsResponse.GetBusinessTermResponse(
      businessTerm.getId(),
      businessTerm.getName(),
      businessTerm.getTechnicalName(),
      businessTerm.getDefinition(),
      businessTerm.getDigitalProductsSplitted(),
      businessTerm.getBfSynonymsSplitted(),
      businessTerm.getOwnerLdapsSplitted()
    );
  }
}
