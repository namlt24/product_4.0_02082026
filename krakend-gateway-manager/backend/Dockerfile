# ---- Stage 1: build jar bang Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Cache dependency truoc de build lai nhanh khi chi sua source
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: runtime nho gon ----
# Khong can cai docker-cli: DockerReloadService goi thang Docker Engine API
# qua Unix socket bang java.nio (SocketChannel + UnixDomainSocketAddress).
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gateway-manager.jar app.jar
# Luu y: container nay chay bang root (khong switch user) vi no can quyen ghi
# vao /var/run/docker.sock (docker.sock tren host thuong chi cho root/nhom
# "docker" truy cap). Day la trade-off thuong gap cua pattern "docker-outside-
# of-docker". O moi truong production that, nen: (1) mount socket qua mot
# proxy co ACL rieng (vi du tecnativa/docker-socket-proxy) chi cho phep
# POST /containers/{id}/restart, hoac (2) thay co che reload bang mot
# webhook/sidecar rieng thay vi cho Control Plane truy cap thang Docker socket.
EXPOSE 9000
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
