DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'filter_criteria_type') THEN
        CREATE TYPE filter_criteria_type AS ENUM ('include', 'exclude');
    END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'filter_criteria_object_type') THEN
        CREATE TYPE filter_criteria_object_type AS ENUM ('schema', 'table', 'view', 'column');
    END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'filter_criteria_condition_type') THEN
		CREATE TYPE filter_criteria_condition_type AS ENUM ('eq', 'like', 'regexp_like');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'filter_criteria_json') THEN
	CREATE TYPE filter_criteria_json AS (
		"type"  filter_criteria_type,
		object_type filter_criteria_object_type,
		condition_type filter_criteria_condition_type,
		value varchar(300)
	);
	end if;
END $$;

CREATE TABLE IF NOT EXISTS extract_delta.log_asset_type_changes(
	job_id uuid NOT NULL,
	asset_name text NOT NULL,
	old_asset_type_id uuid NULL,
	new_asset_type_id uuid NULL,
	fix_flg BOOLEAN,
	CONSTRAINT pk_log_asset_type_changes PRIMARY KEY (job_id,asset_name),
	CONSTRAINT fk_log_asset_type_changes_on_job FOREIGN KEY (job_id) REFERENCES extract_meta.extract_job(job_id) ON DELETE CASCADE
);
