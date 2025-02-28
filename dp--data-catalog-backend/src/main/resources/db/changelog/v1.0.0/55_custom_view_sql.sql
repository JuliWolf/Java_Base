Insert into public.custom_view (custom_view_id, asset_type_id, custom_view_name, header_row_names, header_prepare_query, header_select_query, header_clear_query, table_column_names, table_prepare_query, table_select_query, table_clear_query, role_id) VALUES('405d11b4-2e80-4d8a-ad7f-731c9067b905', '00000000-0000-0000-0001-000400000002', 'Состав схемы', '[]', null, null, null, '[{"column_kind": "RTF", "column_name": "Название объекта"}, {"column_kind": "SINGLE_VALUE_LIST", "column_name": "Тип объекта"}, {"column_kind": "RTF", "column_name": "Описание объекта"}]', null, 'select concat(''<p><a href='',rc2.asset_id,''>'',a.asset_displayname,''</a></p>'') as TableName,
at1.asset_type_name as TableType, a2.value as TableDescription
from relation_component rc
inner join relation r
on r.relation_id = rc.relation_id and rc.asset_id = :assetId and r.relation_type_id = ''00000000-0000-0000-0000-000000007043'' and not rc.deleted_flag
inner join relation_component rc2
on rc.relation_id = rc2.relation_id and rc.asset_id != rc2.asset_id
inner join asset a
on a.asset_id = rc2.asset_id
left join "attribute" a2 on not a2.deleted_flag and a2.attribute_type_id = ''00000000-0000-0000-0000-000000003114'' and a2.asset_id = rc2.asset_id
inner join asset_type at1 on
at1.asset_type_id = a.asset_type_id
order by a.asset_displayname ', null, null) ON CONFLICT DO nothing;

Insert into public.custom_view (custom_view_id, asset_type_id, custom_view_name, header_row_names, header_prepare_query, header_select_query, header_clear_query, table_column_names, table_prepare_query, table_select_query, table_clear_query, role_id) VALUES('4cef9a41-29d7-4172-a37e-443415a2fbf5', '00000000-0000-0000-0000-000000031006', 'Состав БД', '[]', null, null, null, '[{"column_kind": "RTF", "column_name": "Название схемы"}, {"column_kind": "RTF", "column_name": "Описание схемы"}, {"column_kind": "MULTIPLE_VALUE_LIST", "column_name": "Владелец"}]', null, 'select concat(''<p><a href='',rc2.asset_id,''>'',a.asset_displayname,''</a></p>'') as schemaName,
a2.value as schemaDescription,
string_agg(concat(u.first_name, '' '', u.last_name),'';'' order by concat(u.first_name, '' '', u.last_name)) as schemaOwner
from relation_component rc
inner join relation r
on r.relation_id = rc.relation_id and rc.asset_id = :assetId and r.relation_type_id = ''00000000-0000-0000-0000-000000007024'' and not rc.deleted_flag
inner join relation_component rc2
on rc.relation_id = rc2.relation_id and rc.asset_id != rc2.asset_id
inner join asset a on a.asset_id = rc2.asset_id and a.lifecycle_status = ''17d55ff5-9659-4151-9ef8-4e2886f54dd5''
left join "attribute" a2
on not a2.deleted_flag and a2.attribute_type_id = ''00000000-0000-0000-0000-000000003114'' and a2.asset_id = rc2.asset_id
left join responsibility r2
on r2.asset_id = a.asset_id and not r.deleted_flag and r2.role_id = ''00000000-0000-0000-0000-000000005040''
left join "user" u
on u.user_id = r2.user_id
group by schemaName, asset_displayname,schemaDescription
order by a.asset_displayname ', null, null) ON CONFLICT DO nothing;

Insert into public.custom_view (custom_view_id, asset_type_id, custom_view_name, header_row_names, header_prepare_query, header_select_query, header_clear_query, table_column_names, table_prepare_query, table_select_query, table_clear_query, role_id) VALUES('fe1dd959-0246-4a6a-a69e-054d73ceee76', '00000000-0000-0000-0001-000400000009', 'Структура представления', '[]', null, null, null, '[{"column_kind": "RTF", "column_name": "Название колонки"}, {"column_kind": "RTF", "column_name": "Описание колонки"}, {"column_kind": "TEXT", "column_name": "Номер колонки"}, {"column_kind": "TEXT", "column_name": "Тип данных"}, {"column_kind": "BOOLEAN", "column_name": "Nullable"}, {"column_kind": "BOOLEAN", "column_name": "Первичный ключ"}]', null, 'select concat(''<p><a href='',rc2.asset_id,''>'',a.asset_displayname,''</a></p>'') as columnName,
a2.value as columnDescription,
cast(a3.value as integer) as columnPosition,
a4.value as columnDateType,
a5.value as columnNullable,
a6.value as columnPrimaryKey from relation_component rc inner join relation r
on r.relation_id = rc.relation_id and rc.asset_id = :assetId and r.relation_type_id = ''00000000-0000-0000-0000-000000007042'' and not rc.deleted_flag
inner join relation_component rc2
on rc.relation_id = rc2.relation_id and rc.asset_id != rc2.asset_id
inner join asset a
on a.asset_id = rc2.asset_id
left join "attribute" a2
on not a2.deleted_flag and a2.attribute_type_id = ''00000000-0000-0000-0000-000000003114'' and a2.asset_id = rc2.asset_id
left join "attribute" a3
on not a3.deleted_flag and a3.attribute_type_id = ''00000000-0000-0000-0001-000500000020'' and a3.asset_id = rc2.asset_id
left join "attribute" a4
on not a4.deleted_flag and a4.attribute_type_id = ''00000000-0000-0000-0000-000000000219'' and a4.asset_id = rc2.asset_id
left join "attribute" a5
on not a5.deleted_flag and a5.attribute_type_id = ''00000000-0000-0000-0001-000500000011'' and a5.asset_id = rc2.asset_id
left join "attribute" a6
on not a6.deleted_flag and a6.attribute_type_id = ''00000000-0000-0000-0001-000500000015'' and a6.asset_id = rc2.asset_id
order by columnPosition', null, null) ON CONFLICT DO nothing;

Insert into public.custom_view (custom_view_id, asset_type_id, custom_view_name, header_row_names, header_prepare_query, header_select_query, header_clear_query, table_column_names, table_prepare_query, table_select_query, table_clear_query, role_id) VALUES('ce244fd5-55c9-41dd-88e0-f08a49edb45b', '00000000-0000-0000-0000-000000011001', 'Атрибутный состав термина', '[{"row_kind": "RTF", "row_name": "Тех.имя термина"}, {"row_kind": "RTF", "row_name": "Синонимы термина"}]', null, 'select a.value as tech_name, a2.value as synonyms from asset ast left join "attribute" a on ast.asset_id = a.asset_id and not a.deleted_flag and a.attribute_type_id = ''fb4e656f-6c3d-4c73-b09a-00baa40d44b6'' left join "attribute" a2 on ast.asset_id = a2.asset_id and not a2.deleted_flag and a2.attribute_type_id = ''29942fad-13e0-4770-9eec-55e78896f4ac'' where ast.asset_id = :assetId limit 1', null, '[{"column_kind": "RTF", "column_name": "Бизнес-атрибут"}, {"column_kind": "RTF", "column_name": "Определение атрибута"}, {"column_kind": "TEXT", "column_name": "Тех.имя атрибута"}, {"column_kind": "BOOLEAN", "column_name": "Атрибут наследован?"}, {"column_kind": "RTF", "column_name": "Наследовано от"}]', 'drop table if exists bt_inheritance; create temp table bt_inheritance as (select rc.asset_id as parent_bt_id, rc2.asset_id as child_bt_id from relation_component rc inner join relation_component rc2 on rc.relation_type_component_id = ''9e2f472e-08ef-48ef-a63d-b5071aabfecf'' and rc.relation_id = rc2.relation_id and not rc.deleted_flag and rc.asset_id != rc2.asset_id);', 'WITH RECURSIVE chain(from_id, to_id) AS (
    SELECT NULL, cast(:assetId as text)
    UNION
    SELECT c.to_id, cast(bti."parent_bt_id" as text)
    FROM chain c
             LEFT OUTER JOIN bt_inheritance bti ON (cast(bti."child_bt_id" as text) = to_id)
    WHERE c.to_id IS NOT NULL
)

select concat(''<p><a href='',a2.asset_id,''>'',a2.asset_displayname,''</a></p>'') as businessAttribute,
       att.value as ba_desc,
       att2.value as ba_tech_name,
       (case when a.asset_id = :assetId then false
       else true end) as inheritedFlag,
       (case when a.asset_id != :assetId then concat(''<p><a href='',a.asset_id,''>'',a.asset_displayname,''</a></p>'')
       else null end) as businessTermFrom
from asset a
         inner join
     (select cast(from_id as uuid) as path_elem_id FROM chain where from_id is not null) path_elems
     on path_elems.path_elem_id = a.asset_id
         inner join relation_component rc
                    on rc.asset_id = path_elem_id and not rc.deleted_flag and rc.relation_type_component_id = ''eb9fb489-72be-4e0d-8fad-41b567f2dbb9''
         inner join relation_component rc2
                    on rc.relation_id = rc2.relation_id and rc.asset_id != rc2.asset_id
         inner join asset a2
                    on a2.asset_id = rc2.asset_id
         left join "attribute" att
                   on att.attribute_type_id = ''00000000-0000-0000-0000-000000003114'' and att.asset_id = a2.asset_id and not att.deleted_flag
         left join "attribute" att2
                   on att2.attribute_type_id = ''fb4e656f-6c3d-4c73-b09a-00baa40d44b6'' and att2.asset_id = a2.asset_id and not att2.deleted_flag
                   order by a2.asset_displayname;', 'drop table bt_inheritance;', null) ON CONFLICT DO nothing;



Insert into public.custom_view (custom_view_id, asset_type_id, custom_view_name, header_row_names, header_prepare_query, header_select_query, header_clear_query, table_column_names, table_prepare_query, table_select_query, table_clear_query, role_id) VALUES('01030498-5b28-4493-af1c-67fabf117053', '00000000-0000-0000-0000-000000031007', 'Структура таблицы', '[{"row_kind": "TEXT", "row_name": "Расписание обновления"}, {"row_kind": "SINGLE_VALUE_LIST", "row_name": "Уровень конфиденциальности таблицы"}]', null, 'select a.value as refresh_schedule, a2.value as conf from asset ast left join "attribute" a on ast.asset_id = a.asset_id and not a.deleted_flag and a.attribute_type_id = ''00000000-0000-0000-0001-000500000003'' left join "attribute" a2 on ast.asset_id = a2.asset_id and not a2.deleted_flag and a2.attribute_type_id = ''eb633e06-cdd3-43b4-bdcb-9a31859211fe'' where ast.asset_id = :assetId limit 1', null, '[{"column_kind": "RTF", "column_name": "Название колонки"}, {"column_kind": "RTF", "column_name": "Описание колонки"}, {"column_kind": "INTEGER", "column_name": "Номер колонки"}, {"column_kind": "TEXT", "column_name": "Тип данных"}, {"column_kind": "BOOLEAN", "column_name": "Вхождение в PK"}, {"column_kind": "BOOLEAN", "column_name": "Nullable"}]', null, 'select concat(''<p><a href='',rc2.asset_id,''>'',a.asset_displayname,''</a></p>'') as columnName, a2.value as columnDescription, cast(a3.value as integer) as columnPosition, a4.value as columnDateType, a5.value as columnNullable, a6.value as columnPrimaryKey from relation_component rc inner join relation r on r.relation_id = rc.relation_id and rc.asset_id = :assetId and r.relation_type_id = ''00000000-0000-0000-0000-000000007042'' and not rc.deleted_flag inner join relation_component rc2 on rc.relation_id = rc2.relation_id and rc.asset_id != rc2.asset_id inner join asset a on a.asset_id = rc2.asset_id left join "attribute" a2 on not a2.deleted_flag and a2.attribute_type_id = ''00000000-0000-0000-0000-000000003114'' and a2.asset_id = rc2.asset_id left join "attribute" a3 on not a3.deleted_flag and a3.attribute_type_id = ''00000000-0000-0000-0001-000500000020'' and a3.asset_id = rc2.asset_id left join "attribute" a4 on not a4.deleted_flag and a4.attribute_type_id = ''00000000-0000-0000-0000-000000000219'' and a4.asset_id = rc2.asset_id left join "attribute" a5 on not a5.deleted_flag and a5.attribute_type_id = ''00000000-0000-0000-0001-000500000011'' and a5.asset_id = rc2.asset_id left join "attribute" a6 on not a6.deleted_flag and a6.attribute_type_id = ''00000000-0000-0000-0001-000500000015'' and a6.asset_id = rc2.asset_id order by columnPosition', null, null) ON CONFLICT DO nothing;
