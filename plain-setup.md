# Requirenments

## packages

Add to /etc/apt/sources.list:
```
deb http://nginx.org/packages/mainline/ubuntu/ trusty nginx
deb-src http://nginx.org/packages/mainline/ubuntu/ trusty nginx
```

Добавить ключ nginx:
```
wget http://nginx.org/keys/nginx_signing.key
apt-key add nginx_signing.key
rm nginx_signing.key
```

Обновить репы:
```
apt-get update
```

Установить нужный софт:
```
apt-get install openjdk-7-jre supervisor nginx git redis-server nodejs npm
```

## Configure firewall
```
ufw allow ssh
ufw allow http
ufw enable
ufw status
```
status должен выдать такое:
```
Status: active

To                         Action      From
--                         ------      ----
22                         ALLOW       Anywhere
80                         ALLOW       Anywhere
22 (v6)                    ALLOW       Anywhere (v6)
80 (v6)                    ALLOW       Anywhere (v6)
```

# App structure
```
mkdir /apps
mkdir /apps/reference
mkdir /apps/reference/data
mkdir /apps/reference/keys
mkdir /apps/reference/data/versions
mkdir /apps/reference/data/versions-data
mkdir /apps/reference/data/samples-versions
cd /apps/reference
npm install jsdoc
ln -s node_modules/jsdoc jsdoc
```

# supervisor config

Создаем файл `/etc/supervisor/conf.d/reference.conf`

```
[program:reference]
command=java -Dprod=true -jar /apps/reference/reference-0.2.0-standalone.jar api.anychart.com
directory=/apps/reference
stdout_logfile=/var/log/supervisor/reference.out.log
stderr_logfile=/var/log/supervisor/reference.err.log
environment=TIMBRE_LOG_LEVEL="info"
```

Для prod меняем `api.anychart.stg` на `api.anychart.com` и удаляем `-Dprod=true`

Применяем изменения:
```
supervisorctl reread
supervisorctl update
supervisorctl status
```
`BACKOFF` это нормально - мы еще не выложили приложение

# nginx config

Правим файл `/etc/nginx/nginx.conf`
Заменяем строчку 
```
#gzip on;
```
на
```
gzip on;
```


Создаем файл `/etc/nginx/conf.d/api.anychart.com.conf`
```
upstream http_backend {
    server 127.0.0.1:9197;
    keepalive 32;  # both http-kit and nginx are good at concurrency
}

server {
    listen       80;
    server_name  api.anychart.com;

    gzip on;
    gzip_types    text/plain application/javascript application/x-javascript text/javascript text/xml text/css text/html;

    rewrite ^(.*)\.html $1 redirect;

    location / {
        proxy_pass  http://http_backend;

        # tell http-kit to keep the connection
        proxy_http_version 1.1;
        proxy_set_header Connection "";

        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $http_host;

        access_log  /var/log/nginx/api.access.log;
    }

    location =/_plz_ {
        satisfy any;
        allow 192.30.252.0/22;
        auth_basic "Unauthorized";
        auth_basic_user_file /etc/nginx/conf.d/htpasswd;

        proxy_pass  http://http_backend;

        # tell http-kit to keep the connection
        proxy_http_version 1.1;
        proxy_set_header Connection "";

        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $http_host;
    }
}
```

Для staging меняем `server_name  api.anychart.com;` на `server_name  api.anychart.stg;`
+ убираем секцию _plz_

Создаем файл `/etc/nginx/conf.d/htpasswd`
```
robot:$apr1$uS/pYA8p$YOHNc2Lzy98ozGuNZEUWK1
```

Перезапускаем nginx:
```
service nginx configtest
service nginx restart
```

# Reference git keys

Создаем файл `/apps/reference/keys/git`:
```
#!/bin/sh
exec /usr/bin/ssh -o StrictHostKeyChecking=no -i /apps/reference/keys/id_rsa "$@"
```

Так же кладем приватный `id_rsa` и публичный `id_rsa.pub` ключи в папку `/apps/playground/keys`
Я использую ключи anychart large conference room. По этим ключам должен быть разрешен доступ ко всем проектам, которые есть в playground.

Настраиваем права:
```
cd /apps/reference/keys
chmod +x git
chmod go-rwx id_rsa
chmod go-rwx id_rsa.pub
```

Подключаем ключи:
```
export GIT_SSH="/apps/reference/keys/git"
```

# Reference initial checkouts
```
cd /apps/reference/data/
git clone git@github.com:AnyChart/ACDVF.git repo
git clone git@github.com:AnyChart/ACDVF-docs-playground-samples.git samples-repo
```

# Reference local build
В экстремальной ситуации делается на osx, требуется nodejs руби, redis, compass и git
```
compass compile
lein cljsbuild once
lein uberjar
```

Должно закончится вот этим:
```
Compiling reference.handler
Created /Users/alex/Work/anychart/reference-engine/target/reference-0.2.0.jar
Created /Users/alex/Work/anychart/reference-engine/target/reference-0.2.0-standalone.jar
```

# Manual deploy
Подключаемся к серверу по sftp (в отдельном окошке) и заливаем получившуюся `reference-0.2.0-standalone.jar` в папку `/apps/reference`, в моем случае локальный путь к uberjar: `/Users/alex/Work/anychart/reference-engine/target/reference-0.2.0-standalone.jar` и для меня вот так:
```
sftp root@server-ip
sftp> cd /apps/reference
sftp> put /Users/alex/Work/anychart/reference-engine/target/reference-0.2.0-standalone.jar
```

# Запускаем reference
По ssh идем на сервер и вперед:
```
supervisorctl start reference
```

TO BE CONTINUED!!
