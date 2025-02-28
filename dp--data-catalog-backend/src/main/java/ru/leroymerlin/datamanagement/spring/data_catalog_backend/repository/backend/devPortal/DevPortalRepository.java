package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.devPortal.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;

/**
 * @author juliwolf
 */

public interface DevPortalRepository extends JpaRepository<Asset, UUID> {
  @Query(value = """
    select rc2.asset_id
    from relation_component rc
    inner join relation_component rc2 on rc.relation_id = rc2.relation_id and not rc.deleted_flag and rc.asset_id != rc2.asset_id
    inner join relation r on r.relation_id = rc.relation_id
    where
      relation_type_id = '6eb20822-9465-4f49-a66e-554342201bb8' and
      rc.asset_id = :businessFunctionId
    """,
    countQuery = """
      select count(rc2.asset_id)
      from relation_component rc
      inner join relation_component rc2 on rc.relation_id = rc2.relation_id and not rc.deleted_flag and rc.asset_id != rc2.asset_id
      inner join relation r on r.relation_id = rc.relation_id
      where
        relation_type_id = '6eb20822-9465-4f49-a66e-554342201bb8' and
        rc.asset_id = :businessFunctionId
    """, nativeQuery = true)
  Page<UUID> findAllBusinessTermsByBusinessFunctionIdPageable (
    @Param("businessFunctionId") UUID businessFunctionId,
    Pageable pageable
  );

  @Query(value = """
    WITH business_functions as (
      select a.asset_id as bf_id, a.asset_displayname as bf_name
      from public.asset a
      where
        a.asset_type_id = '00000000-0000-0000-0000-000000031103'
        and not a.deleted_flag
        and a.asset_name like '%>%>%>%>%'
        and a.asset_name not like '%>%>%>%>%>%'
    ), relations_flat as (
      select
        rc1.asset_id as asset_id1,
        rc2.asset_id as asset_id2,
        r.relation_type_id as relation_type_id
      from relation_component rc1
      inner join relation_component rc2 on rc1.relation_id = rc2.relation_id and not rc1.deleted_flag and rc1.asset_id != rc2.asset_id
      inner join relation r on r.relation_id = rc1.relation_id
      inner join asset a on a.asset_id = rc1.asset_id and a.asset_type_id = '00000000-0000-0000-0000-000000031103'
      Where
        relation_type_id = 'd930cf17-e48c-4921-9e3e-0650be6b4d55'
    ), domainCodeAt as (
      Select STRING_AGG(at.value, '--') as value, rf.asset_id1
      From relations_flat rf
      inner join attribute at on at.asset_id = rf.asset_id2
                and at.attribute_type_id = '93bbb198-8a23-42c3-a474-9a13f06e3e17'
      GROUP BY rf.asset_id1
    )
    Select
      cast(bf.bf_id as text) as bdIdText,
      bf.bf_name as bfName,
      descriptionAt.value as bfDescription,
      STRING_AGG(synonymsAt.value, '--') as bfSynonyms,
      STRING_AGG(u.username, '--') as bfOwnerLdap,
      rf.value as bfDomainCode
    From business_functions bf
    left join attribute descriptionAt on
      bf.bf_id = descriptionAt.asset_id and 
      not descriptionAt.deleted_flag and
      descriptionAt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    left join responsibility r on 
      r.asset_id = bf.bf_id and 
      r.role_id = '00000000-0000-0000-0000-000000005040' and 
      not r.deleted_flag
    left join "user" u on u.user_id = r.user_id
    left join attribute synonymsAt on 
      synonymsAt.asset_id = bf.bf_id and 
      not synonymsAt.deleted_flag and
      synonymsAt.attribute_type_id = 'e75711d2-9775-4620-9a15-3fa2d6af2d18'
    left join domainCodeAt rf on rf.asset_id1 = bf.bf_id
    WHERE
      bf_id = :businessFunctionId
    Group by bf.bf_id, bf.bf_name, descriptionAt.value, rf.value
  """, nativeQuery = true)
  Optional<BusinessFunction> findBusinessFunctionById (
    @Param("businessFunctionId") UUID businessFunctionId
  );

  @Query(value = """
    WITH business_functions as (
      select a.asset_id as bf_id, a.asset_displayname as bf_name
      from public.asset a
      where
        a.asset_type_id = '00000000-0000-0000-0000-000000031103'
        and not a.deleted_flag
        and a.asset_name like '%>%>%>%>%'
        and a.asset_name not like '%>%>%>%>%>%'
    ), relations_flat as (
      select
        rc1.asset_id as asset_id1,
        rc2.asset_id as asset_id2,
        r.relation_type_id as relation_type_id
      from relation_component rc1
      inner join relation_component rc2 on rc1.relation_id = rc2.relation_id and not rc1.deleted_flag and rc1.asset_id != rc2.asset_id
      inner join relation r on r.relation_id = rc1.relation_id
      inner join asset a on a.asset_id = rc1.asset_id and a.asset_type_id = '00000000-0000-0000-0000-000000031103'
      Where
        relation_type_id = 'd930cf17-e48c-4921-9e3e-0650be6b4d55'
    ), domainCodeAt as (
      Select STRING_AGG(at.value, '--') as value, rf.asset_id1
      From relations_flat rf
      inner join attribute at on at.asset_id = rf.asset_id2
                and at.attribute_type_id = '93bbb198-8a23-42c3-a474-9a13f06e3e17'
      GROUP BY rf.asset_id1
    )
    Select
      cast(bf.bf_id as text) as bdIdText,
      bf.bf_name as bfName,
      descriptionAt.value as bfDescription,
      STRING_AGG(synonymsAt.value, '--') as bfSynonyms,
      STRING_AGG(u.username, '--') as bfOwnerLdap,
      rf.value as bfDomainCode
    From business_functions bf
    left join attribute descriptionAt on
      bf.bf_id = descriptionAt.asset_id and
      not descriptionAt.deleted_flag and
      descriptionAt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    left join responsibility r on
      r.asset_id = bf.bf_id and 
      r.role_id = '00000000-0000-0000-0000-000000005040' and 
      not r.deleted_flag
    left join "user" u on u.user_id = r.user_id
    left join attribute synonymsAt on 
      synonymsAt.asset_id = bf.bf_id and 
      not synonymsAt.deleted_flag and
      synonymsAt.attribute_type_id = 'e75711d2-9775-4620-9a15-3fa2d6af2d18'
    left join domainCodeAt rf on rf.asset_id1 = bf.bf_id
    WHERE 
      (:businessFunctionName is null or bf.bf_name = :businessFunctionName)
      and (:businessFunctionDescription is null or descriptionAt.value = :businessFunctionDescription)
      and (
        :businessFunctionDomainId is null or
        rf.value like :businessFunctionDomainId || '--' || '%' or
        rf.value like '%' || '--' || :businessFunctionDomainId || '--' || '%' or
        rf.value like '%' || '--' || :businessFunctionDomainId or
        rf.value = :businessFunctionDomainId
      )
    Group by bf.bf_id, bf.bf_name, descriptionAt.value, rf.value
    HAVING (
      cast(:businessOwnerLdap as text) is null or
      STRING_AGG(u.username, '--') like :businessOwnerLdap || '--' || '%' or
      STRING_AGG(u.username, '--') like '%' || '--' || :businessOwnerLdap || '--' || '%' or
      STRING_AGG(u.username, '--') like '%' || '--' || :businessOwnerLdap or
      STRING_AGG(u.username, '--') = :businessOwnerLdap
    )
  """, countQuery = """
    WITH business_functions as (
      select a.asset_id as bf_id, a.asset_displayname as bf_name
      from public.asset a
      where
        a.asset_type_id = '00000000-0000-0000-0000-000000031103'
        and not a.deleted_flag
        and a.asset_name like '%>%>%>%>%'
        and a.asset_name not like '%>%>%>%>%>%'
    ), relations_flat as (
      select
        rc1.asset_id as asset_id1,
        rc2.asset_id as asset_id2,
        r.relation_type_id as relation_type_id
      from relation_component rc1
      inner join relation_component rc2 on rc1.relation_id = rc2.relation_id and not rc1.deleted_flag and rc1.asset_id != rc2.asset_id
      inner join relation r on r.relation_id = rc1.relation_id
      inner join asset a on a.asset_id = rc1.asset_id and a.asset_type_id = '00000000-0000-0000-0000-000000031103'
      Where
        relation_type_id = 'd930cf17-e48c-4921-9e3e-0650be6b4d55'
    ), domainCodeAt as (
      Select STRING_AGG(at.value, '--') as value, rf.asset_id1
      From relations_flat rf
      inner join attribute at on at.asset_id = rf.asset_id2
                and at.attribute_type_id = '93bbb198-8a23-42c3-a474-9a13f06e3e17'
      GROUP BY rf.asset_id1
    )
    Select count(*)
    From (
      Select cast(bf.bf_id as text) as bdIdText
      From business_functions bf
      left join attribute descriptionAt on
        bf.bf_id = descriptionAt.asset_id and
        not descriptionAt.deleted_flag and
        descriptionAt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
      left join responsibility r on
        r.asset_id = bf.bf_id and
        r.role_id = '00000000-0000-0000-0000-000000005040' and
        not r.deleted_flag
      left join "user" u on u.user_id = r.user_id
      left join attribute synonymsAt on
        synonymsAt.asset_id = bf.bf_id and
        not synonymsAt.deleted_flag and
        synonymsAt.attribute_type_id = 'e75711d2-9775-4620-9a15-3fa2d6af2d18'
      left join domainCodeAt rf on rf.asset_id1 = bf.bf_id
      WHERE
        (:businessFunctionName is null or bf.bf_name = :businessFunctionName)
        and (:businessFunctionDescription is null or descriptionAt.value = :businessFunctionDescription)
        and (
          :businessFunctionDomainId is null or
          rf.value like :businessFunctionDomainId || '--' || '%' or
          rf.value like '%' || '--' || :businessFunctionDomainId || '--' || '%' or
          rf.value like '%' || '--' || :businessFunctionDomainId or
          rf.value = :businessFunctionDomainId
        )
      Group by bf.bf_id, bf.bf_name, descriptionAt.value, rf.value
      HAVING (
        cast(:businessOwnerLdap as text) is null or
        STRING_AGG(u.username, '--') like :businessOwnerLdap || '--' || '%' or
        STRING_AGG(u.username, '--') like '%' || '--' || :businessOwnerLdap || '--' || '%' or
        STRING_AGG(u.username, '--') like '%' || '--' || :businessOwnerLdap or
        STRING_AGG(u.username, '--') = :businessOwnerLdap
      )
    ) sel
  """, nativeQuery = true)
  Page<BusinessFunction> findAllBusinessFunctionPageable (
    @Param("businessFunctionName") String businessFunctionName,
    @Param("businessFunctionDescription") String businessFunctionDescription,
    @Param("businessFunctionDomainId") String businessFunctionDomainId,
    @Param("businessOwnerLdap") String businessOwnerLdap,
    Pageable pageable
  );

  @Query(value = """
    select
        cast(a.asset_id as text) as idText,
        a.asset_displayname as name,
        technicalNameAtt.value as technicalName,
        descriptionAtt.value as description,
        u.username as ownerLdap,
        string_agg(synonymsAtt.value, '--') as synonyms
    from asset a
    inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%'
    inner join attribute descriptionAtt on
        descriptionAtt.asset_id = a.asset_id and
        not descriptionAtt.deleted_flag and
        descriptionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join responsibility resp on
        resp.asset_id = a.asset_id and resp.role_id = '00000000-0000-0000-0000-000000005040' and not resp.deleted_flag
    inner join "user" u on u.user_id = resp.user_id
    left join attribute synonymsAtt on
        synonymsAtt.asset_id = a.asset_id and
        not synonymsAtt.deleted_flag and
        synonymsAtt.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'
    Where
        (:businessTermName is null or :businessTermName = a.asset_displayname) and
        (:businessTermTechnicalName is null or :businessTermTechnicalName = technicalNameAtt.value)
    group by a.asset_id, a.asset_displayname, technicalNameAtt.value, descriptionAtt.value, u.username
  """, countQuery = """
    Select count(q.*)
    From (
      select
          a.asset_id as id
      from asset a
      inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%'
    inner join attribute descriptionAtt on
        descriptionAtt.asset_id = a.asset_id and
        not descriptionAtt.deleted_flag and
        descriptionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join responsibility resp on
        resp.asset_id = a.asset_id and resp.role_id = '00000000-0000-0000-0000-000000005040' and not resp.deleted_flag
    inner join "user" u on u.user_id = resp.user_id
    left join attribute synonymsAtt on
        synonymsAtt.asset_id = a.asset_id and
        not synonymsAtt.deleted_flag and
        synonymsAtt.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'
    Where
        (:businessTermName is null or :businessTermName = a.asset_displayname) and
        (:businessTermTechnicalName is null or :businessTermTechnicalName = technicalNameAtt.value)
    ) q
  """, nativeQuery = true)
  Page<BusinessTermV1> findAllBusinessTermsPageableV1 (
    @Param("businessTermName") String businessTermName,
    @Param("businessTermTechnicalName") String businessTermTechnicalName,
    Pageable pageable
  );

  @Query(value = """
    select
        cast(a.asset_id as text) as idText,
        a.asset_displayname as name,
        technicalNameAtt.value as technicalName,
        definitionAtt.value as definition,
        string_agg(u.username, ',') as ownerLdaps,
        string_agg(digitalProductsAsset.asset_name, ',') as digitalProducts,
        string_agg(synonymsAtt.value, ',') as synonyms
    from asset a
    inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%'
    inner join attribute definitionAtt on
        definitionAtt.asset_id = a.asset_id and
        not definitionAtt.deleted_flag and
        definitionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join responsibility resp on
        resp.asset_id = a.asset_id and
        resp.role_id = '00000000-0000-0000-0000-000000005040' and
        not resp.deleted_flag
    inner join "user" u on u.user_id = resp.user_id
    left join relation_component rc on
        rc.asset_id = a.asset_id and
        rc.relation_type_component_id = 'd2065e8d-a108-4a22-a562-00281041f714' and
        not rc.deleted_flag
    left join relation_component rc2 on
        rc.relation_id = rc2.relation_id and
        rc.asset_id != rc2.asset_id
    left join asset digitalProductsAsset on digitalProductsAsset.asset_id = rc2.asset_id
    left join attribute synonymsAtt on
        synonymsAtt.asset_id = a.asset_id and
        not synonymsAtt.deleted_flag and
        synonymsAtt.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'
    Where
        (:businessTermName is null or :businessTermName = a.asset_displayname) and
        (:businessTermTechnicalName is null or :businessTermTechnicalName = technicalNameAtt.value)
    group by a.asset_id, a.asset_displayname, technicalNameAtt.value, definitionAtt.value
  """, countQuery = """
    Select count(q.*)
    From (
      select
          a.asset_id as id
      from asset a
      inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%'
    inner join attribute definitionAtt on
        definitionAtt.asset_id = a.asset_id and
        not definitionAtt.deleted_flag and
        definitionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join responsibility resp on
        resp.asset_id = a.asset_id and resp.role_id = '00000000-0000-0000-0000-000000005040' and not resp.deleted_flag
    inner join "user" u on u.user_id = resp.user_id
    Where
        (:businessTermName is null or :businessTermName = a.asset_displayname) and
        (:businessTermTechnicalName is null or :businessTermTechnicalName = technicalNameAtt.value)
    ) q
  """, nativeQuery = true)
  Page<BusinessTermV2> findAllBusinessTermsPageableV2 (
    @Param("businessTermName") String businessTermName,
    @Param("businessTermTechnicalName") String businessTermTechnicalName,
    Pageable pageable
  );

  @Query(value = """
    select
        cast(a.asset_id as text) as idText,
        a.asset_displayname as name,
        technicalNameAtt.value as technicalName,
        descriptionAtt.value as description,
        u.username as ownerLdap,
        string_agg(synonymsAtt.value, '--') as synonyms
    from asset a
    inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%' and
        a.asset_id = :businessTermId
    inner join attribute descriptionAtt on
        descriptionAtt.asset_id = a.asset_id and
        not descriptionAtt.deleted_flag and
        descriptionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join responsibility resp on
        resp.asset_id = a.asset_id and resp.role_id = '00000000-0000-0000-0000-000000005040' and not resp.deleted_flag
    inner join "user" u on u.user_id = resp.user_id
    left join attribute synonymsAtt on
        synonymsAtt.asset_id = a.asset_id and
        not synonymsAtt.deleted_flag and
        synonymsAtt.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'
    group by a.asset_id, a.asset_displayname, technicalNameAtt.value, descriptionAtt.value, u.username
  """, nativeQuery = true)
  Optional<BusinessTermV1> findBusinessTermByIdV1 (
    @Param("businessTermId") UUID businessTermId
  );

  @Query(value = """
    select
        cast(a.asset_id as text) as idText,
        a.asset_displayname as name,
        technicalNameAtt.value as technicalName,
        descriptionAtt.value as definition,
        string_agg(u.username, ',') as ownerLdaps,
        string_agg(digitalProductsAsset.asset_name, ',') as digitalProducts,
        string_agg(synonymsAtt.value, ',') as synonyms
    from asset a
    inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%' and
        a.asset_id = :businessTermId
    inner join attribute descriptionAtt on
        descriptionAtt.asset_id = a.asset_id and
        not descriptionAtt.deleted_flag and
        descriptionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join responsibility resp on
        resp.asset_id = a.asset_id and resp.role_id = '00000000-0000-0000-0000-000000005040' and not resp.deleted_flag
    inner join "user" u on u.user_id = resp.user_id
    left join relation_component rc on
        rc.asset_id = a.asset_id and
        rc.relation_type_component_id = 'd2065e8d-a108-4a22-a562-00281041f714' and
        not rc.deleted_flag
    left join relation_component rc2 on
        rc.relation_id = rc2.relation_id and
        rc.asset_id != rc2.asset_id
    left join asset digitalProductsAsset on digitalProductsAsset.asset_id = rc2.asset_id
    left join attribute synonymsAtt on
        synonymsAtt.asset_id = a.asset_id and
        not synonymsAtt.deleted_flag and
        synonymsAtt.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'
    group by a.asset_id, a.asset_displayname, technicalNameAtt.value, descriptionAtt.value
  """, nativeQuery = true)
  Optional<BusinessTermV2> findBusinessTermByIdV2 (
    @Param("businessTermId") UUID businessTermId
  );

  @Query(value = """
    select count(technicalNameAtt.attribute_id) > 0
    from asset a
    inner join attribute technicalNameAtt on
        technicalNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not technicalNameAtt.deleted_flag and
        technicalNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '00000000-0000-0000-0000-000000011001' and
        a.asset_name like 't__%' and
        a.asset_id = :businessTermId
  """, nativeQuery = true)
  Boolean isBusinessTermExists (
    @Param("businessTermId") UUID businessTermId
  );

  @Query(value = """
    with businessAttributeList as
    (
        select secondRc.asset_id
        from relation_component firstRc
        inner join relation_component secondRc on
            firstRc.relation_id = secondRc.relation_id and
            not firstRc.deleted_flag and
            secondRc.relation_type_component_id = 'ba9fc8b6-3a3b-46d3-ab61-7d5ed64aa94a' and
            firstRc.asset_id = :businessTermId
    )

    select
        cast(a.asset_id as text) as baIdText,
        a.asset_displayname as baName,
        techNameAtt.value as baTechName,
        definitionAtt.value as baDefinition,
        dataTypeAtt.value as baDataType,
        split_part(confidentialityAtt.value,' - ', 2) as baConfidentiality,
        primayKeyAtt.value as baPrimaryKeyValue,
        string_agg(synonymsAtt.value, '--') as btSynonyms
    from businessAttributeList bal
    inner join asset a on a.asset_id = bal.asset_id
    inner join attribute techNameAtt on
        techNameAtt.asset_id = a.asset_id and
        not a.deleted_flag and
        not techNameAtt.deleted_flag and
        techNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
        a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
        a.asset_type_id = '69c28f07-defe-46e7-ae63-f4d6eb7c8df8' and
        a.asset_name like 'a__%'
    inner join attribute definitionAtt on
        definitionAtt.asset_id = a.asset_id and
        not definitionAtt.deleted_flag and
        definitionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
    inner join attribute dataTypeAtt on
        dataTypeAtt.asset_id = a.asset_id and
        not dataTypeAtt.deleted_flag and
        dataTypeAtt.attribute_type_id = '7bf4bf75-189f-4d1f-90fb-52e4e35da5ed'
    left join attribute confidentialityAtt on
        confidentialityAtt.asset_id = a.asset_id and
        not confidentialityAtt.deleted_flag and
        confidentialityAtt.attribute_type_id = 'eb633e06-cdd3-43b4-bdcb-9a31859211fe'
    left join attribute primayKeyAtt on
        primayKeyAtt.asset_id = a.asset_id and
        not primayKeyAtt.deleted_flag and
        primayKeyAtt.attribute_type_id = '8e7dbcfd-925a-48d6-a7e8-e48cbb854255'
    left join attribute synonymsAtt on
        synonymsAtt.asset_id = a.asset_id and
        not synonymsAtt.deleted_flag and
        synonymsAtt.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'
    where
        (:businessAttributeName is null or a.asset_displayname = :businessAttributeName) and
        (:businessAttributeDataType is null or dataTypeAtt.value = :businessAttributeDataType) and
        (:businessAttributeTechnicalName is null or techNameAtt.value = :businessAttributeTechnicalName) and
        (:businessAttributeConfidentiality is null or split_part(confidentialityAtt.value,' - ', 2) = :businessAttributeConfidentiality)
    group by bal.asset_id, baIdText, baName, baTechName, baDefinition, baDataType, baConfidentiality, baPrimaryKeyValue
  """, countQuery = """
    with businessAttributeList as
    (
        select secondRc.asset_id
        from relation_component firstRc
        inner join relation_component secondRc on
            firstRc.relation_id = secondRc.relation_id and
            not firstRc.deleted_flag and
            secondRc.relation_type_component_id = 'ba9fc8b6-3a3b-46d3-ab61-7d5ed64aa94a' and
            firstRc.asset_id = :businessTermId
    )
    Select count(q.*)
    From (
      select a.asset_id
      from businessAttributeList bal
      inner join asset a on a.asset_id = bal.asset_id
      inner join attribute techNameAtt on
          techNameAtt.asset_id = a.asset_id and
          not a.deleted_flag and
          not techNameAtt.deleted_flag and
          techNameAtt.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
          a.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and
          a.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74' and
          a.asset_type_id = '69c28f07-defe-46e7-ae63-f4d6eb7c8df8' and
          a.asset_name like 'a__%'
      inner join attribute definitionAtt on
          definitionAtt.asset_id = a.asset_id and
          not definitionAtt.deleted_flag and
          definitionAtt.attribute_type_id = '00000000-0000-0000-0000-000000000202'
      inner join attribute dataTypeAtt on
          dataTypeAtt.asset_id = a.asset_id and
          not dataTypeAtt.deleted_flag and
          dataTypeAtt.attribute_type_id = '7bf4bf75-189f-4d1f-90fb-52e4e35da5ed'
      left join attribute confidentialityAtt on
          confidentialityAtt.asset_id = a.asset_id and
          not confidentialityAtt.deleted_flag and
          confidentialityAtt.attribute_type_id = 'eb633e06-cdd3-43b4-bdcb-9a31859211fe'
      where
          (:businessAttributeName is null or a.asset_displayname = :businessAttributeName) and
          (:businessAttributeDataType is null or dataTypeAtt.value = :businessAttributeDataType) and
          (:businessAttributeTechnicalName is null or techNameAtt.value = :businessAttributeTechnicalName) and
          (:businessAttributeConfidentiality is null or split_part(confidentialityAtt.value,' - ', 2) = :businessAttributeConfidentiality)
    ) q
  """, nativeQuery = true)
  Page<BusinessTermAttribute> findAllBusinessTermAttributesPageable (
    @Param("businessTermId") UUID businessTermId,
    @Param("businessAttributeName") String businessAttributeName,
    @Param("businessAttributeDataType") String businessAttributeDataType,
    @Param("businessAttributeTechnicalName") String businessAttributeTechnicalName,
    @Param("businessAttributeConfidentiality") String businessAttributeConfidentiality,
    Pageable pageable
  );

  @Query(value = """
    select
        cast(firstRelComp.asset_id as text) as businessTermIdText,
        cast(firstRelComp.relation_id as text) as businessTermRelationIdText,
        cast(secondRelComp.asset_id as text) as relatedBusinessTermIdText,
        concat(firstRelCardinalRca.value, '->', secondRelCardinalRca.value) as businessTermRelationCardinality,
        relNameRca.value as businessTermRelationshipName,
        relTechNameRca.value as businessTermRelationshipTechnicalName
    from
        relation_component firstRelComp
    inner join relation_component secondRelComp on
        firstRelComp.relation_id = secondRelComp.relation_id and
        not firstRelComp.deleted_flag and
        firstRelComp.asset_id = :businessTermId and
        firstRelComp.relation_component_id != secondRelComp.relation_component_id
    inner join relation r on
        r.relation_id = firstRelComp.relation_id and
        r.relation_type_id = 'bea11f4f-8e24-4383-bfc3-1d7b8f1cf16e'
    inner join relation_component_attribute firstRelCardinalRca on
        firstRelCardinalRca.relation_component_id = firstRelComp.relation_component_id and
        firstRelCardinalRca.attribute_type_id = '89b7bded-6a7c-43d9-bcbb-ca240c86749b' and
        not firstRelCardinalRca.deleted_flag
    inner join relation_component_attribute secondRelCardinalRca on
        secondRelCardinalRca.relation_component_id = secondRelComp.relation_component_id and
        secondRelCardinalRca.attribute_type_id = '89b7bded-6a7c-43d9-bcbb-ca240c86749b' and
        not secondRelCardinalRca.deleted_flag
    inner join relation_component_attribute relNameRca on
        not relNameRca.deleted_flag and
        relNameRca.attribute_type_id = '98bc435c-2b50-4fc6-85bd-0ebf200ff439' and
        relNameRca.relation_component_id = firstRelComp.relation_component_id
    inner join relation_component_attribute relTechNameRca on
        not relTechNameRca.deleted_flag and
        relTechNameRca.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        relTechNameRca.relation_component_id = firstRelComp.relation_component_id
    inner join public.asset secondRelCompAsset 
         on secondRelComp.asset_id = secondRelCompAsset.asset_id and 
         secondRelCompAsset.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and 
         secondRelCompAsset.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74'
    WHERE
        (:businessTermRelationCardinality is null or :businessTermRelationCardinality = concat(firstRelCardinalRca.value, '->', secondRelCardinalRca.value)) and
        (:businessTermRelationshipName is null or :businessTermRelationshipName = relNameRca.value) and
        (:businessTermRelationshipTechnicalName is null or :businessTermRelationshipTechnicalName = relTechNameRca.value)
  """, countQuery = """
    select count(firstRelComp.asset_id)
    from
        relation_component firstRelComp
    inner join relation_component secondRelComp on
        firstRelComp.relation_id = secondRelComp.relation_id and
        not firstRelComp.deleted_flag and
        firstRelComp.asset_id = :businessTermId and
        firstRelComp.relation_component_id != secondRelComp.relation_component_id
    inner join relation r on
        r.relation_id = firstRelComp.relation_id and
        r.relation_type_id = 'bea11f4f-8e24-4383-bfc3-1d7b8f1cf16e'
    inner join relation_component_attribute firstRelCardinalRca on
        firstRelCardinalRca.relation_component_id = firstRelComp.relation_component_id and
        firstRelCardinalRca.attribute_type_id = '89b7bded-6a7c-43d9-bcbb-ca240c86749b' and
        not firstRelCardinalRca.deleted_flag
    inner join relation_component_attribute secondRelCardinalRca on
        secondRelCardinalRca.relation_component_id = secondRelComp.relation_component_id and
        secondRelCardinalRca.attribute_type_id = '89b7bded-6a7c-43d9-bcbb-ca240c86749b' and
        not secondRelCardinalRca.deleted_flag
    inner join relation_component_attribute relNameRca on
        not relNameRca.deleted_flag and
        relNameRca.attribute_type_id = '98bc435c-2b50-4fc6-85bd-0ebf200ff439' and
        relNameRca.relation_component_id = firstRelComp.relation_component_id
    inner join relation_component_attribute relTechNameRca on
        not relTechNameRca.deleted_flag and
        relTechNameRca.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' and
        relTechNameRca.relation_component_id = firstRelComp.relation_component_id
    inner join public.asset secondRelCompAsset 
         on secondRelComp.asset_id = secondRelCompAsset.asset_id and 
         secondRelCompAsset.lifecycle_status = '17d55ff5-9659-4151-9ef8-4e2886f54dd5' and 
         secondRelCompAsset.stewardship_status = '55a86990-84bd-4c95-af43-eb015224ba74'
    WHERE
        (:businessTermRelationCardinality is null or :businessTermRelationCardinality = concat(firstRelCardinalRca.value, '->', secondRelCardinalRca.value)) and
        (:businessTermRelationshipName is null or :businessTermRelationshipName = relNameRca.value) and
        (:businessTermRelationshipTechnicalName is null or :businessTermRelationshipTechnicalName = relTechNameRca.value)
  """, nativeQuery = true)
  Page<BusinessRelationship> findAllBusinessTermRelationshipsPageable (
    @Param("businessTermId") UUID businessTermId,
    @Param("businessTermRelationshipName") String businessTermRelationshipName,
    @Param("businessTermRelationCardinality") String businessTermRelationCardinality,
    @Param("businessTermRelationshipTechnicalName") String businessTermRelationshipTechnicalName,
    Pageable pageable
  );
}
