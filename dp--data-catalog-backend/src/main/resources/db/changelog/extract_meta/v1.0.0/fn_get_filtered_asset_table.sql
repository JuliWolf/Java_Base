-- DROP FUNCTION extract_delta.fn_get_filtered_asset_table(uuid, text);

CREATE OR REPLACE FUNCTION extract_delta.fn_get_filtered_asset_table(job_uid uuid, source_name text)
 RETURNS TABLE(asset_id uuid, asset_name text, asset_type_id uuid, asset_displayname text, lifecycle_status uuid, stewardship_status uuid)
 LANGUAGE plpgsql
AS $function$
declare
	 rw record; 
     tp text;
     asset_type_id uuid;
     asset_ids_inc uuid[] := '{}';
     asset_ids_inc_child uuid[] := '{}';
	 result_asset_ids_inc uuid[] := '{}';
     asset_ids_exc uuid[] := '{}';
	 asset_ids_exc_child uuid[] := '{}';
	 result_asset_ids_exc uuid[] := '{}';
     cond text;
     query text;
     id uuid;
	 fill_assets_lsts text;
	 cond_inc text;
	 cond_exc text;
     final_qry text;

begin

		drop table if exists inc_exc_table;
		
		create temp table inc_exc_table as 
		select * from json_populate_recordset(null::filter_criteria_json,  (select filter_criteria::json from extract_meta.extract_job where job_id = job_uid ));
		
		
		for rw in (select "type","object_type", "condition_type", "value" from  inc_exc_table) loop
			tp := rw."type";

			asset_type_id := case 
				when rw."object_type" = 'table' then (select at2.asset_type_id from public.asset_type at2 where asset_type_name = 'Таблица')
				when rw."object_type" = 'view' then (select at2.asset_type_id from public.asset_type at2 where asset_type_name = 'Представление')
				when rw."object_type" = 'schema' then (select at2.asset_type_id from public.asset_type at2 where asset_type_name = 'Схема')
				when rw."object_type" = 'column' then (select at2.asset_type_id from public.asset_type at2 where asset_type_name = 'Колонка')
				end;
			   
			cond := case
					when rw.condition_type = 'EQ'  then concat('asset_displayname = ''', rw.value,'''')
					when rw.condition_type = 'LIKE' then concat('asset_displayname like ''', rw.value,'''')
					when rw.condition_type = 'REGEXP'  then concat('asset_displayname ~ ''', rw.value,'''')
				end;
				RAISE notice '%',asset_type_id;
				RAISE notice '%',cond;

			drop table if exists asset_ids;
			
			query := FORMAT('
			create temp table asset_ids AS
				select ast.asset_id 
				from public.asset ast
				where 1=1 
				and deleted_flag = false 
				and ast.%s 
				and ast.asset_type_id = ''%s''  
				and ast.asset_name like ''%s%%'' ',cond, asset_type_id, source_name);
			raise notice '%',query;
			execute query;
			
			if tp = 'INCLUDE'
			then 
				FOR id IN (SELECT aids.asset_id FROM asset_ids aids) LOOP
				asset_ids_inc := array_append(asset_ids_inc, id::uuid);  -- Добавляем значение в массив
				END LOOP;
			elsif tp = 'EXCLUDE' 
			then 
				FOR id IN (SELECT aids.asset_id FROM asset_ids aids) LOOP
				asset_ids_exc := array_append(asset_ids_exc, id::uuid);  -- Добавляем значение в массив
				END LOOP;
			end if;

		end loop;
		
		RAISE notice '%',asset_ids_exc;
		RAISE notice '%',asset_ids_inc;
	
		-- ТЕПЕРЬ НАЙДЕМ ВСЕ ДОЧЕРНИЕ АССЕТЫ И ДОБАВИМ ИХ В СПИСКИ 
		-- ВЕРНЕМ УСЛОВИЕ ВИДА WHERE ASSET_ID IN (asset_ids_inc) AND ASSET_ID NOT IN (asset_ids_exc) -  ЕСЛИ КАКОЙ-ТО СПИСОК ПУСТОЙ, ТО ОПУСТИМ ЭТО 
	
		--TODO: ПОДУМАТЬ НАД РЕКУРСИВНОЙ СТЕ 
		 fill_assets_lsts :=  'SELECT  
			array_agg(r2.asset_id)   as asset_2_id
		FROM (select ast.asset_id, ast.asset_name
				  from public.asset ast
				  where 1=1 
					and deleted_flag = false 
					and ast.asset_id=any($1) 
			 ) ast1
		JOIN public.relation_component r1
				on ast1.asset_id = r1.asset_id 
				and r1.hierarchy_role = ''PARENT''
		JOIN public.relation_component r2 
			on r1.relation_id = r2.relation_id
			and r2.hierarchy_role = ''CHILD''
			and r2.deleted_flag = false';

		EXECUTE fill_assets_lsts INTO asset_ids_inc_child USING asset_ids_inc;

		EXECUTE fill_assets_lsts INTO result_asset_ids_inc USING asset_ids_inc_child;

		SELECT asset_ids_inc || asset_ids_inc_child || result_asset_ids_inc INTO result_asset_ids_inc;
				
		
		EXECUTE fill_assets_lsts INTO asset_ids_exc_child USING asset_ids_exc;

		EXECUTE fill_assets_lsts INTO result_asset_ids_exc USING asset_ids_exc_child;

		SELECT asset_ids_exc || asset_ids_exc_child || result_asset_ids_exc INTO result_asset_ids_exc;

	if result_asset_ids_inc != '{}'
	then
		cond_inc := concat('and ast.asset_id = any(''',result_asset_ids_inc,''')');
	else 
		cond_inc := '';
	end if;

	if result_asset_ids_exc != '{}'
	then
		cond_exc := concat('and ast.asset_id != all(''',result_asset_ids_exc,''')');
	else 
		cond_exc :=  '';
	end if;
		
	final_qry := format('
	select 
		ast.asset_id 
		, ast.asset_name  
		, ast.asset_type_id 
		, ast.asset_displayname 
		, ast.lifecycle_status 
		, ast.stewardship_status 
	from public.asset ast
	where 1=1
	and deleted_flag = false
	and (asset_name = ''%s'' or  ast.asset_name like ''%s>%%'')
	%s
	%s ', source_name, source_name, cond_inc, cond_exc) ;

	raise notice 'FINAL QUERY %',final_qry;

	return query
	EXECUTE final_qry ;


end;
$function$
;
