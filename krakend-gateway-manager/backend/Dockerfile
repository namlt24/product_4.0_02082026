# ---- Runtime don gian: COPY jar + APM agent da build san ben ngoai ----
#
# DOI 2026-09-14: bo multi-stage "mvn package trong container" - Maven trong
# container KHONG vao duoc repo.maven.apache.org tren mang cong ty (PKIX
# certificate_unknown: firewall giai mao TLS), trong khi Maven ben host dung
# repo/cache cua minh binh thuong. Nen build ben host truoc:
#   ./mvnw.cmd clean package -DskipTests
# lenh nay sinh CA 2 file target/gateway-manager.jar VA
# target/elastic-apm-agent.jar (xem pom.xml - dependency "elastic-apm-agent"
# scope=provided + maven-dependency-plugin execution "copy-apm-agent") - roi
# docker build COPY ca 2 vao image, khong tai gi qua mang trong luc build
# Docker nua (agent tung duoc ADD truc tiep tu repo1.maven.org o day, nhung bi
# rot mat khi doi sang mo hinh nay - sua 2026-09-14: khoi phuc lai qua duong
# host-Maven thay vi ADD, vi ADD se dinh dung PKIX certificate_unknown y het
# ly do da bo multi-stage build).
#
# (cp.2/cp.3/cp.4 deu duoc build dung cach nay - history image cho thay
# "COPY gateway-manager.jar /app/app.jar".)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY gateway-manager.jar app.jar
COPY elastic-apm-agent.jar elastic-apm-agent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/app/elastic-apm-agent.jar", "-XX:+UseContainerSupport", "-jar", "app.jar"]
