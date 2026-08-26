# ---- Stage 1: build jar bang Maven ----
# JDK 25: Spring Boot 4.0.8 ho tro repackage bytecode JDK 25 dung (khac voi
# Spring Boot 3.3.4 truoc day - loi "Unsupported class file major version 69").
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
# Cache dependency truoc de build lai nhanh khi chi sua source
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: runtime nho gon ----
# Khong con can quyen truy cap /var/run/docker.sock nua: truoc day
# DockerReloadService goi Docker Engine API de restart container KrakenD rieng
# moi khi Deploy, gio backend TU LA gateway thuc thi (CompositeOrchestratorEngine),
# khong con container gateway nao khac can restart.
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gateway-manager.jar app.jar
# Elastic APM Java agent - tu dong instrument RestTemplate (UpstreamHttpExecutor)
# + HTTP server transaction (DynamicDispatcherController), KHONG can sua code app
# them gi ca. Tai truc tiep qua ADD (Docker tu download qua HTTPS - image alpine
# nay khong co san curl/wget) - dung KHOP version voi co.elastic.apm:apm-agent-api
# trong pom.xml (agent + API cung 1 phien ban, tranh lech).
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.56.0/elastic-apm-agent-1.56.0.jar /app/elastic-apm-agent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/app/elastic-apm-agent.jar", "-XX:+UseContainerSupport", "-jar", "app.jar"]
