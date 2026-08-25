## Dockerfile don gian: KHONG build Maven trong container.
## Ly do: cac starter noi bo com.viettel.bccs:* chi resolve duoc tu ~/.m2 cuc bo da co san tren may
## dev (copy tay tu may noi bo Viettel) - container Docker khong co Nexus/Internet noi bo de tai lai.
## => Build jar tren host truoc (mvnw clean package -DskipTests), Dockerfile chi dong goi jar co san.
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8003
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
