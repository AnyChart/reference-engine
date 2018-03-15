[<img src="https://cdn.anychart.com/images/logo-transparent-segoe.png?2" width="234px" alt="AnyChart - Robust JavaScript/HTML5 Chart library for any project">](https://anychart.com)

# AnyChart API Reference Engine

[AnyChart API Reference](https://api.anychart.com) is a place where users
can find a detailed description of each 
namespace, class, method or property, along with ready-to-try samples. 
The application consists of two parts: the web part and the generator.
The Web part is just a site responsible for displaying the content, and the generator
is a parser whose main task is to parse the 
[corresponding repository](https://github.com/AnyChart/api.anychart.com).
There are all AnyChart namespaces described in JsDoc format in that repository.
The application backend is written on Clojure and the frontend on Javascript with jQuery.



[![Build Status](https://travis-ci.com/AnyChart/reference-engine.svg?token=ERMLfyrvWdA8g6gi11Vp&branch=master)](https://travis-ci.com/AnyChart/reference-engine)
[Production](http://api.anychart.com) 

[![Build Status](https://travis-ci.com/AnyChart/reference-engine.svg?token=ERMLfyrvWdA8g6gi11Vp&branch=staging)](https://travis-ci.com/AnyChart/reference-engine)
[Staging](http://api.anychart.stg) 

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
The application uses PostgreSQL, so you need to create database and user:
```
sudo -u postgres psql
CREATE USER reference_user WITH PASSWORD 'pass';
CREATE DATABASE reference_db;
GRANT ALL PRIVILEGES ON DATABASE reference_db TO reference_user;
\c reference_db
# create scheme

```


## License
If you have any questions regarding licensing - please contact us. <sales@anychart.com>