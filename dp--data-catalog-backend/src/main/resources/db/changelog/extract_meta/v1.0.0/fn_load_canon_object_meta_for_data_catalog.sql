
/* 
fn_load_canon_object_meta_for_data_catalog - Функция для просчета дельты по метаинформации из разных СИД. 
job_uid - id задачи
instance_uid - uid источника (БД)
 */

CREATE OR REPLACE FUNCTION extract_delta.fn_load_canon_object_meta_for_data_catalog(instance_id uuid,  job_uid uuid)
    RETURNS VOID
    LANGUAGE PLPGSQL
    VOLATILE -- категория изменчивости функции 
AS
$FUNCTION$

DECLARE 
source_name text;
jobStatus text;

columns_tbl_name text;
tables_tbl_name text; 
views_tbl_name text; 
schemata_tbl_name text; 
filter_for_current_assets text;
create_statement_tmp_asset text;
logger text; 


BEGIN

logger = '"Подготовительная часть"';
/* GET job status */
	select 
		job_status into jobStatus
	from extract_meta.extract_job ej 
	where ej.job_id = job_uid;
	
/* CHECK JOB STATUS */
   IF jobStatus != 'DB_PROCESS' THEN
        RAISE EXCEPTION 'Job status should be DB_PROCESS, but now is % ', jobStatus;
    END IF;
	
/* GET STAGE TABLES NAME */	
	select 
		table_stage_name  into columns_tbl_name
	from extract_meta.extract_job_object ejo
	where 1=1
	and ejo.job_id = job_uid 
	and ejo.kafka_topic = 'canon_column_topic';
	
	select 
		table_stage_name  into tables_tbl_name
	from extract_meta.extract_job_object ejo
	where 1=1
	and ejo.job_id = job_uid 
	and ejo.kafka_topic = 'canon_table_topic';
	
	select 
		table_stage_name  into schemata_tbl_name
	from extract_meta.extract_job_object ejo
	where 1=1
	and ejo.job_id = job_uid 
	and ejo.kafka_topic = 'canon_schema_topic';
	
		
	select 
		table_stage_name  into views_tbl_name
	from extract_meta.extract_job_object ejo
	where 1=1
	and ejo.job_id = job_uid 
	and ejo.kafka_topic = 'canon_view_topic';
	
/* GET DB NAME  */
	select 
		asset_displayname into source_name
	from public.asset
	where asset_id = instance_id;

/* CHECK DB EXISTS  */
    IF source_name IS NULL THEN
        RAISE EXCEPTION 'Source with asset_id = % not found', instance_id;
    END IF;
	
/*   VIEWS WITH META GP  ЭТО СДЕЛАНО, ЧТОБЫ ДАЛЬШЕ НЕ ПЛОДИТЬ ЭКЗЕКЬЮТЫ, А ПРОСТО ПОДСТАВЛЯТЬ ПЕРЕМЕННЫЕ - ПОКА ПОД ? ИСПОЛЬЗОВАНИЕ ТАКОГО ПОДХОДА  */
    EXECUTE format('CREATE TEMP VIEW tmp_schema AS
	select * from  extract_stage.%I  
	', schemata_tbl_name);
	
	EXECUTE format('CREATE TEMP VIEW tmp_table AS
	select * from  extract_stage.%I  
	', tables_tbl_name) ;
	
	EXECUTE format('CREATE TEMP VIEW tmp_column  AS
	select * from  extract_stage.%I  
	', columns_tbl_name); 
	
	EXECUTE format('CREATE TEMP VIEW tmp_view  AS
	select * from extract_stage.%I 
	', views_tbl_name); 
	
	/* ВРЕМЕННАЯ ТАБЛИЦА, ЧТОБЫ БРАТЬ ИЗ ТЕКУЩЕГО СРЕЗА ДАННЫЕ ТОЛЬКО ПО НУЖНОЙ БД С УЧЕТОМ ВКЛЮЧЕНИЙ И ИСКЛЮЧЕНИЙ  */
	logger = 'Формирование временной таблицы = срез из public.asset по текущему источнику';
	if  (select filter_criteria from extract_meta.extract_job where job_id = job_uid ) is not null 
	then 
		create temp table temp_asset as
		select * from  extract_delta.fn_get_filtered_asset_table(job_uid, source_name);
	else
		create temp table temp_asset AS
			select *
			from public.asset ast
			where 1=1 
				and deleted_flag = false 
				and (ast.asset_name = source_name or ast.asset_name like concat(source_name,'>%'));		
	end if;
	
logger = 'Формирование PRE_ASSET';
/* FORMED PRE_ASSET TABLE */
   CREATE TEMP TABLE tmp_pre_asset_gp_1 AS
	/* SCHEMAS */
	select 
		concat(source_name,'>',schema_name) as asset_name 
		, concat(source_name,'>',schema_name) as asset_nk
		, at2.asset_type_id as asset_type_id
		, schema_name as asset_displayname 
	from tmp_schema  as2
	left JOIN public.asset_type at2 
	on at2.asset_type_name = 'Схема'
	union all
	/* TABLES*/
	select 
		concat(source_name,'>',schema_name,'>',table_name) as asset_name  
		, concat(source_name,'>',schema_name,'>',table_name) as asset_nk
		, at_t.asset_type_id as asset_type_id 
		, table_name as asset_displayname 
	from tmp_table main_t
	left JOIN public.asset_type at_t
	on at_t.asset_type_name = 'Таблица'
	union all 
	/* VIEWS */
	select 
		concat(source_name,'>',schema_name,'>',view_name) as asset_name  
		, concat(source_name,'>',schema_name,'>',view_name) as asset_nk
		, at_v.asset_type_id as  asset_type_id 
		, view_name as asset_displayname 
	from tmp_view  main_t
	left JOIN public.asset_type at_v
	on at_v.asset_type_name = 'Представление'
	union all 
	/* COLUMNS */
	select 
		concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_nk
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_name 
		, at2.asset_type_id as asset_type_id
		, column_name as asset_displayname 
	from tmp_column main_t
	left JOIN public.asset_type at2
	on at2.asset_type_name = 'Колонка';
	
	CREATE TEMP TABLE tmp_pre_asset_gp AS
	select 
		asset_nk
		, asset_name 
		, asset_type_id
		, asset_displayname 
		, lc_s.status_id as lifecycle_status 
		, stwrd_s.status_id as stewardship_status 
	from tmp_pre_asset_gp_1
	left JOIN public.status lc_s 
	on lc_s.status_name = 'Live'
	left JOIN public.status stwrd_s 
	on stwrd_s.status_name = 'Черновик';

logger = 'Формирование PRE_ATTRIBUTE';
/* FORMED PRE_ATTRIBUTE  */
   CREATE TEMP TABLE tmp_pre_attribute_gp_1 AS 
   /* COLUMNS DATA  */
	SELECT 
		concat(source_name,'>',schema_name,'>',object_name,'>',column_name,'|', att.attribute_type_id ) as attribute_nk
		, att.attribute_type_id  
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_nk
		, CASE
			WHEN main_t.is_nullable = 'YES' THEN 'true'
			ELSE 'false'
		END AS value 
		, CASE
			WHEN main_t.is_nullable = 'YES' THEN true
			ELSE false
		END AS value_bool
		, null::numeric as value_numeric	
		, null::bool as integer_flag
	FROM tmp_column  main_t
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Is nullable'
	UNION ALL  
	SELECT 
		concat(source_name,'>',schema_name,'>',object_name,'>',column_name,'|', att.attribute_type_id ) as attribute_nk
		, att.attribute_type_id  
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_nk
		,  column_type AS value 
		, NULL::bool AS value_bool
		, NULL::numeric AS value_numeric
		, null::bool as integer_flag
	FROM tmp_column   main_t
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Тип данных в системе'
	UNION ALL
	SELECT 
		concat(source_name,'>',schema_name,'>',object_name,'>',column_name,'|', att.attribute_type_id ) as attribute_nk
		, att.attribute_type_id  
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_nk		
		, main_t.column_position::text AS value 
		, NULL::bool AS value_bool
		, main_t.column_position AS value_numeric
		, true as integer_flag 			
	from tmp_column   main_t
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Позиция колонки'
	UNION ALL
	SELECT
		concat(source_name,'>',schema_name,'>',object_name,'>',column_name,'|', att.attribute_type_id ) as attribute_nk
		, att.attribute_type_id 
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_nk
		, column_comment as value 
		, null::bool as value_bool
		, null::numeric as value_numeric 
		, null::bool as integer_flag
	FROM tmp_column  main_t 
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Комментарий в системе'
	WHERE column_comment IS NOT NULL 
	UNION ALL 
	SELECT
		concat(source_name,'>',schema_name,'>',object_name,'>',column_name,'|', att.attribute_type_id ) as attribute_nk
		, att.attribute_type_id 
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) as asset_nk
		, pk_flag::text as value 
		, pk_flag::bool as value_bool
		, null::numeric as value_numeric 
		, null::bool as integer_flag
	FROM tmp_column  main_t 
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Вхождение в PK'
	UNION ALL 
/* TABLES DATA  */
SELECT
		concat(source_name,'>',schema_name,'>',table_name,'|', att.attribute_type_id) as attribute_nk  
		, att.attribute_type_id 
		, concat(source_name,'>',schema_name,'>',table_name) as asset_nk
		, table_comment as value
		, null::bool as value_bool
		, null::numeric as value_numeric 
		, null::bool as integer_flag
	FROM tmp_table main_t 
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Комментарий в системе'
	WHERE table_comment IS NOT NULL 
	UNION ALL 
/* VIEWS DATA  */
	SELECT
		concat(source_name,'>',schema_name,'>',view_name,'|', att.attribute_type_id) as attribute_nk  
		, att.attribute_type_id 
		, concat(source_name,'>',schema_name,'>',view_name) as asset_nk
		, view_sql as value
		, null::bool as value_bool
		, null::numeric as value_numeric 
		, null::bool as integer_flag
	FROM tmp_view  main_t 
	JOIN public.attribute_type  att
		ON att.attribute_type_name = 'SQL запрос построения'
	UNION ALL  
	SELECT
		concat(source_name,'>',schema_name,'>',view_name,'|', att.attribute_type_id) as attribute_nk  
		, att.attribute_type_id 
		, concat(source_name,'>',schema_name,'>',view_name) as asset_nk
		, view_comment as value
		, null::bool as value_bool
		, null::numeric as value_numeric 
		, null::bool as integer_flag
	FROM tmp_view  main_t 
	JOIN public.attribute_type  att
		ON att.attribute_type_name = 'Комментарий в системе'
	WHERE view_comment IS NOT NULL 
	UNION ALL
/* SCHEMA DATA  */
	SELECT
		concat(source_name,'>',schema_name,'|', att.attribute_type_id) as attribute_nk  
		, att.attribute_type_id 
		, concat(source_name,'>',schema_name) as asset_nk		
		, schema_comment  as value
		, null::bool as value_bool
		, null::numeric as value_numeric 
		, null::bool as integer_flag
	FROM tmp_schema  main_t 
	JOIN public.attribute_type  att
	ON att.attribute_type_name = 'Комментарий в системе'
	WHERE schema_comment IS NOT NULL ;
	
	/* ПОДЯТНУТЬ АССЕТ_ИД  */
	CREATE TEMP TABLE tmp_pre_attribute_gp AS 
	select 
		attribute_nk
		, attribute_type_id
		, ast.asset_id
		, asset_nk
		, value
		, value_bool
		, value_numeric
		, integer_flag
	from tmp_pre_attribute_gp_1 pre
	left join temp_asset  ast
		on pre.asset_nk = ast.asset_name;

logger = 'Формирование PRE_RELATIONS';
/* FORMED PRE_RELATIONS */
	CREATE TEMP  TABLE tmp_pre_relation_gp_1 AS 
	/* СХЕМА СОДЕРЖИТЬ ТАБЛИЦУ */
	SELECT 
		concat(rt.relation_type_id, '|' 
		, rtc1.relation_type_component_id, '|' 
		, source_name,'>',schema_name,'|'
		, rtc2.relation_type_component_id, '|' 
		, source_name,'>',schema_name,'>',table_name) AS relation_nk
		, concat(source_name,'>',schema_name) AS asset_1_nk
		, concat(source_name,'>',schema_name,'>',table_name) AS asset_2_nk		
		, rt.relation_type_id
		, rtc1.relation_type_component_id as rtc1
		, rtc2.relation_type_component_id as rtc2
	FROM tmp_table st 
	JOIN public.relation_type rt 
	ON relation_type_name = 'Объектный состав схемы БД'
	join public.relation_type_component rtc1
	on rtc1.relation_type_component_name = 'Принадлежит схеме'
	and rtc1.relation_type_id = rt.relation_type_id
	join public.relation_type_component rtc2
	on rtc2.relation_type_component_name = 'Содержит объект'
	and rtc2.relation_type_id = rt.relation_type_id
	UNION ALL 
	/* СХЕМА СОДЕРЖИТЬ ПРЕДСТАВЛЕНИЕ */
	SELECT 
		concat(rt.relation_type_id, '|' 
		, rtc1.relation_type_component_id, '|' 
		, source_name,'>',schema_name,'|'
		, rtc2.relation_type_component_id, '|' 
		, source_name,'>',schema_name,'>',view_name) AS relation_nk
		, concat(source_name,'>',schema_name) AS asset_1_nk
		, concat(source_name,'>',schema_name,'>',view_name) AS asset_2_nk		
		, rt.relation_type_id
		, rtc1.relation_type_component_id as rtc1
		, rtc2.relation_type_component_id as rtc2
	FROM tmp_view  st 
	JOIN public.relation_type rt 
	ON relation_type_name = 'Объектный состав схемы БД'
	join public.relation_type_component rtc1
	on rtc1.relation_type_component_name = 'Принадлежит схеме'
	and rtc1.relation_type_id = rt.relation_type_id
	join public.relation_type_component rtc2
	on rtc2.relation_type_component_name = 'Содержит объект'
	and rtc2.relation_type_id = rt.relation_type_id
	UNION ALL
	/* ТАБЛИЦА СОДЕРЖИТ КОЛНКУ */
	SELECT 
		concat(rt.relation_type_id, '|'
 		, rtc1.relation_type_component_id, '|' 
		, source_name, '>',schema_name ,'>',object_name ,'|'
		, rtc2.relation_type_component_id, '|' 
		, source_name,'>',schema_name,'>',object_name,'>',column_name) AS relation_nk
		, concat(source_name, '>',schema_name,'>',object_name) AS asset_1_nk
		, concat(source_name,'>',schema_name,'>',object_name,'>',column_name) AS asset_2_nk		
		, rt.relation_type_id
		, rtc1.relation_type_component_id as rtc1
		, rtc2.relation_type_component_id as rtc2
	FROM tmp_column sc 
	JOIN public.relation_type rt 
		ON relation_type_name = 'Состав таблицы/вью'
	join public.relation_type_component rtc1
		on rtc1.relation_type_component_name = 'В таблице/вью' 
	join public.relation_type_component rtc2
		on rtc2.relation_type_component_name = 'Содержится колонка'
	UNION all 
	/* БД СОДЕРЖИТ СХЕМУ  */
	SELECT 
		concat(rt.relation_type_id, '|'
		, rtc1.relation_type_component_id, '|' 
		, source_name, '|'
		, rtc2.relation_type_component_id, '|' 
		, source_name,'>',schema_name) AS relation_nk
		, concat(source_name) AS asset_1_nk
		, concat(source_name,'>',schema_name) AS asset_2_nk		
		, rt.relation_type_id
		, rtc1.relation_type_component_id as rtc1
		, rtc2.relation_type_component_id as rtc2
	FROM tmp_schema as2 
	JOIN public.relation_type rt 
		ON relation_type_name = 'Состав схем БД'
	join public.relation_type_component rtc1
		on rtc1.relation_type_component_name = 'База данных' 
	join public.relation_type_component rtc2
		on rtc2.relation_type_component_name = 'Схема'
	;
	
	/* ПОДЯТНУТЬ АССЕТ_ИД  */
	CREATE TEMP TABLE tmp_pre_relation_gp AS 
	select 
		relation_nk
		, asset_1_nk
		, asset_2_nk		
		, relation_type_id
		, rtc1
		, rtc2
		, ast1.asset_id as asset_1_id
		, ast2.asset_id as asset_2_id
	from tmp_pre_relation_gp_1 pre
	left join temp_asset  ast1
		on pre.asset_1_nk = ast1.asset_name
	left join temp_asset  ast2
		on pre.asset_2_nk = ast2.asset_name;

	/* INSERT INTO STAGE_ASSET */
	logger = 'Считаем дельту, заполняем STAGE_ASSET';	
	WITH pre_asset_hash as  (
		SELECT 
		asset_nk
		, asset_type_id 
		, asset_displayname
		, lifecycle_status 
		, stewardship_status 
		, MD5(REGEXP_REPLACE(CONCAT(
				COALESCE(cast(stewardship_status as text),''),'#',
				COALESCE(cast(lifecycle_status as text),''),'#',
				COALESCE(cast(asset_displayname as text),'')
				),'\t',' ')) as pre_ast_hash
		FROM tmp_pre_asset_gp
	),
	asset_hash as (
		SELECT
		 asset_id 
		,asset_name 
		,asset_type_id
		, asset_displayname
		, lifecycle_status 
		, stewardship_status 
		, MD5(REGEXP_REPLACE(CONCAT(
				COALESCE(cast(stewardship_status as text),''),'#',
				COALESCE(cast(lifecycle_status as text),''),'#',
				COALESCE(cast(asset_displayname as text),'')
				),'\t',' ')) as ast_hash
		FROM temp_asset
		WHERE 1=1
		)
	INSERT into extract_delta.stage_asset
	(job_id,
	asset_name ,
	asset_type_id ,
	asset_displayname ,
	lifecycle_status ,
	stewardship_status ,
	inserted_datetime ,
	action_decision ,
	action_process_status ,
	matched_asset_id
	)
	SELECT 
		job_uid as job_id
		, coalesce(asset_nk, asset_name) as asset_name
		, coalesce(ah.asset_type_id, prh.asset_type_id) as asset_type_id  --- Порядок в колалеске важен - всегдя тянем старый тип, изменение типа обрабатывается отдельно  
		, coalesce(prh.asset_displayname, ah.asset_displayname) as asset_displayname
		, case  
			when prh.asset_nk is null 
				then (select status_id from status where status_name = 'Deprecated')
		    else coalesce(prh.lifecycle_status, ah.lifecycle_status)
		end as lifecycle_status
		, coalesce(ah.stewardship_status, prh.stewardship_status) stewardship_status
		, now() as inserted_datetime
		, case 
			when prh.asset_nk is null and ah.lifecycle_status not in (select status_id from status where status_name = 'Deprecated') then 'U' 
			-- when prh.asset_nk is null and ah.lifecycle_status  in (select status_id from status where status_name = 'Depricated') then 'N' 
			when ah.asset_name is null then 'I' 
			when  prh.asset_nk = ah.asset_name and pre_ast_hash != ast_hash then 'U'
		end as action_decision 
		, null::text as action_processed_status
		, cast(asset_id as uuid) as matched_asset_id
	FROM asset_hash ah
	FULL JOIN pre_asset_hash prh
		on prh.asset_nk = ah.asset_name
	where 1=1
	and not ( coalesce(prh.asset_nk,'null') = coalesce(ah.asset_name,'null') and pre_ast_hash = ast_hash) 
	and not ( prh.asset_nk is null and ah.lifecycle_status in (select status_id from status where status_name = 'Deprecated'))
	and coalesce(ah.asset_name, 'null') != source_name;
	
	/* INSERT INTO CHANGES_ASSET_TYPE */
	logger = 'Заполняем log_asset_type_changes ассетами с изменяющимися asset_type';	
	INSERT into extract_delta.log_asset_type_changes
	(job_id,
	asset_name ,
	old_asset_type_id ,
	new_asset_type_id,
	fix_flg
	)
	SELECT 
		job_uid as job_id
		, old_a.asset_name 
		, old_a.asset_type_id as old_asset_type_id
		, new_a.asset_type_id as  new_asset_type_id
		, false as fix_flg
	FROM temp_asset  old_a
	INNER JOIN tmp_pre_asset_gp new_a
		ON new_a.asset_nk = old_a.asset_name
		AND old_a.asset_type_id != new_a.asset_type_id;


	logger = 'Считаем дельту, заполняем STAGE_ATTRIBUTE';
/* INSERT INTO STAGE_ATTRIBUTE */
	with pre_attribute_hash as  (
	select 
		attribute_nk  
			, asset_nk
			, asset_id
			, attribute_type_id 
			, value
			, value_bool
			, value_numeric 
			, integer_flag
			, MD5(REGEXP_REPLACE(CONCAT(
			COALESCE(cast(attribute_type_id as text),''),'#',
			COALESCE(cast(value as text),''),'#',
			COALESCE(cast(value_bool as text),''),'#',
			COALESCE(cast(value_numeric as text),''),'#',
			COALESCE(cast(integer_flag as text),'')
			),'\t',' ')) as pre_atr_hash
	from tmp_pre_attribute_gp ),
	attribute_hash as (
	select
	atr.asset_id
	, asset_name
	, asset_type_id 
	, attribute_id
	, attribute_type_id
	, value
	, concat( asset_name,'|', attribute_type_id) as attribute_nk  
	, MD5(REGEXP_REPLACE(CONCAT(
			COALESCE(cast(attribute_type_id as text),''),'#',
			COALESCE(cast(value as text),''),'#',
			COALESCE(cast(value_bool as text),''),'#',
			COALESCE(cast(value_numeric as text),''),'#',
			COALESCE(cast(integer_flag as text),'')
			),'\t',' ')) as atr_hash
	from public."attribute" atr
	join temp_asset  ast
		on atr.asset_id = ast.asset_id
		and atr.deleted_flag = false
		and atr.attribute_type_id in (select attribute_type_id from public.attribute_type at2 
									  where at2.attribute_type_name in ('Is nullable','SQL запрос построения','Комментарий в системе',
																		'Позиция колонки', 'Тип данных в системе','Вхождение в PK'))
	where 1=1
	)
	INSERT into extract_delta.stage_attribute(
	job_id,
	nk,
	asset_name_nk,
	asset_id,
	attribute_type_id ,
	value,
	inserted_datetime ,
	action_decision ,
	action_process_status ,
	matched_attribute_id 
	)
	select 
	job_uid as job_id
	, concat(coalesce(asset_nk, asset_name),'|', coalesce(ah.attribute_type_id, prh.attribute_type_id)) as nk 
	, coalesce(asset_nk, asset_name) as asset_name_nk
	, coalesce(ah.asset_id, prh.asset_id) as asset_id
	, coalesce(ah.attribute_type_id, prh.attribute_type_id) as attribute_type_id
	, prh.value
	, now() as inserted_datetime
	, case 
			when prh.attribute_nk is null then 'N' --  ничего не уаляем из attribute
			when ah.attribute_nk is null then 'I' 
			when  prh.attribute_nk =  ah.attribute_nk and pre_atr_hash != atr_hash then 'U'
		end as action_decision 
	, null::text as action_processed_status
	, ah.attribute_id as matched_attribute_id
	from attribute_hash ah
	full join pre_attribute_hash prh
		on ah.attribute_nk = prh.attribute_nk
	where 1=1 
		and not (coalesce(prh.attribute_nk,'null') = coalesce(ah.attribute_nk,'null') and pre_atr_hash = atr_hash)
		and coalesce(asset_nk, asset_name) not in (select asset_name from extract_delta.log_asset_type_changes where fix_flg = false);

	logger = 'Считаем дельту, заполняем STAGE_RELATION';	
/* INSERT INTO STAGE_RELATION */
	with rel_hash as (
	select
	concat(
			relation_type_id, '|'
			, r1.relation_type_component_id, '|' 
			, ast1.asset_name, '|'
			, r2.relation_type_component_id, '|'
			, ast2.asset_name
	) as relation_nk 
	, relation_type_id
	, rel.relation_id
	, ast1.asset_name as asset_1_nk
	, ast2.asset_name as asset_2_nk
	, r1.relation_type_component_id as rtc1
	, r2.relation_type_component_id as rtc2
	, ast1.asset_id  as asset_1_id
	, ast2.asset_id  as asset_2_id
	from public.relation rel
	join public.relation_component r1
		on rel.relation_id = r1.relation_id
		and r1.hierarchy_role = 'PARENT'
	join  public.relation_component r2
		on r1.relation_id  = r2.relation_id 
		and r2.hierarchy_role = 'CHILD'
	join temp_asset ast1
		on ast1.asset_id = r1.asset_id 
	join temp_asset ast2
		on ast2.asset_id = r2.asset_id 
	where rel.deleted_flag = false
	)
	INSERT INTO extract_delta.stage_relation (
	job_id  ,
	nk  ,
	relation_type_id  ,
	relation_type_component_1_id  ,
	asset_1_id  ,
	asset_name_1_nk  ,
	relation_type_component_2_id  ,
	asset_2_id  ,
	asset_name_2_nk  ,
	inserted_datetime ,
	action_decision ,
	action_process_status ,
	matched_relation_id 
	)
	SELECT 
	job_uid as job_id
	,  coalesce(gp.relation_nk,rel_hash.relation_nk)   as nk
	, coalesce(gp.relation_type_id,rel_hash.relation_type_id) as relation_type_id
	, coalesce(rel_hash.rtc1, gp.rtc1) as relation_type_component_type_1_id --- ЗДЕСЬ ВАЖЕН ПОРЯДОК В КОАЛЕСКЕ, БЕРЕМ ВСЕГДА ПЕРВЫМ ТОТ, ЧТО УЖЕ ЕСТЬ В ДК (public.relation)
	, coalesce(gp.asset_1_id, rel_hash.asset_1_id) as asset_2_id
	, coalesce(gp.asset_1_nk, rel_hash.asset_1_nk) as asset_name_1_nk
	, coalesce(rel_hash.rtc2, gp.rtc2) as relation_type_component_type_2_id --- ЗДЕСЬ ВАЖЕН ПОРЯДОК В КОАЛЕСКЕ, БЕРЕМ ВСЕГДА ПЕРВЫМ ТОТ, ЧТО УЖЕ ЕСТЬ В ДК (public.relation)
	, coalesce(gp.asset_2_id, rel_hash.asset_2_id) as asset_2_id
	, coalesce(gp.asset_2_nk, rel_hash.asset_2_nk) as asset_name_2_nk
	, now() as inserted_datetime
	, CASE 
		when rel_hash.relation_nk is null then 'I'
		when gp.relation_nk is null then 'N'--  ничего не уаляем из attribute
		end as action_decision 
	, null::text as action_processed_status
	, relation_id  as matched_relation_id
	FROM rel_hash 
	FULL JOIN tmp_pre_relation_gp  gp
		ON rel_hash.relation_nk = gp.relation_nk 
	AND rel_hash.relation_type_id = gp.relation_type_id
	WHERE 1=1 
		AND  NOT (coalesce(rel_hash.relation_nk,'null') = coalesce(gp.relation_nk, 'null'))
		and coalesce(rel_hash.asset_1_nk,'null') not in (select asset_name from extract_delta.log_asset_type_changes where fix_flg = false)
		and coalesce(rel_hash.asset_2_nk,'null') not in (select asset_name from extract_delta.log_asset_type_changes where fix_flg = false);
	
	logger = 'Завершаем работу';
	update extract_meta.extract_job 
	set 
	job_status = 'STAGE_COMPLETE'
	, last_status_change_datetime = now()
	where job_id = job_uid;

logger = 'Удаление временных таблиц и вью';	
-- /* DROP VIEWS */
 drop view if exists tmp_schema;
 drop view if exists tmp_view;
 drop view if exists tmp_table;
 drop view if exists tmp_column;

-- /* DROP TABLES */
drop table if exists tmp_pre_asset_gp_1;
drop table if exists tmp_pre_attribute_gp_1;
drop table if exists tmp_pre_relation_gp_1;
drop table if exists tmp_pre_asset_gp;
drop table if exists tmp_pre_attribute_gp ;
drop table if exists tmp_pre_relation_gp  ;
drop table if exists temp_asset ;

logger = 'Обработка исключений';	
EXCEPTION
    WHEN OTHERS THEN
		update extract_meta.extract_job 
		set 
		last_status_change_datetime = now()
		, job_error = concat('на этапе ',logger,' ошибка ',sqlerrm)
		, job_error_flag = true
		where job_id = job_uid;
		RAISE NOTICE 'Произошла ошибка: % на этапе %', sqlerrm, logger;
END;
$FUNCTION$;


