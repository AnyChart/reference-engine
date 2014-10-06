# Run local docs server on OSX:
## Requirenments
* Java >= 1.7
  ```
  java -version
  ```
  Если версия младше 1.7, то идем сюда:
  http://www.oracle.com/technetwork/java/javase/downloads/index.html
  и устанавливаем jdk.
* nodejs http://nodejs.org/

## Install
* Качаем https://dl.dropboxusercontent.com/u/55760684/anychart/reference-engine-0.1-standalone.jar и кладем куда-нибудь, например в 
``` ~/Work/anychart/reference-engine ```
* Открываем эту папку в терминале
* Запускаем ``` jar xf reference-engine-0.1-standalone.jar jsdoc ```

## Running

Запускаем jar, указывая путь к исходникам. Например:
``` 
java -jar reference-engine-0.1-standalone.jar ~/Work/anychart/ACDVF/src
```
Сервер запущен, при измении исходников документация автоматически перегенерится для измененного файла. Для полного ребилда документации достаточно остановить сервер и запустить его заново. Браузер откроется сам.
