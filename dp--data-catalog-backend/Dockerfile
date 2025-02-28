FROM docker.art.lmru.tech/maven:3.9.3 AS builder

ARG WORKDIR=/application/
WORKDIR ${WORKDIR}

COPY pom.xml .
#RUN mvn verify --fail-never
COPY . .

RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:17-jdk-alpine
ARG WORKDIR=/application/
ARG KEYSTORES=/application/keystores
ARG TARGET=/application/target/application.jar

WORKDIR ${WORKDIR}
COPY --from=builder ${TARGET} ${WORKDIR}
COPY ./keystores $KEYSTORES
RUN chmod 777 $KEYSTORES

USER daemon

ENTRYPOINT exec java $JAVA_OPTS $JAVA_SECURITTY_OPTS -jar /application/application.jar $PROPG_OPTS
