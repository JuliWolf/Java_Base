package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import logger.LoggerWrapper;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions.ExtractJobNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions.WrongJobStatusException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author juliwolf
 */

public class JobsServiceTest extends ServiceWithUserIntegrationTest {
  private final JobsServiceImpl jobsService;
  private final ExtractJobRepository extractJobRepository;

  private final UserRepository userRepository;
  private final AssetRepository assetRepository;
  private final RoleRepository roleRepository;
  private final RelationRepository relationRepository;
  private final RelationComponentRepository relationComponentRepository;
  private final AssetTypeRepository assetTypeRepository;
  private final AttributeRepository attributeRepository;
  private final AttributeTypeRepository attributeTypeRepository;
  private final RelationTypeRepository relationTypeRepository;
  private final ResponsibilityRepository responsibilityRepository;
  private final RelationTypeComponentRepository relationTypeComponentRepository;
  private final AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  private final RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  private final StageAssetRepository stageAssetRepository;
  private final StageAttributeRepository stageAttributeRepository;
  private final StageRelationRepository stageRelationRepository;
  private final StageResponsibilityRepository stageResponsibilityRepository;

  AssetType assetTypeOne = null;
  AssetType assetTypeTwo = null;

  Asset assetOne;
  Asset assetTwo;

  JobsServiceUtils jobsServiceUtils;

  @Autowired
  public JobsServiceTest (
    JobsServiceImpl jobsService,
    ExtractJobRepository extractJobRepository,
    UserRepository userRepository, AssetRepository assetRepository,
    RoleRepository roleRepository, RelationRepository relationRepository,
    RelationComponentRepository relationComponentRepository,
    AssetTypeRepository assetTypeRepository,
    AttributeRepository attributeRepository,
    AttributeTypeRepository attributeTypeRepository,
    RelationTypeRepository relationTypeRepository,
    ResponsibilityRepository responsibilityRepository,
    RelationTypeComponentRepository relationTypeComponentRepository,
    AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository,
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository,
    StageAssetRepository stageAssetRepository,
    StageAttributeRepository stageAttributeRepository,
    StageRelationRepository stageRelationRepository,
    StageResponsibilityRepository stageResponsibilityRepository
  ) {
    this.jobsService = jobsService;
    this.extractJobRepository = extractJobRepository;
    this.userRepository = userRepository;
    this.assetRepository = assetRepository;
    this.roleRepository = roleRepository;
    this.relationRepository = relationRepository;
    this.relationComponentRepository = relationComponentRepository;
    this.assetTypeRepository = assetTypeRepository;
    this.attributeRepository = attributeRepository;
    this.attributeTypeRepository = attributeTypeRepository;
    this.relationTypeRepository = relationTypeRepository;
    this.responsibilityRepository = responsibilityRepository;
    this.relationTypeComponentRepository = relationTypeComponentRepository;
    this.assetTypeAttributeTypeAssignmentRepository = assetTypeAttributeTypeAssignmentRepository;
    this.relationTypeComponentAssetTypeAssignmentRepository = relationTypeComponentAssetTypeAssignmentRepository;
    this.stageAssetRepository = stageAssetRepository;
    this.stageAttributeRepository = stageAttributeRepository;
    this.stageRelationRepository = stageRelationRepository;
    this.stageResponsibilityRepository = stageResponsibilityRepository;

    User extractUser = this.userRepository.save(new User("extract user", "firstname", "last name", ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType.API, "someEmail@gcom.ru"));

    this.jobsServiceUtils = new JobsServiceUtils(
      stageAssetRepository,
      stageAttributeRepository,
      stageRelationRepository,
      stageResponsibilityRepository,
      assetTypeRepository,
      assetRepository,
      roleRepository,
      relationRepository,
      attributeRepository,
      relationTypeRepository,
      attributeTypeRepository,
      responsibilityRepository,
      relationComponentRepository,
      relationTypeComponentRepository,
      assetTypeAttributeTypeAssignmentRepository,
      relationTypeComponentAssetTypeAssignmentRepository,
      extractUser,
      language
    );
  }
  
  @BeforeEach
  public void prepareConnectedValues () {
    assetTypeOne = assetTypeRepository.save(new AssetType("first asset type", "first asset Type description", "fat", "red", language, null));
    assetTypeTwo = assetTypeRepository.save(new AssetType("second asset type", "second asset Type description", "sat", "blue", language, null));

    assetOne = assetRepository.save(new Asset("firs asset", assetTypeOne, "displayed name", language, null, null, user));
    assetTwo = assetRepository.save(new Asset("second asset", assetTypeTwo, "displayed name", language, null, null, user));
  }

  @AfterEach
  @BeforeAll
  public void clearData () {
    LoggerWrapper.info("Clear Data");
    extractJobRepository.deleteAll();

    stageAssetRepository.deleteAll();
    stageAttributeRepository.deleteAll();
    stageRelationRepository.deleteAll();
    stageResponsibilityRepository.deleteAll();

    responsibilityRepository.deleteAll();
    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationRepository.deleteAll();
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();

    roleRepository.deleteAll();
    attributeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @Test
  @Order(1)
  public void extractJobsSuccessIntegrationTest () {
    ExtractJob successJob = extractJobRepository.save(new ExtractJob("Some topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "new status", JobStatus.SUCCESS, assetOne, "{\"criteria\":\"value\"}", true));
    ExtractJob failedJob = new ExtractJob("fail topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "new status", JobStatus.DUMPED_DB, assetOne, "{\"criteria\":\"value\"}", true);
    failedJob.setJobErrorFlag(true);
    extractJobRepository.save(failedJob);

    ExtractJob activeJob = extractJobRepository.save(new ExtractJob("another topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "new status", JobStatus.DUMPED_DB, assetTwo, "{\"criteria\":\"value\"}", false));

    assertEquals(1, jobsService.extractJobs().getJobs().size());
  }

  @Test
  @Order(2)
  public void executeUpdateJobNotFoundIntegrationTest (){
    assertThrows(ExtractJobNotFoundException.class, () -> jobsService.executeUpdate(new PostExecuteUpdateRequest(UUID.randomUUID()), user));
  }

  @Test
  @Order(3)
  public void executeUpdateJobIsNotStageCompleteIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.NEW, null, null, null));

    assertThrows(WrongJobStatusException.class, () -> jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user));
  }

  @Test
  @Order(4)
  public void executeUpdateCompletableFutureTimeoutExceededIntegrationTest () {
    jobsService.setTIMEOUT_MINUTES(0);
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));
    this.jobsServiceUtils.prepareStageAssets(extractJob, 10, ActionDecision.I);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);

    assertAll(
      () -> assertEquals(0, response.getInserted().getAssets(), "inserted assets"),
      () -> assertEquals(10, response.getError().getAssets(), "error assets")
    );

    jobsService.setTIMEOUT_MINUTES(40);
  }

  @Test
  @Order(5)
  public void executeUpdateCreateAllSuccessIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));
    this.jobsServiceUtils.prepareStageAssets(extractJob, 100, ActionDecision.I);
    this.jobsServiceUtils.prepareStageAttributes(extractJob, 100, ActionDecision.I);
    this.jobsServiceUtils.prepareStageRelations(extractJob, 100, ActionDecision.I);
    this.jobsServiceUtils.prepareStageResponsibilities(extractJob, 100, ActionDecision.I);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);
    List<StageAsset> stageAssets = stageAssetRepository.findAll();
    List<StageRelation> stageRelations = stageRelationRepository.findAll();
    List<StageAttribute> stageAttributes = stageAttributeRepository.findAll();
    List<StageResponsibility> stageResponsibilities = stageResponsibilityRepository.findAll();

    List<StageAsset> notProcessedAssets = stageAssets.stream().filter(asset -> asset.getActionDecision() == null).toList();
    List<StageRelation> notProcessedRelations = stageRelations.stream().filter(asset -> asset.getActionDecision() == null).toList();
    List<StageAttribute> notProcessedAttributes = stageAttributes.stream().filter(asset -> asset.getActionDecision() == null).toList();
    List<StageResponsibility> notProcessedResponsibilities = stageResponsibilities.stream().filter(asset -> asset.getActionDecision() == null).toList();

    assertAll(
      () -> assertEquals(0, notProcessedAssets.size(), "not processed assets"),
      () -> assertEquals(0, notProcessedRelations.size(), "not processed relations"),
      () -> assertEquals(0, notProcessedAttributes.size(), "not processed attributes"),
      () -> assertEquals(0, notProcessedResponsibilities.size(), "not processed responsibilities"),
      () -> assertEquals(100, response.getInserted().getAssets(), "inserted assets"),
      () -> assertEquals(100, response.getInserted().getAttributes(), "inserted attributes"),
      () -> assertEquals(100, response.getInserted().getRelations(), "inserted relation"),
      () -> assertEquals(100, response.getInserted().getResponsibilities(), "inserted responsibilities"),
      () -> assertEquals(0, response.getError().getAssets(), "error assets"),
      () -> assertEquals(0, response.getError().getAttributes(), "error attributes"),
      () -> assertEquals(0, response.getError().getRelations(), "error relation"),
      () -> assertEquals(0, response.getError().getResponsibilities(), "error responsibilities")
    );
  }

  @Test
  @Order(6)
  public void executeUpdateUpdateAllSuccessIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));
    this.jobsServiceUtils.prepareStageAssets(extractJob, 100, ActionDecision.U);
    this.jobsServiceUtils.prepareStageAttributes(extractJob, 100, ActionDecision.U);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);

    assertAll(
      () -> assertEquals(100, response.getUpdated().getAssets(), "updated assets"),
      () -> assertEquals(100, response.getUpdated().getAttributes(), "updated attributes"),
      () -> assertEquals(0, response.getError().getAssets(), "error assets"),
      () -> assertEquals(0, response.getError().getAttributes(), "error attributes")
    );
  }

  @Test
  @Order(7)
  public void executeUpdateDeleteAllSuccessIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));
    this.jobsServiceUtils.prepareStageAssets(extractJob, 100, ActionDecision.D);
    this.jobsServiceUtils.prepareStageAttributes(extractJob, 100, ActionDecision.D);
    this.jobsServiceUtils.prepareStageRelations(extractJob, 100, ActionDecision.D);
    this.jobsServiceUtils.prepareStageResponsibilities(extractJob, 100, ActionDecision.D);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);

    assertAll(
      () -> assertEquals(100, response.getDeleted().getAssets(), "deleted assets"),
      () -> assertEquals(100, response.getDeleted().getAttributes(), "deleted attributes"),
      () -> assertEquals(100, response.getDeleted().getRelations(), "deleted relation"),
      () -> assertEquals(100, response.getDeleted().getResponsibilities(), "deleted responsibilities"),
      () -> assertEquals(0, response.getError().getAssets(), "error assets"),
      () -> assertEquals(0, response.getError().getAttributes(), "error attributes"),
      () -> assertEquals(0, response.getError().getRelations(), "error relation"),
      () -> assertEquals(0, response.getError().getResponsibilities(), "error responsibilities")
    );
  }

  @Test
  public void executeUpdateCreateDeleteUpdateAllSuccessIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));
    this.jobsServiceUtils.prepareStageAssets(extractJob, 5, ActionDecision.I);
    this.jobsServiceUtils.prepareStageAssets(extractJob, 23, ActionDecision.U);
    this.jobsServiceUtils.prepareStageAssets(extractJob, 15, ActionDecision.D);

    this.jobsServiceUtils.prepareStageAttributes(extractJob, 11, ActionDecision.I);
    this.jobsServiceUtils.prepareStageAttributes(extractJob, 2, ActionDecision.U);
    this.jobsServiceUtils.prepareStageAttributes(extractJob, 17, ActionDecision.D);

    this.jobsServiceUtils.prepareStageResponsibilities(extractJob, 51, ActionDecision.I);
    this.jobsServiceUtils.prepareStageResponsibilities(extractJob, 29, ActionDecision.D);

    this.jobsServiceUtils.prepareStageRelations(extractJob, 3, ActionDecision.D);
    this.jobsServiceUtils.prepareStageRelations(extractJob, 27, ActionDecision.I);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);

    assertAll(
      () -> assertEquals(5, response.getInserted().getAssets(), "inserted assets"),
      () -> assertEquals(11, response.getInserted().getAttributes(), "inserted attributes"),
      () -> assertEquals(27, response.getInserted().getRelations(), "inserted relation"),
      () -> assertEquals(51, response.getInserted().getResponsibilities(), "inserted responsibilities"),
      () -> assertEquals(23, response.getUpdated().getAssets(), "updated assets"),
      () -> assertEquals(2, response.getUpdated().getAttributes(), "updated attributes"),
      () -> assertEquals(15, response.getDeleted().getAssets(), "deleted assets"),
      () -> assertEquals(17, response.getDeleted().getAttributes(), "deleted attributes"),
      () -> assertEquals(3, response.getDeleted().getRelations(), "deleted relation"),
      () -> assertEquals(29, response.getDeleted().getResponsibilities(), "deleted responsibilities"),
      () -> assertEquals(0, response.getError().getAssets(), "error assets"),
      () -> assertEquals(0, response.getError().getAttributes(), "error attributes"),
      () -> assertEquals(0, response.getError().getRelations(), "error relation"),
      () -> assertEquals(0, response.getError().getResponsibilities(), "error responsibilities")
    );
  }

  @Test
  public void executeUpdateErrorWhileRequestAllSuccessIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob( "kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));

    AssetType firstAssetType = assetTypeRepository.save(new AssetType("1_inserted_asset_type_name_for_asset_1", "1_inserted_asset_type_description_1", "acr_1", "color", language, user));
    Asset firstAsset = assetRepository.save(new Asset("1inserted_attribute_asset_name_1", firstAssetType, "1_inserted_displayed name", language, null, null, user));
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("1_inserted_attribute_type_name_1", "inserted_attribute_type_description_1", AttributeKindType.TEXT, null, null, language, user));
    stageAttributeRepository.save(new StageAttribute(extractJob, firstAsset.getAssetId(), firstAttributeType.getAttributeTypeId(), "inserted_some text", ActionDecision.I));

    AssetType secondAssetType = assetTypeRepository.save(new AssetType("2_inserted_asset_type_name_for_asset_2", "2_inserted_asset_type_description_2", "2_acr_2", "color", language, user));
    Asset secondAsset = assetRepository.save(new Asset("2_inserted_attribute_asset_name_2", secondAssetType, "2_inserted_displayed name", language, null, null, user));
    AttributeType secondAttributeType = attributeTypeRepository.save(new AttributeType("2_inserted_attribute_type_name_2", "inserted_attribute_type_description_2", AttributeKindType.TEXT, null, null, language, user));
    stageAttributeRepository.save(new StageAttribute(extractJob, secondAsset.getAssetId(), secondAttributeType.getAttributeTypeId(), "2_inserted_some text", ActionDecision.I));

    this.jobsServiceUtils.prepareStageAssets(extractJob, 11, ActionDecision.I);
    this.jobsServiceUtils.prepareStageAssets(extractJob, 23, ActionDecision.U);
    this.jobsServiceUtils.prepareStageAttributes(extractJob, 17, ActionDecision.D);
    this.jobsServiceUtils.prepareStageRelations(extractJob, 3, ActionDecision.D);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);

    assertAll(
      () -> assertEquals(11, response.getInserted().getAssets(), "inserted assets"),
      () -> assertEquals(23, response.getUpdated().getAssets(), "updated assets"),
      () -> assertEquals(17, response.getDeleted().getAttributes(), "deleted attributes"),
      () -> assertEquals(3, response.getDeleted().getRelations(), "deleted relation"),
      () -> assertEquals(0, response.getError().getAssets(), "error assets"),
      () -> assertEquals(2, response.getError().getAttributes(), "error attributes"),
      () -> assertEquals(0, response.getError().getRelations(), "error relation")
    );
  }

  @Test
  public void executeUpdateCreateAssetAndSetValueIntegrationTest () {
    ExtractJob extractJob = extractJobRepository.save(new ExtractJob("kafka-topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "source name", JobStatus.STAGE_COMPLETE, null, null, null));
    int number = 1;
    AssetType firstAssetType = assetTypeRepository.save(new AssetType(number + "_asset_type_name", number + "_asset_type_description", number + "_acr", "color", language, user));
    StageAsset firstStageAsset = new StageAsset(extractJob, number + "_asset_name", firstAssetType.getAssetTypeId(), number + "_asset_displayname", null, null, ActionDecision.I);
    stageAssetRepository.save(firstStageAsset);

    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType(number + "_attribute_type_name", number + "_attribute_type_description", AttributeKindType.TEXT, null, null, language, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(firstAssetType, firstAttributeType, null));
    StageAttribute firstStageAttribute = new StageAttribute(extractJob, null, firstAttributeType.getAttributeTypeId(), "some text", ActionDecision.I);
    firstStageAttribute.setAssetNameNk(firstStageAsset.getAssetName());
    stageAttributeRepository.save(firstStageAttribute);

    number = 2;
    AssetType secondAssetType = assetTypeRepository.save(new AssetType(number + "_asset_type_name", number + "_asset_type_description", number + "_acr", "color", language, user));
    StageAsset secondStageAsset = new StageAsset(extractJob, number + "_asset_name", secondAssetType.getAssetTypeId(), number + "_asset_displayname", null, null, ActionDecision.I);
    stageAssetRepository.save(secondStageAsset);

    AttributeType secondAttributeType = attributeTypeRepository.save(new AttributeType(number + "_attribute_type_name", number + "_attribute_type_description", AttributeKindType.TEXT, null, null, language, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(secondAssetType, secondAttributeType, null));
    StageAttribute secondStageAttribute = new StageAttribute(extractJob, null, secondAttributeType.getAttributeTypeId(), "some text", ActionDecision.I);
    secondStageAttribute.setAssetNameNk(secondStageAsset.getAssetName());
    stageAttributeRepository.save(secondStageAttribute);

    number = 3;
    AssetType thirdAssetType = assetTypeRepository.save(new AssetType(number + "_asset_type_name", number + "_asset_type_description", number + "_acr", "color", language, user));
    StageAsset thirdStageAsset = new StageAsset(extractJob, number + "_asset_name", firstAssetType.getAssetTypeId(), number + "_asset_displayname", null, null, ActionDecision.I);
    stageAssetRepository.save(thirdStageAsset);

    number = 4;
    AssetType forthAssetType = assetTypeRepository.save(new AssetType(number + "_asset_type_name", number + "_asset_type_description", number + "_acr", "color", language, user));
    StageAsset forthStageAsset = new StageAsset(extractJob, number + "_asset_name", firstAssetType.getAssetTypeId(), number + "_asset_displayname", null, null, ActionDecision.I);
    stageAssetRepository.save(forthStageAsset);

    RelationType relationType = relationTypeRepository.save(new RelationType(number + "_relation_type_name", number + "_description", 2, false, false, language, user));
    RelationTypeComponent firstComponent = relationTypeComponentRepository.save(new RelationTypeComponent(number + "_child_component", "desc", null, null, null, language, relationType, user));
    RelationTypeComponent secondComponent = relationTypeComponentRepository.save(new RelationTypeComponent(number + "_some_name", "desc", null, null, null, language, relationType, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstComponent, thirdAssetType, false, null, null));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondComponent, forthAssetType, false, null, null));

    StageRelation stageRelation = new StageRelation(extractJob, relationType.getRelationTypeId(), firstComponent.getRelationTypeComponentId(), null, secondComponent.getRelationTypeComponentId(), null, ActionDecision.I);
    stageRelation.setAssetName1Nk(thirdStageAsset.getAssetName());
    stageRelation.setAssetName2Nk(forthStageAsset.getAssetName());
    stageRelationRepository.save(stageRelation);

    number = 5;
    AssetType fifthAssetType = assetTypeRepository.save(new AssetType(number + "_asset_type_name", number + "_asset_type_description", number + "_acr", "color", language, user));
    StageAsset fifthStageAsset = new StageAsset(extractJob, number + "_asset_name", firstAssetType.getAssetTypeId(), number + "_asset_displayname", null, null, ActionDecision.I);
    stageAssetRepository.save(fifthStageAsset);

    number = 6;
    AssetType sixthAssetType = assetTypeRepository.save(new AssetType(number + "_asset_type_name", number + "_asset_type_description", number + "_acr", "color", language, user));
    StageAsset sixthStageAsset = new StageAsset(extractJob, number + "_asset_name", firstAssetType.getAssetTypeId(), number + "_asset_displayname", null, null, ActionDecision.I);
    stageAssetRepository.save(sixthStageAsset);

    RelationType secondRelationType = relationTypeRepository.save(new RelationType(number + "_relation_type_name", number + "_description", 2, false, false, language, user));
    RelationTypeComponent secondFirstComponent = relationTypeComponentRepository.save(new RelationTypeComponent(number + "_child_component", "desc", null, null, null, language, secondRelationType, user));
    RelationTypeComponent secondSecondComponent = relationTypeComponentRepository.save(new RelationTypeComponent(number + "_some_name", "desc", null, null, null, language, secondRelationType, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondFirstComponent, fifthAssetType, false, null, null));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondSecondComponent, sixthAssetType, false, null, null));

    StageRelation secondStageRelation = new StageRelation(extractJob, secondRelationType.getRelationTypeId(), secondFirstComponent.getRelationTypeComponentId(), null, secondSecondComponent.getRelationTypeComponentId(), null, ActionDecision.I);
    secondStageRelation.setAssetName1Nk(fifthStageAsset.getAssetName());
    secondStageRelation.setAssetName2Nk(sixthStageAsset.getAssetName());
    stageRelationRepository.save(secondStageRelation);

    number = 7;
    RelationType thirdRelationType = relationTypeRepository.save(new RelationType(number + "_relation_type_name", number + "_description", 2, false, false, language, user));
    RelationTypeComponent thirdFirstComponent = relationTypeComponentRepository.save(new RelationTypeComponent(number + "_child_component", "desc", null, null, null, language, thirdRelationType, user));
    RelationTypeComponent thirdSecondComponent = relationTypeComponentRepository.save(new RelationTypeComponent(number + "_some_name", "desc", null, null, null, language, thirdRelationType, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdFirstComponent, forthAssetType, false, null, null));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdSecondComponent, sixthAssetType, false, null, null));

    StageRelation thirdStageRelation = new StageRelation(extractJob, thirdRelationType.getRelationTypeId(), thirdFirstComponent.getRelationTypeComponentId(), null, thirdSecondComponent.getRelationTypeComponentId(), null, ActionDecision.I);
    thirdStageRelation.setAssetName1Nk(forthStageAsset.getAssetName());
    thirdStageRelation.setAssetName2Nk(sixthStageAsset.getAssetName());
    stageRelationRepository.save(thirdStageRelation);

    PostExecuteUpdateResponse response = jobsService.executeUpdate(new PostExecuteUpdateRequest(extractJob.getJobId()), user);

    Optional<StageAsset> updateFirstStageAsset = stageAssetRepository.findById(firstStageAsset.getStageAssetId());
    Optional<StageAsset> updateSecondStageAsset = stageAssetRepository.findById(secondStageAsset.getStageAssetId());
    Optional<StageAsset> updateThirdStageAsset = stageAssetRepository.findById(thirdStageAsset.getStageAssetId());
    Optional<StageAsset> updateForthStageAsset = stageAssetRepository.findById(forthStageAsset.getStageAssetId());
    Optional<StageAsset> updateFifthStageAsset = stageAssetRepository.findById(fifthStageAsset.getStageAssetId());
    Optional<StageAsset> updateSixthStageAsset = stageAssetRepository.findById(sixthStageAsset.getStageAssetId());

    Optional<StageAttribute> updateFirstStageAttribute = stageAttributeRepository.findById(firstStageAttribute.getStageAttributeId());
    Optional<StageAttribute> updateSecondStageAttribute = stageAttributeRepository.findById(secondStageAttribute.getStageAttributeId());
    Optional<StageRelation> updateFirstStageRelation = stageRelationRepository.findById(stageRelation.getStageRelationId());
    Optional<StageRelation> updateSecondStageRelation = stageRelationRepository.findById(secondStageRelation.getStageRelationId());
    Optional<StageRelation> updateThirdStageRelation = stageRelationRepository.findById(thirdStageRelation.getStageRelationId());

    assertAll(
      () -> assertEquals(updateFirstStageAttribute.get().getAssetId(), updateFirstStageAsset.get().getCreatedAssetId(), "created first attribute and check created first asset"),
      () -> assertEquals(updateSecondStageAttribute.get().getAssetId(), updateSecondStageAsset.get().getCreatedAssetId(), "created second attribute and check created second asset"),
      () -> assertEquals(updateFirstStageRelation.get().getAsset1Id(), updateThirdStageAsset.get().getCreatedAssetId(), "created first relation and check created third asset"),
      () -> assertEquals(updateFirstStageRelation.get().getAsset2Id(), updateForthStageAsset.get().getCreatedAssetId(), "created first relation and check created forth asset"),
      () -> assertEquals(updateSecondStageRelation.get().getAsset1Id(), updateFifthStageAsset.get().getCreatedAssetId(), "created second relation and check created fifth asset"),
      () -> assertEquals(updateSecondStageRelation.get().getAsset2Id(), updateSixthStageAsset.get().getCreatedAssetId(), "created second relation and check created sixth asset"),
      () -> assertEquals(updateThirdStageRelation.get().getAsset1Id(), updateForthStageAsset.get().getCreatedAssetId(), "created third relation and check created forth asset"),
      () -> assertEquals(updateThirdStageRelation.get().getAsset2Id(), updateSixthStageAsset.get().getCreatedAssetId(), "created third relation and check created sixth asset")
    );
  }
}
