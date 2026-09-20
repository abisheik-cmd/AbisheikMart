FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package \
    && mkdir -p /build/runtime \
    && cp target/abisheikmart.war /build/runtime/abisheikmart.war \
    && cp /root/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar /build/runtime/h2-2.2.224.jar

FROM eclipse-temurin:17-jre-jammy

ARG TOMCAT_VERSION=9.0.112
ENV CATALINA_HOME=/opt/tomcat \
    PATH=/opt/tomcat/bin:$PATH \
    DB_DRIVER=org.h2.Driver \
    DB_USERNAME=sa \
    DB_PASSWORD= \
    H2_DATA_DIR=/data/h2 \
    H2_PORT=9123

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL "https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" -o /tmp/tomcat.tar.gz \
    && mkdir -p /opt \
    && tar -xzf /tmp/tomcat.tar.gz -C /opt \
    && mv "/opt/apache-tomcat-${TOMCAT_VERSION}" "$CATALINA_HOME" \
    && rm /tmp/tomcat.tar.gz \
    && rm -rf "$CATALINA_HOME/webapps"/* \
    && mkdir -p /data/h2 /opt/h2

COPY --from=build /build/runtime/abisheikmart.war "$CATALINA_HOME/webapps/ROOT.war"
COPY --from=build /build/runtime/h2-2.2.224.jar /opt/h2/h2-2.2.224.jar
COPY railway-entrypoint.sh /usr/local/bin/railway-entrypoint.sh
RUN chmod +x /usr/local/bin/railway-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/railway-entrypoint.sh"]
