CREATE SEQUENCE version_id_seq;
CREATE TABLE versions (
  id integer PRIMARY KEY DEFAULT nextval('version_id_seq'),
  key varchar(255) not NULL,
  commit varchar(40) not NULL,
  hidden BOOLEAN DEFAULT FALSE,
  tree TEXT,
  search TEXT,
  show_samples BOOLEAN DEFAULT TRUE
);

CREATE SEQUENCE page_id_seq;
CREATE TYPE page_type AS ENUM ('namespace', 'class', 'typedef', 'enum');
CREATE TABLE pages (
   id integer PRIMARY KEY DEFAULT nextval('page_id_seq'),
   type varchar(100),
   version_id integer references versions(id),
   url varchar(255) not null,
   full_name varchar(255),
   content jsonb
)

create table sitemap (
  page_url varchar(255),
  version_id integer references versions(id),
  last_modified bigint
);

CREATE MATERIALIZED VIEW search_table AS
select
  version_id,
  type,
  json ->> 'name' as name,
  full_name || '.' || (json ->> 'name') as full_name,
  regexp_replace(json ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(json ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(json ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  full_name || '#' || (json ->> 'name') as link
FROM
  (select json_array_elements ((content -> 'methods') :: json) as json, full_name, version_id, 'method'::TEXT as type
   from pages
      where type = 'class') AS data
UNION ALL
-- overrides from class methods
select
  version_id,
  type,
  json ->> 'name' as name,
  full_name || '.' || (json ->> 'name') as full_name,
  regexp_replace(json ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(json ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(json ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  full_name || '#' || (json ->> 'name') as link
FROM
  (select json_array_elements ((json -> 'overrides') :: json) as json, full_name, version_id, type FROM
  (select json_array_elements ((content -> 'methods') :: json) as json, full_name, version_id, 'method'::TEXT as type
   from pages
      where type = 'class' and full_name = 'anychart.core.Chart') AS data) as data2
UNION ALL
SELECT
  version_id,
  type,
  json ->> 'name' as name,
  json ->> 'full-name' as full_name,
  regexp_replace(json ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(json ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(json ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  full_name || '#' || (json ->> 'name') as link
FROM
  (select json_array_elements ((content -> 'functions') :: json) as json, full_name, version_id, 'function'::TEXT as type
   from pages
        where type = 'namespace') AS data
UNION ALL
SELECT
  version_id,
  type,
  json ->> 'name' as name,
  json ->> 'full-name' as full_name,
  regexp_replace(json ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(json ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(json ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  full_name || '#' || (json ->> 'name') as link
FROM
  (select json_array_elements ((content -> 'constants') :: json) as json, full_name, version_id, 'constant'::TEXT as type
   from pages
        where type = 'namespace') AS data
UNION ALL
---select fields from enum as enum to searching on them
SELECT
  version_id,
  type,
  name,
  fn,
  regexp_replace(json ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(json ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(json ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  fn as link
FROM
  (select json_array_elements ((content -> 'fields') :: json) as json, content ->> 'name' as name,
     full_name as fn, version_id, 'enum'::TEXT as type
   from pages
        where type = 'enum') AS data
UNION ALL
SELECT
  version_id,
  type,
  content ->> 'name' as name,
  content ->> 'full-name' as full_name,
  regexp_replace(content ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(content ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(content ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  url as link
FROM pages  where type = 'class'
UNION ALL
SELECT
  version_id,
  type,
  content ->> 'name' as name,
  content ->> 'full-name' as full_name,
  regexp_replace(content ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(content ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(content ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  url as link
FROM pages  where type = 'enum'
UNION ALL
SELECT
  version_id,
  type,
  content ->> 'name' as name,
  content ->> 'full-name' as full_name,
  regexp_replace(content ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(content ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(content ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  url as link
FROM pages  where type = 'typedef'
UNION ALL
SELECT
  version_id,
  type,
  content ->> 'name' as name,
  content ->> 'full-name' as full_name,
  regexp_replace(content ->> 'short-description', '(<[^>]*>|{[^}]*})', '', 'g')  as short_description,
  regexp_replace(content ->> 'description', '(<[^>]*>|{[^}]*})', '', 'g')  as description,
  regexp_replace(content ->> 'detailed', '(<[^>]*>|{[^}]*})', '', 'g')  as detailed,
  url as link
FROM pages  where type = 'namespace';


-- To change pages type:
-- ALTER TABLE pages ALTER COLUMN content TYPE JSONB USING content::JSONB;
-- ALTER TABLE pages ALTER COLUMN content TYPE TEXT USING content;

-- To refresh view:
-- REFRESH MATERIALIZED VIEW search_table;