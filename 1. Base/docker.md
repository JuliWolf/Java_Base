+ [Core concepts](#core-concepts)
+ [Commands](#commands)


## Core concepts

+ [1. Что такое докер](#1-что-такое-докер)
+ [2. Docker image](#2-docker-image)
+ [3. Docker container](#3-docker-container)
+ [4. Layer based instruction](#4-layer-based-instruction)

### 1. Что такое докер

Docker — это платформа, которая позволяет упаковать в контейнер приложение со всем окружением и зависимостями, а затем доставить и запустить его в целевой системе.<br>
Приложение, упакованное в контейнер, изолируется от операционной системы и других приложений. <br>
Поэтому разработчики могут не задумываться, в каком окружении будет работать их приложение, а инженеры по эксплуатации — единообразно запускать приложения и меньше заботиться о системных зависимостях.


### 2. Docker image
Образ Docker (Docker Image) - это неизменяемый файл, содержащий исходный код, библиотеки, зависимости, инструменты и другие файлы, необходимые для запуска приложения.<br>
Это шаблон для контейнера.<br><br>

Они представляют приложение и его виртуальную среду в определенный момент времени. Такая согласованность является одной из отличительных особенностей Docker. <br>
Он позволяет разработчикам тестировать и экспериментировать программное обеспечение в стабильных, однородных условиях.<br><br>

Образ - это шаблон, на основе которого создается контейнер, существует отдельно и не может быть изменен. <br>
При запуске контейнерной среды внутри контейнера создается копия файловой системы (docker образа) для чтения и записи.<br><br>

Содержат весь код, инстукции, данные, необходимые для создания контейнера


### 3. Docker container
Контейнер Docker (Docker Container) - это виртуализированная среда выполнения, в которой пользователи могут изолировать приложения от хостовой системы. <br>
Эти контейнеры представляют собой компактные портативные хосты, в которых можно быстро и легко запустить приложение.<br><br>

Важной особенностью контейнера является стандартизация вычислительной среды, работающей внутри контейнера. <br>
Это не только гарантирует, что ваше приложение работает в идентичных условиях, но и упрощает обмен данными с другими партнерами по команде.<br><br>

Контейнеры работают автономно, изолированно от основной системы и других контейнеров, и потому ошибка в одном из них не влияет на другие работающие контейнеры, а также поддерживающий их сервер.<br><br>

В отличие от виртуальных машин, где виртуализация выполняется на аппаратном уровне, контейнеры виртуализируются на уровне приложений.<br><br>

Работающий инстанс, созданный на основе image


### 4. Layer based instruction
При создании контейнера, докер кеширует состояние исходных файлов.
При повторной попытке создания контейнера на данных, которые не были изменены, докер не будет повторять весь процесс сначала, вместо этого возьмет из кеша
- Каждая команда в докере это отдельный слой, которая кешируется
- Если результат выполнения одной из команд не может быть взят из кеша, то все последующие команды тоже будут перевыполняться, так как нет гарантии, что после выполнения следующих команд результат результат не поменяется
- Все слои в итоге образуют image
- При создании контейнера докер создает еще один слой(layer) на основе image


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
+ [5. docker -(options)](#5-docker--options)


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
Удалить не активный контейнер
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
`docker cp {from_path} {to_path}`
`docker cp test/. distracted_swartz:/test`


### 5. docker -(options)

#### docker ps
`-a`: `docker ps -a` говорит о том, что мы хотим увидеть все контейнеры, которые когда либо были созданы, а не только активные

#### docker run
`-d`: `docker run -p 3000:3000 -d ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd` run with detach mode
`-p`: `docker run -p 3000:3000 -d ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd` открыть порт докера по определенному порту
`--rm`: `docker run -p 3000:3000 -d --rm ac8d5bf025fb71403ddbeac8984105dbd7a5580a20accb29b3779c44a23c03dd` удалить контейнер когда он остановится
`--name`: `docker run -p 3000:80 -d --rm --name goalsapp 122e98fcbfc4` назначить кастомное имя контейнеру

#### docker logs
`-f`: `docker logs -f zealous_lehmann` продолжать слушать лог в runtime

#### docker start
`-a`: `docker start -a zealous_lehmann` Запустить остановленный контейнер с attach модом
`-i`: `docker start -i zealous_lehmann` Запустить контейнер в интерактивном режиме
`-t`: `docker start -it zealous_lehmann` Запустить контейнер и активировать терминал

### docker build
`-t`: ` docker build -t goals:latest .` Создать image с именем и тегом

## END ---------------- Commands ----------------

