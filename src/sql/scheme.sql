CREATE SEQUENCE version_id_seq;
CREATE TABLE versions (
  id           INTEGER PRIMARY KEY DEFAULT nextval('version_id_seq'),
  key          VARCHAR(255) NOT NULL,
  commit       VARCHAR(40)  NOT NULL,
  hidden       BOOLEAN             DEFAULT FALSE,
  tree         TEXT,
  search       TEXT,
  show_samples BOOLEAN             DEFAULT TRUE
);


CREATE SEQUENCE page_id_seq;
CREATE TYPE PAGE_TYPE AS ENUM ('namespace', 'class', 'typedef', 'enum');
CREATE TABLE pages (
  id         INTEGER PRIMARY KEY DEFAULT nextval('page_id_seq'),
  type       VARCHAR(100),
  version_id INTEGER REFERENCES versions (id),
  url        VARCHAR(255) NOT NULL,
  full_name  VARCHAR(255),
  content    JSONB
);


CREATE TABLE sitemap (
  page_url      VARCHAR(255),
  version_id    INTEGER REFERENCES versions (id),
  last_modified BIGINT
);
