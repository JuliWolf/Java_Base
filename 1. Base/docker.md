+ [Core concepts](#core-concepts)
+ [Commands](#commands)
+ [Kubernetes](#kubernetes)


## Core concepts

+ [1. Что такое докер](#1-что-такое-докер)
+ [2. Docker image](#2-docker-image)
+ [3. Docker container](#3-docker-container)
+ [4. Layer based instruction](#4-layer-based-instruction)
+ [5. Volumes](#5-volumes)
+ [6. Bind mounts](#6-bind-mounts)
+ [7. Volumes vs Bind mounts](#7-volumes-vs-bind-mounts)
+ [8. Read only volume](#8-read-only-volume)
+ [9. ENV](#9-env)
+ [10. ARGUMENTS](#10-arguments)
+ [11. Network](#11-network)
+ [12. Docker compose](#12-docker-compose)

### 1. Что такое докер

Docker — это платформа, которая позволяет упаковать в контейнер приложение со всем окружением и зависимостями, а затем доставить и запустить его в целевой системе.<br>
Приложение, упакованное в контейнер, изолируется от операционной системы и других приложений. <br>
Поэтому разработчики могут не задумываться, в каком окружении будет работать их приложение, а инженеры по эксплуатации — единообразно запускать приложения и меньше заботиться о системных зависимостях.


### 2. Docker image
Образ Docker (Docker Image) - это неизменяемый файл, содержащий исходный код, библиотеки, зависимости, инструменты и другие файлы, необходимые для запуска приложения.<br>
Это шаблон для контейнера.<br>

Они представляют приложение и его виртуальную среду в определенный момент времени. Такая согласованность является одной из отличительных особенностей Docker. <br>
Он позволяет разработчикам тестировать и экспериментировать программное обеспечение в стабильных, однородных условиях.<br>

Образ - это шаблон, на основе которого создается контейнер, существует отдельно и не может быть изменен. <br>
При запуске контейнерной среды внутри контейнера создается копия файловой системы (docker образа) для чтения и записи.<br>

Содержат весь код, инстукции, данные, необходимые для создания контейнера


### 3. Docker container
Контейнер Docker (Docker Container) - это виртуализированная среда выполнения, в которой пользователи могут изолировать приложения от хостовой системы. <br>
Эти контейнеры представляют собой компактные портативные хосты, в которых можно быстро и легко запустить приложение.<br>

Важной особенностью контейнера является стандартизация вычислительной среды, работающей внутри контейнера. <br>
Это не только гарантирует, что ваше приложение работает в идентичных условиях, но и упрощает обмен данными с другими партнерами по команде.<br>

Контейнеры работают автономно, изолированно от основной системы и других контейнеров, и потому ошибка в одном из них не влияет на другие работающие контейнеры, а также поддерживающий их сервер.<br>

В отличие от виртуальных машин, где виртуализация выполняется на аппаратном уровне, контейнеры виртуализируются на уровне приложений.<br>

Работающий инстанс, созданный на основе image


### 4. Layer based instruction
При создании контейнера, докер кеширует состояние исходных файлов.
При повторной попытке создания контейнера на данных, которые не были изменены, докер не будет повторять весь процесс сначала, вместо этого возьмет из кеша
- Каждая команда в докере это отдельный слой, которая кешируется
- Если результат выполнения одной из команд не может быть взят из кеша, то все последующие команды тоже будут перевыполняться, так как нет гарантии, что после выполнения следующих команд результат результат не поменяется
- Все слои в итоге образуют image
- При создании контейнера докер создает еще один слой(layer) на основе image


### 5. Volumes
Volume - том находится не в контейнере, а на жесктном диске системы, которая содержит контейнеры. <br>
Тома являются постоянным хранилищем для контейнеров Docker.<br>
Тома не привязаны к времени жизни контейнера, поэтому данные в них не исчезают при удалении контейнера. <br>
Контейнеры могут добавлять данные в тома и читать данные из томов<br>

Определить папку, которая будет сохранена в Volume можно в DockerFile с помощью команды `VOLUME`
```
FROM node:14

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

EXPOSE 80
# use anonymous volume
VOLUME ["/app/feedback"]

CMD ["node", "server.js"]
```

Существует 2 типа volumes:<br>
- **anonymous volumes** - существуют до тех пор, пока существует контейнер, который к нему обращается
`-v /app/node_modules`<br>
`docker run -p 3000:80 -d --rm --name feedback-app -v feedback:/app/feedback -v /app/node_modules feedback-node:volumes`
- **named volumes**
`-v nodeModules:/app/node_modules`<br>
`docker run -p 3000:80 -d --rm --name feedback-app -v feedback:/app/feedback -v nodeModules:/app/node_modules feedback-node:volumes`


### 6. Bind mounts
Файл или каталог хоста (машина, на которой работает docker) монтируется в контейнер<br>
Запись в примонтированный каталог могут вести программы как в контейнере, так и на хосте<br>

`-v {absolute path to folder}:{where to mount in docker}`<br>
`docker run -p 3000:80 -d --rm --name feedback-app -v feedback:/app/feedback -v "/media/juliwolf/Downloads/test":/app feedback-node:volumes`<br>

`docker run -p 3000:80 -d --rm --name feedback-app -v feedback:/app/feedback -v "/media/juliwolf/Downloads/test":/app -v /app/node_modules feedback-node:volumes`<br>

*Note: Если происходит пересечение папок для создания volume, bind mounts в приоритете будет папка, которая имеет более точнее значение, т.е. `-v /app/node_modules` и поэтому папка `node_modules` не будет переписана в процессе монтирования


### 7. Volumes vs Bind mounts
| Anonymous volume                                                         | Named volume                                | Bind mount                                                                    |
|--------------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------------------------|
| Привязан к одному контейнеру                                             | Не привязан к одному контейнеру             | Не привязан к контейнеру, так как является ссылкой на файл или папку на хосте |
| Существует до тех пор пока контейнер не будет остановлен или перезапущен | Существуют и после остановки контейнера     | Существуют и после остановки контейнера                                       |
| Не может быть использован несколькими контейнерами                       | Можно использовать несколькими контейнерами | Можно использовать несколькими контейнерами                                   |
| Не может быть переиспользован                                            | Можно переиспользовать                      | Можно переиспользовать                                                        |
| Может быть полезен для сохранения файлов/папок внутри контейнера         |                                             |                                                                               |
| Могут быть созданы из Dockerfile с помощью команды `VOLUME`              | Не может быть создан внутри Dockerfile      |                                                                               |


### 8. Read only volume
По дефолту тома имею настройку read-write, то есть контейнерам можно менять данные внутри volume<br>
read-only volume дают права только на чтение информации


### 9. ENV
env - это переменные окружения<br>
переменные можно задать в Dockerfile с помощью команды ENV
```
FROM node:14

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

ENV PORT 80

EXPOSE $PORT

CMD ["node", "server.js"]
```

Переменные окружения можно задать при старте контейнера с помощью команды `--env` или `-e`<br>
`--env` `-e`: `docker run -p 3000:8000 -d --rm --env PORT=8000 --name feedback-app -v feedback:/app/feedback feedback-node:volumes` Создать env переменные для контейнера

Так же можно задать env переменные из файла с помощью команды `--env-file`<br>
`--env-file`: `docker run -p 3000:8000 --env-file ./.env  -d --rm --name feedback-app -v feedback:/app/feedback feedback-node:volumes` Создать env переменные из файла


### 10. ARGUMENTS
Build arguments - это переменные которые передаются во время процесса сборки<br>
Переменные можно задать внутри Dockerfile и назначить им дефолтные значения<br>
```
FROM node:14

ARG DEFAULT_PORT=80

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

ENV PORT $DEFAULT_PORT

EXPOSE $PORT

CMD ["node", "server.js"]
```
С помощью команды `--build-arg` можно задать значение аргументам<br>
`docker build -t feedback-node:dev --build-arg DEFAULT_PORT=8000 .`


### 11. Network
Для обмена данными между контейнерами можно создать `docker network` в рамках которой несколько контейнеров смогут обмениваться данными между друг другом<br>
Можно вручную настроить общение между докерами
- Создать контейнер 1
- Найти информацию об ip адресе контейнера 1 с помощью команды `docker container inspect {containerId/containerName}`
- Создать контейнер 2, используя ip адрес первого контейнера

При использовании `docker network` docker самостоятельно настроит ip адреса<br>
Для обращения к другому контейнеру необходимо указать его имя вместо его ip
```js
mongoose.connect(
  `mongodb://{containerName}:27017/course-goals`
)
```
При отправке запроса приложением на внешний ip адрес, докер определяет к какому контейнеру относится данный запрос и перенаправляет запрос к нужному контейнеру


### 12. Docker compose
Это инструмент для определения и запуска многоконтейнерных docker-приложений<br>
С его помощью можно описать кнфигурацию всех сервисов, сетей и томов, необходимых для приложения и запустить их одной командой<br>
Это упрощает управление сложными приложениями, состоящии из нескольких взаимосвязанных сервисов.

## END ---------------- Core concepts ----------------


## Commands

+ [1. docker build](#1-docker-build)
+ [2. docker run](#2-docker-run)
+ [3. docker ps](#3-docker-ps)
+ [4. docker stop](#4-docker-stop)
+ [5. docker start](#5-docker-start)
+ [6. docker attach](#6-docker-attach)
+ [7. docker logs](#7-docker-logs)
+ [8. docker rm](#8-docker-rm)
+ [9. docker images](#9-docker-images)
+ [10. docker rmi](#10-docker-rmi)
+ [11. docker image prune](#11-docker-image-prune)
+ [12. docker image inspect](#12-docker-image-inspect)
+ [13. docker container prune](#13-docker-container-prune)
+ [14. docker cp](#14-docker-cp)
+ [15. docker tag](#15-docker-tag)
+ [16. docker pull](#16-docker-pull)
+ [17. docker volume ls](#17-docker-volume-ls)
+ [18. docker volume inspect](#18-docker-volume-inspect)
+ [19. docker volume rm](#19-docker-volume-rm)
+ [20. docker volume prune](#20-docker-volume-prune)
+ [21. docker network](#21-docker-network)
+ [22. docker compose up](#22-docker-compose-up)
+ [23. docker compose down](#23-docker-compose-down)
+ [24. docker compose build](#24-docker-compose-build)
+ [25. docker exec](#25-docker-exec)
+ [26. docker compose run](#26-docker-compose-run)
+ [27. docker -(options)](#27-docker--options)


### 1. docker build
Собрать image по Dockerfile из текущей папки<br>
`docker build .`


### 2. docker run
Запустить собранный image по id <br>
`docker run ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd`

Запустить собранный image на определенном порту (на каком порту будет доступен:какой порт докер контейнера)<br>
`docker run -p 3000:3000 ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd`
<br><br>
Дефолтно запускат с attach модом, то есть блокирует консоль, позволяя смотреть лог докер контейнера в консоли


### 3. docker ps
Получить список запущенных контейнеров<br>
`docker ps`
```
CONTAINER ID   IMAGE          COMMAND                  CREATED              STATUS              PORTS                                       NAMES
347a5357cfc6   ac8d5bf025fb   "docker-entrypoint.s…"   About a minute ago   Up About a minute   0.0.0.0:3000->3000/tcp, :::3000->3000/tcp   zealous_lehmann
4ed1e4cbdbe2   ac8d5bf025fb   "docker-entrypoint.s…"   About a minute ago   Up About a minute   3000/tcp  
```

### 4. docker stop
Остановить контейнер по имени<br>
`docker stop zealous_lehmann`


### 5. docker start
Рестартануть ранее остановленный контейнер<br>
`docker start zealous_lehmann`

Дефолтно запускат с dettach модом, то есть не блокирует консоль<br><br>

Запустить контейнер, который был остановлен с attach модом<br>
`docker start -a zealous_lehmann`


### 6. docker attach
Подключить к докер контейнеру для прослушивания логов контейнера<br>
`docker attach zealous_lehmann`


### 7. docker logs
Получить логи контейнера, в том числе те, которые были сохранены при предыдущих запусках<br>
`docker logs zealous_lehmann`
<br><br>
Посмотреть старые логи и начать слушать новые в runtime<br>
`docker logs -f zealous_lehmann`


### 8. docker rm
Удалить не активный контейнер<br>
`docker rm zealous_lehmann`


### 9. docker images
Показать список имеющихся images<br>
`docker images`<br>

```
<none>                <none>    122e98fcbfc4   47 hours ago    1.13GB
<none>                <none>    ac8d5bf025fb   2 days ago      917MB
node                  latest    89871f29e084   9 days ago      1.12GB
postgres              15        f2258b53bc9c   5 months ago    425MB
testcontainers/ryuk   0.5.1     ec913eeff75a   20 months ago   12.7MB
```

### 10. docker rmi
Удалить image<br>
`docker rmi 122e98fcbfc4`


### 11. docker image prune
Удалить все неиспользуемые images<br>
`docker image prune 122e98fcbfc4`
```js
[
    {
        "Id": "sha256:122e98fcbfc4c6186a4384967e6716d9faa071fe7fad4fd97668555789d3648a",
        "RepoTags": [],
        "RepoDigests": [],
        "Parent": "",
        "Comment": "buildkit.dockerfile.v0",
        "Created": "2025-01-29T21:21:12.719709391+03:00",
        "DockerVersion": "",
        "Author": "",
        "Config": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "80/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "NODE_VERSION=23.6.1",
                "YARN_VERSION=1.22.22"
            ],
            "Cmd": [
                "node",
                "server.js"
            ],
            "ArgsEscaped": true,
            "Image": "",
            "Volumes": null,
            "WorkingDir": "/app",
            "Entrypoint": [
                "docker-entrypoint.sh"
            ],
            "OnBuild": null,
            "Labels": null
        },
        "Architecture": "amd64",
        "Os": "linux",
        "Size": 1131769059,
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/vyol4zct2wnakp7nhzurmfev7/diff:/var/lib/docker/overlay2/1x5ksd9kqaq7b1jff18dkr36b/diff:/var/lib/docker/overlay2/8a9a77df6eac7e60d4f0651daf3412df87dbc8293a62d9f4932ebe7ab84b7a48/diff:/var/lib/docker/overlay2/000fec75916a2233ccd5a5ae933ff098117b7b6e7d9e50e2219052b9c393a07a/diff:/var/lib/docker/overlay2/90c7d9d4978a76cd99e9225c32300c41536c30d2f8e7465b1bee239cecf209d3/diff:/var/lib/docker/overlay2/5ea9033cc5eb14cab930a9586792f0b02e3e8b07fbebfa676e2eff19afdce2cb/diff:/var/lib/docker/overlay2/6a6e799377bd59d93e0979d51cfae86116518b3a3aae20015442feffe04c6dbf/diff:/var/lib/docker/overlay2/8a470b4cacf19311d1f1485fc5a5521d197bcf9a0dd82aa1a626b6d32aabf6b3/diff:/var/lib/docker/overlay2/cc206067a4163eb68d6a819af6ae0061d315afeb8f5730d972229a97533a4a77/diff:/var/lib/docker/overlay2/58a573809fc9883fdde8a814798723f863b2ec4f1bed758f2e66a9250c3833c7/diff",
                "MergedDir": "/var/lib/docker/overlay2/oojpsh75pu2x4huurlt4ktrff/merged",
                "UpperDir": "/var/lib/docker/overlay2/oojpsh75pu2x4huurlt4ktrff/diff",
                "WorkDir": "/var/lib/docker/overlay2/oojpsh75pu2x4huurlt4ktrff/work"
            },
            "Name": "overlay2"
        },
        "RootFS": {
            "Type": "layers",
            "Layers": [
                "sha256:397f1b2e2505dea0d7c0b50de353e19504d56946e1dea36c966a419781dc8223",
                "sha256:0e5c23e041eea3f22f26c0e18cc317f4afec966222e6c366101c7360b8f5861d",
                "sha256:f379f60055253177ce53ad242666595be7f6ccd0f02d3ade3aef3a3c57bb9a39",
                "sha256:ebad64620a59cab136095d4224c81a3058054a8c9ea9e8c8221ddc6bdfccf13d",
                "sha256:79d4ceba85ee804e8ba63d318e062bbeb4e17a76dc7531da7e7829fa0d030cd6",
                "sha256:67baf29a39b7fa91aeffb27dabddc1fde6ee52ee29da880d9275ac621786dd02",
                "sha256:8cf30c97a5ec1108cf196ae7d543face5da9d3089a5a92acdaf4479e6c817609",
                "sha256:e039dcc088d50734499272f262a70ed55449cceb914fa324a7e1f0e23ff1c06e",
                "sha256:d8701281607e74df55736b38506870908c96c05a3f0f41b81bb63d22a827e1e4",
                "sha256:66066ce20a7d88f6b3d377d9105f89146f67f46d4627940480c2efd75ab9ec61",
                "sha256:416fbc474a3218824e3636ff9194fdd72c50ea93935b35fa24181e350690cac7"
            ]
        },
        "Metadata": {
            "LastTagTime": "0001-01-01T00:00:00Z"
        }
    }
]
```


### 12. docker image inspect
Посмотреть подробную информацию image<br>
`docker image inspect`


### 13. docker container prune
Удалить все остановленные контейнеры<br>
`docker container prune`


### 14. docker cp
Скопировать данные из контейнера или в контейнер<br>
`docker cp {from_path} {to_path}`<br>
`docker cp test/. distracted_swartz:/test`


### 15. docker tag
Переименовать image - создается клон исходного image<br>
`docker tag {from} {to}`
`docker tag goals:latest juliwolf/node-hello-world:latest`


### 16. docker pull
Загрузить последнюю версию image<br>
`docker pull juliwolf/node-hello-world`


### 17. docker volume ls
Посмотреть список томов в докере<br>
`docker volume ls`


### 18. docker volume inspect
Посмотреть подробную информацию тома<br>
`docker volume inspect feedback`
```js
[
    {
        "CreatedAt": "2025-02-04T21:57:52+03:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/feedback/_data",
        "Name": "feedback",
        "Options": null,
        "Scope": "local"
    }
]
```


### 19. docker volume rm
Удалить том<br>
`docker volume rm feedback-files`


### 20. docker volume prune
Удалить все неиспользуемые тома<br>
`docker volume prune`


### 21. docker network
Создать внутреннюю сеть для докер контейнеров<br>
`docker network create favorites-net`


### 22. docker compose up
Стартует все контейнеры, описанные в файле `docker-compose`<br>
`docker-compose up`


### 23. docker compose down
Останавливает все контейнеры, описанные в файле `docker-compose`<br>
`docker-compose down`


### 24. docker compose build
Только запускает создание всех не созданных images<br>
`docker-compose build`


### 25. docker exec
Выполняет команды внутри консоли конкретного контейнера<br>
`docker exec suspicious_brahmagupta npm init`
Для того, чтобы коннект с консолью выбранного контейнера не терялся, нужно обавить параметр `-it`


### 26. docker compose run
Выполнить команду для докер контейнера описанного в файле<br>
`docker-compose run {containerName} {command}`<br>
Для того, чтобы контейнер был удален автоматически после использования надо добавить `--rm`<br>
`docker-compose run --rm {containerName} {command}`

### 27. docker -(options)

#### docker ps
`-a`: `docker ps -a` говорит о том, что мы хотим увидеть все контейнеры, которые когда либо были созданы, а не только активные

#### docker run
`-d`: `docker run -p 3000:3000 -d ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd` run with detach mode<br>
`-p`: `docker run -p 3000:3000 -d ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd` открыть порт докера по определенному порту<br>
`--rm`: `docker run -p 3000:3000 -d --rm ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd` удалить контейнер когда он остановится<br>
`--name`: `docker run -p 3000:80 -d --rm --name goalsapp 122e98fcbfc4` назначить кастомное имя контейнеру<br>
`-v`: `docker run -p 3000:80 -d --rm --name feedback-app -v feedback:/app/feedback feedback-node:volumes` создать named volume   <br>
`-v ...:ro`: `docker run -p 3000:80 -d --rm --name feedback-app -v feedback:/app/feedback:ro feedback-node:volumes` создать named read only volume   <br>
`--env` `-e`: `docker run -p 3000:8000 -d --rm --env PORT=8000 --name feedback-app -v feedback:/app/feedback feedback-node:volumes` Создать env переменные для контейнера<br>
`--env-file`: `docker run -p 3000:8000 --env-file ./.env  -d --rm --name feedback-app -v feedback:/app/feedback feedback-node:volumes` Создать env переменные из файла<br>
`--network`: `docker run --name mongodb -d --network favorites-net mongo` запустить контейнер с определенным docker-network

#### docker logs
`-f`: `docker logs -f zealous_lehmann` продолжать слушать лог в runtime<br>

#### docker start
`-a`: `docker start -a zealous_lehmann` Запустить остановленный контейнер с attach модом<br>
`-i`: `docker start -i zealous_lehmann` Запустить контейнер в интерактивном режиме<br>
`-t`: `docker start -it zealous_lehmann` Запустить контейнер и активировать терминал<br>

### docker build
`-t`: ` docker build -t goals:latest .` Создать image с именем и тегом<br>

### docker network
`ls`: `docker network ls` Посмотреть список докер нетворков

### docker-compose
`--build` `docker-compose up --build server` Активировать режим отслеживания изменений в docker image

## END ---------------- Commands ----------------

## Kubernetes

+ [1. Какие проблемы решает Kubernetes](#1-какие-проблемы-решает-kubernetes)
+ [2. Что означает Kubernetes](#2-что-означает-kubernetes)
+ [3. Основные понятия](#3-основные-понятия)
+ [4. Что нужно подготовить для начала работы](#4-что-нужно-подготовить-для-начала-работы)
+ [5. Worker node](#5-worker-node)
+ [6. Master node](#6-master-node)
+ [7. Commands](#7-commands)
+ [8. Declarative setup](#8-declarative-setup)
+ [9. Volumes](#9-volumes)
+ [10. Environment variables](#10-environment-variables)
+ [11. Networking](#11-networking)

### 1. Какие проблемы решает Kubernetes
- Оркестрация - автоматизация процессов управления контейнерами.
- Самоисцеление -  автоматический перезапуск контейнеров при их сбоях
- Скалирование - гибкое увеличение или уменьшение числа контейнеров в зависимости от текущей нагрузки.
- Балансировка нагрузки - Распределение входящего трафика по контейнерам

### 2. Что означает Kubernetes
это открытая платформа для автоматизации развёртывания и масштабирования контейнеризированных приложений и управления ими.<br>
Kubernetes не является cloud service провайдером<br>
Может использоваться любым cloud service провайдером

### 3. Основные понятия
Worker node - Это отдельная физическая или виртуальная машина, на которой развёрнуты и выполняются контейнеры приложений.
- Pod(Container) - Базовая единица для запуска и управления приложениями: один или несколько контейнеров, которым гарантирован запуск на одном узле, обеспечивается разделение ресурсов и межпроцессное взаимодействие.
- Proxy/Config - Прокси для упаравления доступами к сети контейнера

Master node - Контролирует все контейнеры. Удаляет если они не нужны, создает новые при увеличении нагрузки

Cluster = Master node + Worker nodes 

### 4. Что нужно подготовить для начала работы
1. Создать кластер и ноды (Worker + Masker Node)
2. Настроить API Server, kubelet и другие сервисы Kubernetes
3. Создать другие провайдеры, которые могут быть необходимы для работы (Load Balancer, Filesystems etc)

### 5. Worker node
- Это компьютер/машина/виртуальная машина
- Управляется Master Node
  - Внутри находится один и более Pod
    - Это одно или более контейнеров приложений и их ресурсов (IP, volume etc)
    - Создаются и управляются Kubernetes (Master Node)
  - Внутри должен быть установлен docker для запуска контейнеров
  - Kubelet - приложение для взаимодействия master node с worker node
  - Proxy - для обработки входящих и исходящих запросов

### 6. Master node
- Внутри работает API Server для взаимодействия с Worker node
- Scheduler - Наблюдает за подами, выбирает worker node, на котором запустить Pod
- Kube-Controller Manager - контролирует и наблюдает за Worker node, меняет количество подов
- Cloud-Controller Manager - Тоже самое что Kube-Controller Manager только специфичен для конкретного cloud provider. Переводит инструкции для конкретного провайдера

### 7. Commands

+ [1. kuberctl create](#1-kuberctl-create)
+ [2. kuberctl get](#2-kuberctl-get)
+ [3. kuberctl delete](#3-kuberctl-delete)
+ [4. kubectl expose](#4-kubectl-expose)
+ [5. kubectl scale](#5-kubectl-scale)
+ [6. kubectl set](#6-kubectl-set)
+ [7. kubectl rollout](#7-kubectl-rollout)
+ [8. kubectl apply](#8-kubectl-apply)

#### 1. kuberctl create
Создать объект<br>
`kubectl create deployment first-app --image=juliwolf/kub-first-app`

#### 2. kuberctl get
Получить список объектов группы<br>
`kubectl get pods`
```
NAME                         READY   STATUS             RESTARTS   AGE
first-app-564775dddc-7qv52   0/1     ImagePullBackOff   0          60s
```

#### 3. kuberctl delete
Удалить объект определенной группы<br>
`kubectl delete deployment first-app`

#### 4. kubectl expose
Расшарить Ip адрес <br>
` kubectl expose deployment first-app --port 8080 --type=NodePort`<br>
`--type` может иметь разные значения
  - `ClusterIp` - будет доступен только внутри кластера
  - `NodePort` - Будет расшарен внутри workerNode
  - `LoadBalancer` - будет создан IP для доступа извне

#### 5. kubectl scale
Увеличить количество желаемых реплик<br>
`kubectl scale deployment/first-app --replicas=3`<br>
`--replicas`- говорит о том, сколько инстансов необходимо создать

#### 6. kubectl set
Обновить image для пода<br>
`kubectl set image deployment/first-app kub-first-app=juliwolf/kub-first-app`<br>
image будет обновлен, только если будет иметь другой код или другой tag

#### 7. kubectl rollout
Посмотреть состояние отката<br>
`kubectl rollout status deployment/first-app`<br><br>

`Waiting for deployment "first-app" rollout to finish: 1 out of 3 new replicas have been updated...`

Отменить последний deployment<br>
`kubectl rollout undo deployment/first-app`

Посмотреть историю деплоев<br>
`kubectl rollout history deployment/first-app`
```
deployment.apps/first-app 
REVISION  CHANGE-CAUSE
1         <none>
3         <none>
4         <none>

```

Откатить поды к определенному релизу<br>
`kubectl rollout undo deployment/first-app --to-revision=1`

#### 8. kubectl apply
Создать объект из конфигурационного файла/файлов<br>
`kubectl apply -f=deployment.yaml`

### 8. Declarative setup

+ [1. Setup example](#1-setup)
+ [2. Проверка работоспособности контейнера](#2-проверка-работоспособности-контейнера)
+ [3. Правила загрузки image для контейнера](#3-правила-загрузки-image-для-контейнера)

#### 1. Setup
```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend
spec:
  selector:
    app: second-app
  ports:
    - protocol: 'TCP'
      port: 80
      targetPort: 8080
    # - protocol: 'TCP'
    #   port: 443
    #   targetPort: 443
  type: LoadBalancer
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: second-app-deployment
spec:
  selector:
    matchLabels:
      app: second-app
      tier: second-app
    # matchExpressions
  replicas: 1
  template:
    metadata:
      labels: 
        app: second-app
        tier: second-app
    spec: 
      containers:
        - name: second-node
          image: juliwolf/kub-first-app:2
        # - name: ...
        #   image: ...
```

#### 2. Проверка работоспособности контейнера
За проверку работоспособности контейнера отвечает свойство `livenessProbe`
```yaml
    spec: 
      containers:
        - name: second-node
          image: juliwolf/kub-first-app:2
          livenessProbe:
            httpGet:
              path: /
              port: 8080
            periodSeconds: 10
            initialDelaySeconds: 5
```

#### 3. Правила загрузки image для контейнера
Свойство `imagePullPolicy` настраивает правила загрузки версии image для контейнера
- `IfNotPresent` Загружает новый image если локально его нет
- `Always` каждый раз, когда создается контейнер, будет загружаться новый image
- `Never` Кубер никогда не будет пытаться загрузить image, всегда будет использоваться локальный


### 9. Volumes

+ [1. Конфигурация volume](#1-конфигурация-volume)
+ [2. Типы volume](#2-типы-volume)
+ [3. Persistent volume](#3-persistent-volume)

#### 1. Конфигурация volume
Volume внутри pods кубера настраивается через `volumes`, где задается название и тип volume<br>
Для контейнера дополнительно необходимо указать свойство `volumeMounts`, внутри которого указывается путь до папки для монтирования volume и название volume который будет использоваться
```yaml
    spec:
      containers:
        - name: story
          image: juliwolf/kub-data-demo:1
          volumeMounts:
            - mountPath: /app/story
              name: story-volume
      volumes:
        - name: story-volume
          emptyDir: {}
```

#### 2. Типы volume
https://kubernetes.io/docs/concepts/storage/volumes/#volume-types
1. `emptyDir` - Директория внутри пода (удаляется при перезагрузке пода). Не шарится между несколькими инстансами
```yaml
      volumes:
        - name: story-volume
          emptyDir: {}
```
2. `hostPath` - Место на кластере, который содержит под. Подходит для кейсов, кода есть несколько инстансов на одном кластере
```yaml
      volumes:
        - name: story-volume
          hostPath:
            path: /data
            type: DirectoryOrCreate
```

#### 3. Persistent volume
https://kubernetes.io/docs/concepts/storage/persistent-volumes/<br>
Это volume, который не зависит от конкретной ноды<br>
Для настройки volume используется `kind: PersistentVolume`
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: host-pv
spec:
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  storageClassName: standard
  accessModes:
    # read by single node (pods from one node can read)
    - ReadWriteOnce
    # read by multiple node (pods from multiple node can read)
    - ReadOnlyMany
    # read and write by multiple node (pods from multiple node can read and write)
    - ReadWriteMany
  hostPath: 
    path: /data
    type: DirectoryOrCreate
```
Для использования данного volume или какой-то его части необходимо создать `kind: PersistentVolumeClaim`

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: host-pvc
spec:
  volumeName: host-pv
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

Для связи deployment и созданного `PersistentVolumeClaim` необходимо в volume указать `persistentVolumeClaim`
```yaml
...
    spec:
      containers:
        - name: story
          image: juliwolf/kub-data-demo:1
          volumeMounts:
            - mountPath: /app/story
              name: story-volume
      volumes:
        - name: story-volume
          persistentVolumeClaim:
            claimName: host-pvc
```

### 10. Environment variables
https://kubernetes.io/docs/concepts/containers/container-environment/ <br>
1. Самый простой способ передать переменные контейнеру, это прописать их в настройке контейнера в `env`
```yaml
...
    spec:
      containers:
        - name: story
          image: juliwolf/kub-data-demo:2
          env:
            - name: STORY_FOLDER
              value: 'story'
          volumeMounts:
            - mountPath: /app/story
              name: story-volume
      volumes:
        - name: story-volume
          persistentVolumeClaim:
            claimName: host-pvc
```

2. Создание ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: data-store-env
data:
  folder: 'story'
  # key: value...
  
```
Использование
```yaml
...
spec:
  containers:
    - name: story
      image: juliwolf/kub-data-demo:2
      env:
        - name: STORY_FOLDER
          valueFrom:
            configMapKeyRef:
              name: data-store-env
              key: folder
      volumeMounts:
        - mountPath: /app/story
          name: story-volume
```

### 11. Networking
## END ---------------- Kubernetes ----------------

