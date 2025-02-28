-- DROP FUNCTION extract_delta.fn_load_api_dpc_for_data_catalog(uuid, uuid);

CREATE OR REPLACE FUNCTION extract_delta.fn_load_api_dpc_for_data_catalog(instance_id uuid, job_uid uuid)
 RETURNS void
 LANGUAGE plpgsql
AS $function$

DECLARE 
source_name text;
jobStatus text;

dpc_table_name text; 
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
		table_stage_name  into dpc_table_name
	from extract_meta.extract_job_object ejo
	where 1=1
	and ejo.job_id = job_uid;
	
/* GET DB NAME  */
	select 
		asset_displayname into source_name
	from public.asset
	where asset_id = instance_id;
	
/* CHECK DB EXISTS  */
    IF source_name IS NULL THEN
        RAISE EXCEPTION 'Source with asset_id = % not found', instance_id;
    END IF;
	
    EXECUTE format('CREATE TEMP VIEW tmp_dpc AS
	select * from  extract_stage.%I  
	', dpc_table_name);
	
	
	/* ВРЕМЕННАЯ ТАБЛИЦА, ЧТОБЫ БРАТЬ ИЗ ТЕКУЩЕГО СРЕЗА ДАННЫЕ ТОЛЬКО ПО НУЖНОЙ API*/
	logger = 'Формирование временной таблицы = срез из public.asset по текущему источнику';
	create temp table temp_asset AS
			select *
			from public.asset ast
			where 1=1 
				and deleted_flag = false 
				and asset_type_id = '0782d09d-f74e-4775-952e-691f59bfa41b';		
		
    logger = 'Формирование PRE_ASSET';
   /* FORMED ASSET TABLE */
   CREATE TEMP TABLE tmp_dpc_pre_asset AS
	select 
		digital_product_code as asset_name 
		, digital_product_code as asset_nk
		, '0782d09d-f74e-4775-952e-691f59bfa41b'::uuid as asset_type_id
		, digital_product_name as asset_displayname 
		, case 
				when tmp_dpc.digital_product_status = 'Active' then '42725f1c-e0b2-4b88-bb89-919dddad350c'::uuid
				when tmp_dpc.digital_product_status = 'Retired' then 'b735537a-c2f5-47d2-969f-5dc23ab01034'::uuid
				when tmp_dpc.digital_product_status = 'Not Ready' then '0d8eeac7-c1fc-428a-9be1-ecddb4732d57'::uuid
				when tmp_dpc.digital_product_status = 'Verification' then 'd742b2c8-39b3-41f0-8d01-a0938056660c'::uuid
				when tmp_dpc.digital_product_status = 'In progress' then '36c73448-e454-4820-abd2-71ac2364a9d4'::uuid
				when tmp_dpc.digital_product_status = 'Activation' then '385ba614-e85a-4745-bc46-6f641f7a83d9'::uuid
				when tmp_dpc.digital_product_status = 'Activation in progress' then 'bb2ea299-d2f1-4a44-a8b9-a8fcfafc989c'::uuid
				when tmp_dpc.digital_product_status = 'Deletion' then 'c3099e0b-844f-4232-9fe9-afda3d4c75fe'::uuid
		end as lifecycle_status 
		,  '55a86990-84bd-4c95-af43-eb015224ba74'::uuid as stewardship_status 
	from tmp_dpc;
	
logger = 'Формирование PRE_ATTRIBUTE';
/* FORMED PRE_ATTRIBUTE  */
   CREATE TEMP TABLE tmp_dpc_pre_attribute_1 AS 
   /* Описание */
	SELECT 
		concat(digital_product_code,'|','00000000-0000-0000-0000-000000003114') as attribute_nk
		, '00000000-0000-0000-0000-000000003114'::uuid attribute_type_id  
		, digital_product_code as asset_nk
		, digital_product_description as  value 
	FROM tmp_dpc
	WHERE digital_product_description is not null
	UNION ALL 
	/* Идентификатор в системе */
	SELECT 
	concat(digital_product_code,'|','cd8a00b0-6ad2-47b8-9021-d1a89d78122f') as attribute_nk
	, 'cd8a00b0-6ad2-47b8-9021-d1a89d78122f'::uuid attribute_type_id  
	, digital_product_code as asset_nk
	, digital_product_code as  value 
	FROM tmp_dpc;
	
	/* ПОДЯТНУТЬ АССЕТ_ИД  */
	CREATE TEMP TABLE tmp_dpc_pre_attribute AS 
	select 
		attribute_nk
		, attribute_type_id
		, asset_nk
		, ast.asset_id
		, value
	from tmp_dpc_pre_attribute_1 pre
	left join temp_asset ast
		on pre.asset_nk = ast.asset_name;

logger = 'Формирование PRE_RELATIONS';
/* FORMED PRE_RELATIONS */
	CREATE TEMP  TABLE tmp_dpc_pre_relation AS 
	/* Состав каталога */
	SELECT 
		concat('93a32089-e4fc-44e8-9725-806b456f0661', '|' 
		, '41573f2a-47ed-490d-b1d3-975c622e77d4', '|' 
		, source_name,'|'
		, '289d609c-7e7c-43be-b84a-db971b54f37e', '|' 
		, digital_product_code) AS relation_nk
		, source_name AS asset_1_nk
		, digital_product_code AS asset_2_nk		
		, '93a32089-e4fc-44e8-9725-806b456f0661'::uuid as relation_type_id
		, '41573f2a-47ed-490d-b1d3-975c622e77d4'::uuid as rtc1
		, '289d609c-7e7c-43be-b84a-db971b54f37e'::uuid as rtc2
		, instance_id as asset_1_id
		, ast2.asset_id as asset_2_id
	FROM tmp_dpc pre
	left join temp_asset  ast2
		on pre.digital_product_code = ast2.asset_name;
		
logger = 'Формирование PRE_RESPONSIBILITIES';
/* FORMED PRE_RESPONSIBILITIES */
	CREATE TABLE tmp_dpc_pre_responsibility AS 
	SELECT 
		responsibility_nk
		, asset_nk
		, asset_id
		, t1.username
		, user_id
		, role_id
	from (
		SELECT
			CONCAT(digital_product_owner, '|', '00000000-0000-0000-0000-000000005040', '|', digital_product_code) as responsibility_nk--Бизнес-владелец
			, digital_product_code as asset_nk
			, digital_product_owner as username
			, '00000000-0000-0000-0000-000000005040'::uuid as role_id
		from tmp_dpc
        where digital_product_owner is not null
		UNION ALL 
		SELECT
			CONCAT(digital_product_manager, '|', '0fd7d00c-0aba-445a-b21e-9c75b588135c', '|', digital_product_code) --Менеджер продукта
			, digital_product_code as asset_nk
			, digital_product_manager as username
			, '0fd7d00c-0aba-445a-b21e-9c75b588135c'::uuid as role_id 
		from tmp_dpc
        where digital_product_manager is not null
	) t1
	left join temp_asset  
		on asset_nk = asset_name
	left join public.user u
			on t1.username = u.username;
			
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
		FROM tmp_dpc_pre_asset
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
		, coalesce(ah.asset_type_id, prh.asset_type_id) as asset_type_id 
		, coalesce(prh.asset_displayname, ah.asset_displayname) as asset_displayname
		, coalesce(prh.lifecycle_status, ah.lifecycle_status) as lifecycle_status
		, coalesce(prh.stewardship_status, ah.stewardship_status) stewardship_status
		, now() as inserted_datetime
		, case 
			when prh.asset_nk is null then 'D' 
			when ah.asset_name is null then 'I' 
			when prh.asset_nk = ah.asset_name and pre_ast_hash != ast_hash then 'U'
		end as action_decision 
		, null::text as action_processed_status
		, cast(asset_id as uuid) as matched_asset_id
	FROM asset_hash ah
	FULL JOIN pre_asset_hash prh
		on prh.asset_nk = ah.asset_name
	where 1=1
	and not ( coalesce(prh.asset_nk,'null') = coalesce(ah.asset_name,'null') and pre_ast_hash = ast_hash);
	
	logger = 'Считаем дельту, заполняем STAGE_ATTRIBUTE';
/* INSERT INTO STAGE_ATTRIBUTE */
	with pre_attribute_hash as  (
	select 
		attribute_nk  
			, asset_nk
			, asset_id
			, attribute_type_id 
			, value
			, MD5(REGEXP_REPLACE(CONCAT(
			COALESCE(cast(attribute_type_id as text),''),'#',
			COALESCE(cast(value as text),'')
			),'\t',' ')) as pre_atr_hash
	from tmp_dpc_pre_attribute
	),
	attribute_hash as (
	select
	atr.asset_id
	, asset_name
	, attribute_id
	, attribute_type_id
	, value
	, concat( asset_name,'|', attribute_type_id) as attribute_nk  
	, MD5(REGEXP_REPLACE(CONCAT(
			COALESCE(cast(attribute_type_id as text),''),'#',
			COALESCE(cast(value as text),'')
			),'\t',' ')) as atr_hash
	from public."attribute" atr
	join temp_asset  ast
		on atr.asset_id = ast.asset_id
		and atr.deleted_flag = false
		and atr.attribute_type_id in ('00000000-0000-0000-0000-000000003114','cd8a00b0-6ad2-47b8-9021-d1a89d78122f')
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
		and not (coalesce(prh.attribute_nk,'null') = coalesce(ah.attribute_nk,'null') and pre_atr_hash = atr_hash);

	logger = 'Считаем дельту, заполняем STAGE_RELATION';	
/* INSERT INTO STAGE_RELATION */
	with rel_hash as (
	select
	concat(
			relation_type_id, '|'
			, rc1.relation_type_component_id, '|' 
			, ast1.asset_name, '|'
			, rc2.relation_type_component_id, '|'
			, ast2.asset_name
	) as relation_nk 
	, relation_type_id
	, rel.relation_id
	, ast1.asset_name as asset_1_nk
	, ast2.asset_name as asset_2_nk
	, rc1.relation_type_component_id as rtc1
	, rc2.relation_type_component_id as rtc2
	, ast1.asset_id  as asset_1_id
	, ast2.asset_id  as asset_2_id
	from public.relation rel
	inner join public.relation_component rc1
		on rel.relation_id = rc1.relation_id 
		and not rel.deleted_flag 
		and rel.relation_type_id = '93a32089-e4fc-44e8-9725-806b456f0661'::uuid
		and rc1.asset_id = '46e38655-fa10-4ac9-a0ee-3eead072854d'::uuid
		and rc1.relation_type_component_id = '41573f2a-47ed-490d-b1d3-975c622e77d4'::uuid
	inner join public.relation_component rc2
		on rc2.asset_id != rc1.asset_id 
		and rc1.relation_id = rc2.relation_id	
	inner join public.asset ast1
		on ast1.asset_id = rc1.asset_id
	inner join temp_asset ast2
		on ast2.asset_id = rc2.asset_id
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
	, coalesce(rel_hash.rtc1, gp.rtc1) as relation_type_component_type_1_id --- ЗaДЕСЬ ВАЖЕН ПОРЯДОК В КОАЛЕСКЕ, БЕРЕМ ВСЕГДА ПЕРВЫМ ТОТ, ЧТО УЖЕ ЕСТЬ В ДК (public.relation)
	, coalesce(gp.asset_1_id, rel_hash.asset_1_id) as asset_1_id
	, coalesce(gp.asset_1_nk, rel_hash.asset_1_nk) as asset_name_1_nk
	, coalesce(rel_hash.rtc2, gp.rtc2) as relation_type_component_type_2_id --- ЗДЕСЬ ВАЖЕН ПОРЯДОК В КОАЛЕСКЕ, БЕРЕМ ВСЕГДА ПЕРВЫМ ТОТ, ЧТО УЖЕ ЕСТЬ В ДК (public.relation)
	, coalesce(gp.asset_2_id, rel_hash.asset_2_id) as asset_2_id
	, coalesce(gp.asset_2_nk, rel_hash.asset_2_nk) as asset_name_2_nk
	, now() as inserted_datetime
	, CASE 
		when rel_hash.relation_nk is null then 'I'
		when gp.relation_nk is null then 'N'--  ничего не уаляем из relation
		end as action_decision 
	, null::text as action_process_status
	, relation_id  as matched_relation_id
	FROM rel_hash 
	FULL JOIN tmp_dpc_pre_relation  gp
		ON rel_hash.relation_nk = gp.relation_nk 
	AND rel_hash.relation_type_id = gp.relation_type_id
	WHERE 1=1 
		AND  NOT (coalesce(rel_hash.relation_nk,'null') = coalesce(gp.relation_nk, 'null'));

	logger = 'Считаем дельту, заполняем STAGE_RESPONSIBILITY';	
/* INSERT INTO STAGE_RESPONSIBILITY */
	WITH resp_hash as (
		select 
		concat(username, '|', role_id,'|', asset_name)  as responsibility_nk
		, responsibility_id 
		, t1.user_id
		, username
		, role_id
		, a.asset_id 
		, a.asset_name
		from responsibility t1
		join public.user u
			on u.user_id = t1.user_id 
		join temp_asset a
			on a.asset_id = t1.asset_id 
		where not t1.deleted_flag 
		and role_id in ('00000000-0000-0000-0000-000000005040','0fd7d00c-0aba-445a-b21e-9c75b588135c')
	)
	INSERT INTO extract_delta.stage_responsibility 
	(
		job_id,
		nk ,
		role_id ,
		asset_id ,
		user_id,
		group_id,
		responsible_type,
		asset_name_nk ,
		inserted_datetime ,
		action_decision ,
		action_process_status ,
		matched_responsibility_id
	)
	select 
		job_uid
		, coalesce(hash.responsibility_nk, pre.responsibility_nk) as nk 
		, coalesce(hash.role_id, pre.role_id) as role_id
		, coalesce(hash.asset_id, pre.asset_id) as asset_id 
		, coalesce(hash.user_id, pre.user_id) as user_id
		, null::uuid as group_id
		, 'USER' AS responsible_type
		, coalesce(hash.asset_name, pre.asset_nk) as asset_name_nk
		, now() as inserted_datetime
		, case 
			when hash.responsibility_nk is null then 'I'
			when pre.responsibility_nk is null and hash.asset_name  in (select asset_nk from tmp_dpc_pre_asset)  then 'D' --- если ассет не под удаление, но мы не приджойнились то либо нет больше такой роли, либо больше нет такого юзера
			when pre.responsibility_nk is null and hash.asset_name not in (select asset_nk from tmp_dpc_pre_asset) then 'N'
		  end as action_decision
		, null::text as action_process_status
		, hash.responsibility_id as matched_responsibility_id 
	from resp_hash hash
	full join tmp_dpc_pre_responsibility pre
		on hash.responsibility_nk = pre.responsibility_nk
	WHERE 1=1 
		AND  NOT (coalesce(hash.responsibility_nk,'null') = coalesce(pre.responsibility_nk, 'null'));
	

	logger = 'Завершаем работу';
	update extract_meta.extract_job 
	set 
	job_status = 'STAGE_COMPLETE'
	, last_status_change_datetime = now()
	where job_id = job_uid;

logger = 'Удаление временных таблиц и вью';	
-- /* DROP VIEWS */
 drop view if exists tmp_dpc;

/* DROP TABLES */
drop table if exists temp_asset;
drop table if exists tmp_dpc_pre_asset;
drop table if exists tmp_dpc_pre_attribute_1;
drop table if exists tmp_dpc_pre_attribute;
drop table if exists  tmp_dpc_pre_relation;
drop table if exists  tmp_dpc_pre_responsibility; 


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
$function$
;
