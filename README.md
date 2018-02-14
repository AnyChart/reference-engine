[<img src="https://cdn.anychart.com/images/logo-transparent-segoe.png?2" width="234px" alt="AnyChart - Robust JavaScript/HTML5 Chart library for any project">](https://anychart.com)

# AnyChart API Reference Engine

[Production](http://api.anychart.com) 
[![Build Status](https://travis-ci.com/AnyChart/reference-engine.svg?token=ERMLfyrvWdA8g6gi11Vp&branch=master)](https://travis-ci.com/AnyChart/reference-engine)

[Staging](http://api.anychart.stg) 
[![Build Status](https://travis-ci.com/AnyChart/reference-engine.svg?token=ERMLfyrvWdA8g6gi11Vp&branch=staging)](https://travis-ci.com/AnyChart/reference-engine)

## Set up 
```
1. Put api.anychart.com repository to data/repo folder
2. Set nginx to route: data/versions-static -> http://domain/si
3. Set up ts-tests to data/ to check TypeScript index.d.ts

```


## Install dependencies
```
# to build js
sudo apt-get install closure-compiler

sudo npm install -g jsdoc
sudo ln -s /usr/bin/jsdoc /usr/local/bin/jsdoc
```


## Setup local database
```
sudo -u postgres psql

CREATE USER reference_user WITH PASSWORD 'pass';

CREATE DATABASE reference_db;

GRANT ALL PRIVILEGES ON DATABASE reference_db TO reference_user;

\c reference_db

# create scheme

```