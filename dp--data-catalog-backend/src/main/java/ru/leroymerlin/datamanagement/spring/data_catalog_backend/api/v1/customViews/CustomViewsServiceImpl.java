package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.models.CustomViewWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.CustomView;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewQueryDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.DroppingTableException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.ErrorWhileParsingJsonException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewTableColumnName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.Operation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.QueryType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;

/**
 * @author juliwolf
 */

@Service
public class CustomViewsServiceImpl extends CustomViewsDAO implements CustomViewsService {
  private final AssetTypesDAO assetTypesDAO;

  private final RolesDAO rolesDAO;

  private final DataSource dataSource;

  private String tableNamesPattern = null;

  private final String TABLE_SELECT_QUERY_PATTERN = ".*select[.\\s\\S]*from[.\\s\\S]*order by.*";
  private final String HEADER_SELECT_QUERY_PATTERN = ".*select[.\\s\\S]*from[.\\s\\S]*limit 1$";

  public CustomViewsServiceImpl (AssetTypesDAO assetTypesDAO, RolesDAO rolesDAO, DataSource dataSource) {
    this.assetTypesDAO = assetTypesDAO;
    this.rolesDAO = rolesDAO;
    this.dataSource = dataSource;
  }

  @Override
  public PostCustomViewResponse createCustomView (
    PostCustomViewRequest customViewRequest,
    User user
  ) throws
    RoleNotFoundException,
    JsonProcessingException,
    AssetTypeNotFoundException,
    CustomViewQueryDoesNotMatchPatternException
  {
    AssetType assetType = assetTypesDAO.findAssetTypeById(UUID.fromString(customViewRequest.getAsset_type_id()));
    Role role = StringUtils.isNotEmpty(customViewRequest.getRole_id())
      ? rolesDAO.findRoleById(UUID.fromString(customViewRequest.getRole_id()))
      : null;

    if (StringUtils.isNotEmpty(customViewRequest.getTable_select_query())) {
      validateQueryByPattern(customViewRequest.getTable_select_query(), TABLE_SELECT_QUERY_PATTERN, QueryType.TABLE);
      validateIfQueryDropMainTable(customViewRequest.getTable_select_query());
    }

    if (StringUtils.isNotEmpty(customViewRequest.getHeader_select_query())) {
      validateQueryByPattern(customViewRequest.getHeader_select_query(), HEADER_SELECT_QUERY_PATTERN, QueryType.HEADER);
      validateIfQueryDropMainTable(customViewRequest.getHeader_select_query());
    }

    // If exists one of query then should exist another
    if (StringUtils.isNotEmpty(customViewRequest.getTable_prepare_query())) {
      validateIfQueryDropMainTable(customViewRequest.getTable_prepare_query());
      validateIfQueryDropMainTable(customViewRequest.getTable_clear_query());
    }

    // If exists one of query then should exist another
    if (StringUtils.isNotEmpty(customViewRequest.getHeader_prepare_query())) {
      validateIfQueryDropMainTable(customViewRequest.getHeader_prepare_query());
      validateIfQueryDropMainTable(customViewRequest.getHeader_clear_query());
    }

    checkHeaderRowNames(customViewRequest.getHeader_row_names());
    checkTableColumnNames(customViewRequest.getTable_column_names());

    ObjectMapper objectMapper = new ObjectMapper();
    String headerRowNames = objectMapper.writeValueAsString(customViewRequest.getHeader_row_names());
    String tableRowNames = objectMapper.writeValueAsString(customViewRequest.getTable_column_names());

    CustomView customView = customViewRepository.save(new CustomView(
      assetType,
      customViewRequest.getCustom_view_name(),
      headerRowNames,
      customViewRequest.getHeader_prepare_query(),
      customViewRequest.getHeader_select_query(),
      customViewRequest.getHeader_clear_query(),
      tableRowNames,
      customViewRequest.getTable_prepare_query(),
      customViewRequest.getTable_select_query(),
      customViewRequest.getTable_clear_query(),
      role,
      user
    ));

    return new PostCustomViewResponse(
      customView.getCustomViewId(),
      customView.getAssetType().getAssetTypeId(),
      customView.getCustomViewName(),
      customView.getRoleUUID(),
      customViewRequest.getHeader_row_names(),
      customView.getHeaderPrepareQuery(),
      customView.getHeaderSelectQuery(),
      customView.getHeaderClearQuery(),
      customViewRequest.getTable_column_names(),
      customView.getTablePrepareQuery(),
      customView.getTableSelectQuery(),
      customView.getTableClearQuery(),
      customView.getCreatedOn(),
      user.getUserId()
    );
  }

  @Override
  public PatchCustomViewResponse updateCustomView (
    UUID customViewId,
    PatchCustomViewRequest customViewRequest,
    User user
  ) throws
    RoleNotFoundException,
    JsonProcessingException,
    CustomViewNotFoundException
  {
    CustomView customView = findCustomViewById(customViewId);

    Optional<List<CustomViewHeaderRowName>> headerRowNames = OptionalUtils.getOptionalFromField(customViewRequest.getHeader_row_names());
    Optional<List<CustomViewTableColumnName>> tableColumnNames = OptionalUtils.getOptionalFromField(customViewRequest.getTable_column_names());

    checkHeaderRowNames(headerRowNames.orElse(null));
    checkTableColumnNames(tableColumnNames.orElse(null));

    boolean isTableQueryEmpty = StringUtils.isEmpty(customView.getTableSelectQuery());
    boolean isRequestTableQueryEmpty = customViewRequest.getTable_select_query() == null;

    boolean isHeaderQueryEmpty = StringUtils.isEmpty(customView.getHeaderSelectQuery());
    boolean isRequestHeaderQueryEmpty = customViewRequest.getHeader_select_query() == null;

    if (
      (isHeaderQueryEmpty && OptionalUtils.isEmpty(customViewRequest.getTable_select_query()) && isRequestHeaderQueryEmpty) ||
      (isTableQueryEmpty && OptionalUtils.isEmpty(customViewRequest.getHeader_select_query()) && isRequestTableQueryEmpty)
    ) {
      throw new SomeRequiredFieldsAreEmptyException();
    }

    ObjectMapper objectMapper = new ObjectMapper();
    String headerRowNamesString = objectMapper.writeValueAsString(headerRowNames.orElse(new ArrayList<>()));
    String tableRowNamesString = objectMapper.writeValueAsString(tableColumnNames.orElse(new ArrayList<>()));

    OptionalUtils.doActionIfPresent(customViewRequest.getHeader_select_query(), selectQuery -> updateSelectQuery(selectQuery, HEADER_SELECT_QUERY_PATTERN, QueryType.HEADER, customView::setHeaderSelectQuery));
    OptionalUtils.doActionIfPresent(customViewRequest.getHeader_prepare_query(), prepareQuery -> updatePrepareOrClearQuery(prepareQuery, customView::setHeaderPrepareQuery));
    OptionalUtils.doActionIfPresent(customViewRequest.getHeader_clear_query(), clearQuery -> updatePrepareOrClearQuery(clearQuery, customView::setHeaderClearQuery));
    OptionalUtils.doActionIfPresent(customViewRequest.getHeader_row_names(), headerRowNamesValue -> customView.setHeaderRowNames(headerRowNamesString));

    OptionalUtils.doActionIfPresent(customViewRequest.getTable_select_query(), selectQuery -> updateSelectQuery(selectQuery, TABLE_SELECT_QUERY_PATTERN, QueryType.TABLE, customView::setTableSelectQuery));
    OptionalUtils.doActionIfPresent(customViewRequest.getTable_prepare_query(), prepareQuery -> updatePrepareOrClearQuery(prepareQuery, customView::setTablePrepareQuery));
    OptionalUtils.doActionIfPresent(customViewRequest.getTable_clear_query(), clearQuery -> updatePrepareOrClearQuery(clearQuery, customView::setTableClearQuery));
    OptionalUtils.doActionIfPresent(customViewRequest.getTable_column_names(), tableColumnNamesValue -> customView.setTableColumnNames(tableRowNamesString));

    OptionalUtils.doActionIfPresent(customViewRequest.getRole_id(), roleIdValue -> {
      Role role = null;
      String roleId = roleIdValue.orElse(null);

      if (roleIdValue.isPresent()) {
        role = rolesDAO.findRoleById(UUID.fromString(roleId));
      }

      customView.setRole(role);
    });

    if (StringUtils.isNotEmpty(customViewRequest.getCustom_view_name())) {
      customView.setCustomViewName(customViewRequest.getCustom_view_name());
    }

    customView.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    customView.setLastModifiedBy(user);

    CustomView updatedCustomView = customViewRepository.save(customView);

    return new PatchCustomViewResponse(
      updatedCustomView.getCustomViewId(),
      updatedCustomView.getAssetType().getAssetTypeId(),
      updatedCustomView.getCustomViewName(),
      updatedCustomView.getRoleUUID(),
      objectMapper.readValue(updatedCustomView.getHeaderRowNames(), List.class),
      updatedCustomView.getHeaderPrepareQuery(),
      updatedCustomView.getHeaderSelectQuery(),
      updatedCustomView.getHeaderClearQuery(),
      objectMapper.readValue(updatedCustomView.getTableColumnNames(), List.class),
      updatedCustomView.getTablePrepareQuery(),
      updatedCustomView.getTableSelectQuery(),
      updatedCustomView.getTableClearQuery(),
      updatedCustomView.getCreatedOn(),
      updatedCustomView.getCreatedByUUID(),
      updatedCustomView.getLastModifiedOn(),
      updatedCustomView.getLastModifiedByUUID()
    );
  }

  @Override
  public GetCustomViewsResponse getCustomViewsByParams (
    UUID roleId,
    UUID assetTypeId,
    String customViewName,
    Integer pageNumber,
    Integer pageSize
  ) throws ErrorWhileParsingJsonException {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<CustomViewWithConnectedValues> customViews = customViewRepository.findAllByParamsPageable(
      roleId,
      assetTypeId,
      customViewName,
      PageRequest.of(pageNumber, pageSize, Sort.by("customViewName").ascending())
    );

    List<GetCustomViewResponse> response = customViews.stream().map(customView -> {
      try {
        return new GetCustomViewResponse(customView);
      } catch (JsonProcessingException e) {
        throw new ErrorWhileParsingJsonException();
      }
    }).toList();

    return new GetCustomViewsResponse(
      customViews.getTotalElements(),
      pageSize,
      pageNumber,
      response
    );
  }

  @Override
  public GetCustomViewResponse getCustomViewById (UUID customViewId) throws ErrorWhileParsingJsonException {
    Optional<CustomViewWithConnectedValues> optionalCustomView = customViewRepository.findByIdWithJoinedTables(customViewId);
    
    if (optionalCustomView.isEmpty()) {
      throw new CustomViewNotFoundException();
    }

    try {
      return new GetCustomViewResponse(optionalCustomView.get());
    } catch (JsonProcessingException e) {
      throw new ErrorWhileParsingJsonException();
    }
  }

  @Override
  public void deleteCustomViewById (UUID customViewId, User user) throws CustomViewNotFoundException {
    CustomView customView = findCustomViewById(customViewId);

    customView.setIsDeleted(true);
    customView.setDeletedBy(user);
    customView.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    customViewRepository.save(customView);
  }

  private void checkHeaderRowNames (List<CustomViewHeaderRowName> headerRowNames) throws SomeRequiredFieldsAreEmptyException {
    if (headerRowNames == null) return;

    headerRowNames.forEach(item -> {
      if (item.getRow_kind() == null || StringUtils.isEmpty(item.getRow_name())) {
        throw new SomeRequiredFieldsAreEmptyException();
      }
    });
  }

  private void checkTableColumnNames (List<CustomViewTableColumnName> tableColumnNames) throws SomeRequiredFieldsAreEmptyException {
    if (tableColumnNames == null) return;

    tableColumnNames.forEach(item -> {
      if (item.getColumn_kind() == null || StringUtils.isEmpty(item.getColumn_name())) {
        throw new SomeRequiredFieldsAreEmptyException();
      }
    });
  }

  private void updateSelectQuery (Optional<String> optionalQuery, String pattern, QueryType queryType, Operation action) {
    String query = optionalQuery.orElse(null);

    if (StringUtils.isEmpty(query)) return;

    validateQueryByPattern(query, pattern, queryType);
    validateIfQueryDropMainTable(query);
    action.execute(query);
  }

  private void updatePrepareOrClearQuery (Optional<String> optionalQuery, Operation action) {
    String query = optionalQuery.orElse(null);

    if (StringUtils.isEmpty(query)) return;

    validateIfQueryDropMainTable(query);
    action.execute(query);
  }

  private void validateQueryByPattern (String query, String pattern, QueryType queryType) {
    if (StringUtils.isEmpty(query)) return;

    Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(query.toLowerCase());

    if (matcher.find()) return;

    throw new CustomViewQueryDoesNotMatchPatternException(queryType, pattern);
  }

  private void validateIfQueryDropMainTable (String query) {
    try {
      loadTableNames();
    } catch (SQLException sqlException) {
      throw new DroppingTableException();
    }

    String regex = "DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(`|\")?(" + tableNamesPattern + ")(`|\")?";

    Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(query);

    if (!matcher.find()) return;

    throw new DroppingTableException();
  }

  private void loadTableNames () throws SQLException {
    if (StringUtils.isNotEmpty(tableNamesPattern)) return;

    List<String> tableNames = new ArrayList<>();

    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});

      while (resultSet.next()) {
        String tableName = resultSet.getString("TABLE_NAME");

        tableNames.add(tableName);
      }
    }

    tableNamesPattern = tableNames.stream()
      .map(Pattern::quote)
      .collect(Collectors.joining("|"));
  }
}
