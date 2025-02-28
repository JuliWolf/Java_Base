package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.CustomView;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewHeaderQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewTableQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewHeaderRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewTableRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.CustomViewsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;

/**
 * @author juliwolf
 */

@Service
public class AssetCustomViewServiceImpl implements AssetCustomViewService {
  private final Integer PAGE_SIZE = 50;

  @Autowired
  private CustomViewsDAO customViewsDAO;

  @Autowired
  private AssetsDAO assetsDAO;

  @Autowired
  private EntityManager entityManager;

  @Override
  public GetAssetCustomViewHeaderRows getAssetCustomViewHeaderRows (
    UUID assetId,
    UUID customViewId
  ) throws
    JsonProcessingException,
    CustomViewHeaderQueryIsEmptyException
  {
    Asset asset = assetsDAO.findAssetById(assetId);
    CustomView customView = customViewsDAO.findCustomViewById(customViewId);

    if (StringUtils.isEmpty(customView.getHeaderSelectQuery())) {
      throw new CustomViewHeaderQueryIsEmptyException();
    }

    List<Object[]> resultList = executeQuery(customView.getHeaderPrepareQuery(), customView.getHeaderSelectQuery(), customView.getHeaderClearQuery(), asset.getAssetId());
    ObjectMapper objectMapper = new ObjectMapper();
    TypeReference<List<CustomViewHeaderRowName>> typeRef = new TypeReference<>() {};
    List<CustomViewHeaderRowName> list = objectMapper.readValue(customView.getHeaderRowNames(), typeRef);

    return new GetAssetCustomViewHeaderRows(
      asset.getAssetId(),
      resultList,
      list
    );
  }

  @Override
  @Transactional
  public GetAssetCustomViewTableRows getAssetCustomViewTableRows (
    UUID assetId,
    UUID customViewId,
    Integer pageNumber,
    Integer pageSize
  ) throws AssetNotFoundException, CustomViewNotFoundException {
    pageSize = PageableUtils.getPageSize(pageSize, PAGE_SIZE);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Asset asset = assetsDAO.findAssetById(assetId);
    CustomView customView = customViewsDAO.findCustomViewById(customViewId);

    if (StringUtils.isEmpty(customView.getTableSelectQuery())) {
      throw new CustomViewTableQueryIsEmptyException();
    }

    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
    List<Object[]> resultList = executeQuery(customView.getTablePrepareQuery(), customView.getTableSelectQuery(), customView.getTableClearQuery(), asset.getAssetId());

    return new GetAssetCustomViewTableRows(
      pageRequest,
      resultList
    );
  }

  private List<Object[]> executeQuery (String prepareQuery, String selectQuery, String clearQuery, UUID assetId) {
    if (StringUtils.isNotEmpty(prepareQuery)) {
      Query prepareStatement = entityManager.createNativeQuery(prepareQuery);
      setAssetIdParameter(prepareQuery, prepareStatement, assetId);
      prepareStatement.executeUpdate();
    }

    Query selectStatement = entityManager.createNativeQuery(selectQuery);
    selectStatement.setParameter("assetId", assetId);
    List<Object[]> resultList = selectStatement.getResultList();

    if (StringUtils.isNotEmpty(clearQuery)) {
      Query clearStatement = entityManager.createNativeQuery(clearQuery);
      setAssetIdParameter(clearQuery, clearStatement, assetId);
      clearStatement.executeUpdate();
    }

    return resultList;
  }

  private void setAssetIdParameter (String query, Query statement, UUID assetId) {
    boolean containsAssetId = query.contains(":assetId");

    if (!containsAssetId) return;

    statement.setParameter("assetId", assetId);
  }
}
