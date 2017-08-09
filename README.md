# AnyChart API Reference Engine


## Setup local database
```
sudo -u postgres psql

CREATE USER reference_user WITH PASSWORD 'pass';

CREATE DATABASE reference_db;

GRANT ALL PRIVILEGES ON DATABASE reference_db TO reference_user;

```